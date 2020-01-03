package com.axiomalaska.polylineencoder;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.DefaultCoordinateOperationFactory;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.axiomalaska.polylineencoder.UnsupportedGeometryTypeException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

public class Reprojector {   
    public static Geometry reproject( String wkt, int fromSrid, int toSrid) throws MismatchedDimensionException, NoSuchAuthorityCodeException, FactoryException, UnsupportedGeometryTypeException, TransformException, ParseException{
        return reproject( WktUtil.wktToGeom( wkt, fromSrid ), toSrid );
    }
    
    public static Geometry reproject( Geometry geom, int toSrid) throws NoSuchAuthorityCodeException, FactoryException, UnsupportedGeometryTypeException, MismatchedDimensionException, TransformException{
        DefaultCoordinateOperationFactory trFactory = new DefaultCoordinateOperationFactory();

        CoordinateReferenceSystem sourceCS = CRS.decode("EPSG:" + geom.getSRID() );
        CoordinateReferenceSystem targetCS = CRS.decode("EPSG:" + toSrid );
        CoordinateOperation tr = trFactory.createOperation(sourceCS, targetCS);
        MathTransform mtrans = tr.getMathTransform();
        
        Geometry newGeom = null;
        GeometryFactory geomFactory = new GeometryFactory( new PrecisionModel( PrecisionModel.FLOATING ), toSrid );
        switch( getGeometryType( geom ) ){
            case POINT:
                Point point = (Point) geom;
                DirectPosition2D point2d = new DirectPosition2D( point.getX(), point.getY() );               
                mtrans.transform( point2d, point2d ); 
                newGeom = geomFactory.createPoint( new Coordinate( point2d.getX(), point2d.getY() ) );
                break;
            case LINESTRING: 
                newGeom = reprojectLineString( geomFactory, mtrans, (LineString) geom );
                break;
            case POLYGON:
                Polygon polygon = (Polygon) geom;
                LinearRing shell = lineStringToLinearRing( reprojectLineString( geomFactory, mtrans, polygon.getExteriorRing() ) );
                int numHoles = polygon.getNumInteriorRing();
                LinearRing[] holes = new LinearRing[ numHoles ];
                for( int i = 0; i < numHoles; i++ ){
                    holes[i] = lineStringToLinearRing( reprojectLineString( geomFactory, mtrans, polygon.getInteriorRingN(i) ) );
                }                
                newGeom = geomFactory.createPolygon(shell, holes);
                break;
        }
        
        return newGeom;
    }
    
    private static LinearRing lineStringToLinearRing( LineString lineString ){
        GeometryFactory geomFactory = new GeometryFactory( lineString.getPrecisionModel(), lineString.getSRID() );
        Coordinate[] newCoords = null;
        if( !lineString.getStartPoint().equals( lineString.getEndPoint() ) ){
            newCoords = new Coordinate[ lineString.getNumPoints() + 1 ];
            for( int i = 0, len = lineString.getNumPoints(); i < len; i++ ){
                newCoords[i] = lineString.getCoordinateN(i);
            }
            newCoords[ lineString.getNumPoints() ] = lineString.getCoordinateN(0);
        } else {
            newCoords = lineString.getCoordinates();
        }
        return geomFactory.createLinearRing( newCoords );
    }
    
    private static LineString reprojectLineString( GeometryFactory geomFactory, MathTransform mtrans, LineString lineString ) throws TransformException{
        int numPoints = lineString.getNumPoints();
        double points[] = new double[ numPoints * 2 ];
        for( int i = 0; i < numPoints; i++ ){
            points[ i * 2 ] = lineString.getPointN( i ).getX();
            points[ ( i * 2 ) + 1 ] = lineString.getPointN( i ).getY();
        }
        
        mtrans.transform( points, 0, points, 0, numPoints );
        
        Coordinate[] newCoordinates = new Coordinate[ numPoints ];
        for( int i = 0; i < numPoints; i++ ){
            newCoordinates[i] = new Coordinate( points[i], points[i + 1] );
        }
        return geomFactory.createLineString( newCoordinates );        
    }
    
    private static GeometryType getGeometryType( Geometry geom ) throws UnsupportedGeometryTypeException{
        String geomType = geom.getGeometryType().toUpperCase();
        String[] validTypes = new String[ GeometryType.values().length ];
        
        int i = 0;
        for( GeometryType geoType : GeometryType.values() ){
            if( geomType.equals( geoType.toString() ) ){
                return geoType; 
            }
            validTypes[ i++ ] = geoType.toString();
        }
        throw new UnsupportedGeometryTypeException( geomType, validTypes );
    }

    public static Geometry swapOrdinates( Geometry geom ){
        GeometryFactory geomFactory = new GeometryFactory( new PrecisionModel( PrecisionModel.FLOATING ), geom.getSRID() );
        Point oldPoint = (Point) geom;
        return geomFactory.createPoint( new Coordinate( oldPoint.getY(), oldPoint.getX() ) );        
    }
}