package com.axiomalaska.polylineencoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.distance.DistanceOp;

/**
 * Class to apply the Google polyline encoding algorithm to JTS geometries (LineStrings and Polygons). 
 * Original encoding methods are taken from Mark Rambow's implementation (the hard work).
 * Adapted and expanded by Shane StClair (JTS geometry usage, statification, etc).
 * 
 * @see http://code.google.com/apis/maps/documentation/utilities/polylinealgorithm.html
 * @see http://facstaff.unca.edu/mcmcclur/GoogleMaps/EncodePolyline/
 */
public class PolylineEncoder {
    private static final String[] ALLOWED_WKT_TYPES = new String[]{"LINESTRING","POLYGON"};
    private static final GeometryFactory GEOMETRY_FACTORY_4326 = buildGeometryFactory( 4326 );
    
    private static GeometryFactory buildGeometryFactory( int epsg ){
        return new GeometryFactory( new PrecisionModel( PrecisionModel.FLOATING ), epsg );
    }

    /**
     * Douglas-Peucker algorithm, adapted for encoding. Uses default encoding settings.
     * @param geometry
     * @return
     * @throws UnsupportedGeometryTypeException
     * @throws ParseException 
     */
    public static EncodedPolyline encode( String wkt ) throws UnsupportedGeometryTypeException, ParseException {
        return encode( WktUtil.wktToGeom(wkt), new PolylineEncoderSettings() );
    }

    /**
     * Douglas-Peucker algorithm, adapted for encoding. Uses default encoding settings.
     * @param geometry
     * @return
     * @throws UnsupportedGeometryTypeException
     * @throws ParseException 
     */
    public static EncodedPolyline encode( String wkt, int epsg ) throws UnsupportedGeometryTypeException, ParseException {
        return encode( WktUtil.wktToGeom(wkt, epsg), new PolylineEncoderSettings() );
    }
    
    
    /**
     * Douglas-Peucker algorithm, adapted for encoding. Uses default encoding settings.
     * @param geometry
     * @return
     * @throws UnsupportedGeometryTypeException
     */
    public static EncodedPolyline encode( Geometry geometry ) throws UnsupportedGeometryTypeException {
        return encode( geometry, new PolylineEncoderSettings() );
    }
    
    /**
     * Douglas-Peucker algorithm, adapted for encoding. Accepts custom encoding settings.
     * 
     * @param geometry JTS geometry (LineString or Polygon) 
     * @return
     * @throws UnsupportedGeometryTypeException
     */
    public static EncodedPolyline encode( Geometry geometry, PolylineEncoderSettings settings ) throws UnsupportedGeometryTypeException {
        LineString line = getLineToEncode( geometry );
        int i, maxLoc = 0;
        Stack<int[]> stack = new Stack<int[]>();
        double[] dists = new double[ line.getNumPoints() ];
        double maxDist, absMaxDist = 0.0, temp = 0.0;
        int[] current;
        String encodedPoints, encodedLevels;

        if( line.getNumPoints() > 2) {
            int[] stackVal = new int[] { 0, ( line.getNumPoints() - 1) };
            stack.push(stackVal);

            while (stack.size() > 0) {
                current = stack.pop();
                maxDist = 0;

                for (i = current[0] + 1; i < current[1]; i++) {
                    temp = PolylineEncoder.distance(
                         line.getPointN( i )
                        ,line.getPointN( current[0] )
                        ,line.getPointN( current[1] )
                    );
                    if (temp > maxDist) {
                        maxDist = temp;
                        maxLoc = i;
                        if (maxDist > absMaxDist) {
                            absMaxDist = maxDist;
                        }
                    }
                }
                if (maxDist > settings.getVerySmall() ) {
                    dists[maxLoc] = maxDist;
                    int[] stackValCurMax = { current[0], maxLoc };
                    stack.push(stackValCurMax);
                    int[] stackValMaxCur = { maxLoc, current[1] };
                    stack.push(stackValMaxCur);
                }
            }
        }

        EncodedPolyline encodedPolyline = new EncodedPolyline();
        
        encodedPoints = PolylineEncoder.createEncodings( line, dists );
        encodedPolyline.setPoints( encodedPoints );
        
        encodedLevels = PolylineEncoder.encodeLevels( settings, line, dists, absMaxDist );
        encodedPolyline.setLevels( encodedLevels );

        return encodedPolyline;
    }

    /**
     * distance(p0, p1, p2) computes the distance between the point p0 and the
     * segment [p1,p2]. This could probably be replaced with something that is a
     * bit more numerically stable.
     * 
     * @param point
     * @param point2
     * @param point3
     * @return
     */
    private static double distance(Point point, Point point2, Point point3) {
        LineString line = GEOMETRY_FACTORY_4326.createLineString( new Coordinate[]{
            point2.getCoordinate()
           ,point3.getCoordinate()           
        });
        return DistanceOp.distance( point, line );
    }

    private static int floor1e5(double coordinate) {
        return (int) Math.floor(coordinate * 1e5);
    }

    private static String encodeSignedNumber(int num) {
        int sgn_num = num << 1;
        if (num < 0) {
            sgn_num = ~(sgn_num);
        }
        return (encodeNumber(sgn_num));
    }

    private static String encodeNumber(int num) {
        StringBuffer encodeString = new StringBuffer();

        while (num >= 0x20) {
            int nextValue = (0x20 | (num & 0x1f)) + 63;
            encodeString.append((char) (nextValue));
            num >>= 5;
        }

        num += 63;
        encodeString.append((char) (num));

        return encodeString.toString();
    }

    /**
     * Now we can use the previous function to march down the list of points and
     * encode the levels. Like createEncodings, we ignore points whose distance
     * (in dists) is undefined.
     */
    private static String encodeLevels( PolylineEncoderSettings settings, LineString line, double[] dists, double absMaxDist){
        StringBuffer encodedLevels = new StringBuffer();

        List<Double> validDists = new ArrayList<Double>();
        for( int k = 0, kn = dists.length; k < kn; k++ ){
            if( dists[k] > 0 ){
                double ok = dists[k];
                validDists.add( ok );
            }
        }
        
        @SuppressWarnings("unused")
        int ok = 0;
        
        //start point
        if ( settings.isForceEndpoints() ) {
            encodedLevels.append( encodeNumber( settings.getNumLevels() - 1 ) );
        } else {
            encodedLevels.append( encodeNumber( settings.getNumLevels() - PolylineEncoder.computeLevel( settings, absMaxDist ) - 1 ) );
        }
        
        //middle points        
        for ( int i = 1, n = line.getNumPoints(); i < n - 1; i++) {
            if( dists[i] != 0){
                encodedLevels.append( encodeNumber( settings.getNumLevels() - PolylineEncoder.computeLevel( settings, dists[i] ) - 1 ) );
            }
        }
        
        //end point
        if( settings.isForceEndpoints() ){
            encodedLevels.append(encodeNumber( settings.getNumLevels() - 1));
        } else {
            encodedLevels.append(encodeNumber( settings.getNumLevels() - PolylineEncoder.computeLevel( settings, absMaxDist) - 1));
        }
        
        return encodedLevels.toString();
    }

    /**
     * This computes the appropriate zoom level of a point in terms of it's
     * distance from the relevant segment in the DP algorithm. Could be done in
     * terms of a logarithm, but this approach makes it a bit easier to ensure
     * that the level is not too large.
     */
    private static int computeLevel( PolylineEncoderSettings settings, double absMaxDist) {
        int lev = 0;
        if (absMaxDist > settings.getVerySmall() ){
            while( absMaxDist < settings.getZoomLevelBreaks()[lev] ) {
                lev++;
            }
            return lev;
        }
        return lev;
    }

    private static String createEncodings( LineString line, double[] dists) {
        StringBuffer encodedPoints = new StringBuffer();
        
        int plat = 0;
        int plng = 0;
        
        for (int i = 0, n = line.getNumPoints(); i < n; i++) {
            if( dists[i] != 0 || i == 0 || i == n - 1) {
                Point point = line.getPointN( i );

                int lnge5 = floor1e5( point.getX() );
                int late5 = floor1e5( point.getY() );                

                int dlat = late5 - plat;
                int dlng = lnge5 - plng;

                plat = late5;
                plng = lnge5;

                encodedPoints.append(encodeSignedNumber(dlat));
                encodedPoints.append(encodeSignedNumber(dlng));

            }
        }
        return encodedPoints.toString();
    }
        
    public static EncodedPolyline dumbEncodeFromWkt( String wkt ) throws UnsupportedGeometryTypeException, ParseException {
        Geometry geometry = new WKTReader().read( wkt );
        return dumbEncode( geometry );
    }

    
    /**
     * Encodes all points in a JTS geometry and sets 
     * the level to display all points at all levels. Not efficient, but
     * useful if you can't get things quite right using the normal
     * Douglas Peucker methodology. 
     * 
     * @param geometry JTS geometry (LineString or Polygon)
     * @return
     * @throws UnsupportedGeometryTypeException
     */
    public static EncodedPolyline dumbEncode( Geometry geometry ) throws UnsupportedGeometryTypeException {
        PolylineEncoderSettings settings = new PolylineEncoderSettings();
        return dumbEncode( geometry, settings.getNumLevels() - 1, 1 );
    }
    
    
    /**
     * Encodes all points in a JTS geometry and sets 
     * the level to display all points at all levels. Not efficient, but
     * useful if you can't get things quite right using the normal
     * Douglas Peucker methodology.
     * 
     * @param geometry JTS geometry (LineString or Polygon)
     * @param level Level for which to encode the points (usually the lowest zoom level).
     * @param step Step size to use when moving through points (use 1 to include all points)
     * @return
     * @throws UnsupportedGeometryTypeException
     */
    public static EncodedPolyline dumbEncode( Geometry geometry, int level, int step) throws UnsupportedGeometryTypeException {
        LineString line = getLineToEncode( geometry );

        StringBuffer encodedPoints = new StringBuffer();
        StringBuffer encodedLevels = new StringBuffer();

        int plat = 0;
        int plng = 0;

        for (int i = 0, n = line.getNumPoints(); i < n; i += step) {
            Point point = (Point) line.getPointN( i );

            int lnge5 = floor1e5( point.getX() );
            int late5 = floor1e5( point.getY() );

            int dlat = late5 - plat;
            int dlng = lnge5 - plng;

            plat = late5;
            plng = lnge5;

            encodedPoints.append( encodeSignedNumber(dlat) ).append( encodeSignedNumber(dlng) );
            encodedLevels.append( encodeNumber(level) );
        }

        EncodedPolyline encodedPolyline = new EncodedPolyline();
        encodedPolyline.setPoints( encodedPoints.toString() );
        encodedPolyline.setLevels( encodedLevels.toString() );

        return encodedPolyline;
    }
 
       
    private static LineString getLineToEncode( Geometry geometry ) throws UnsupportedGeometryTypeException{
        LineString line = null;
        if( geometry.getGeometryType().toUpperCase().equals("LINESTRING") ){
            line = (LineString) geometry;
        } else if ( geometry.getGeometryType().toUpperCase().equals("POLYGON") ){
            line = ( (Polygon) geometry ).getExteriorRing();
        } else {
            throw new UnsupportedGeometryTypeException( geometry.getGeometryType(), ALLOWED_WKT_TYPES );            
        }
        return line;
    }   
}