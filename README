# polyline-encoder

This library allows easy conversion of JTS geometries and WKT to
Google encoded polylines. It was originally based on an implementation
of the encoding algorithm by Mark Rambow, but was extended for additional
functionality and Mavenization.

https://developers.google.com/maps/documentation/utilities/polylinealgorithm

## Requirements

Java 11+
Maven 3+

## Example usage

```
import com.axiomalaska.polylineencoder.EncodedPolyline;
import com.axiomalaska.polylineencoder.PolylineEncoder;
import com.axiomalaska.polylineencoder.UnsupportedGeometryTypeException;
import com.axiomalaska.polylineencoder.WktUtil;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public static void jtsExample() throws UnsupportedGeometryTypeException{
    GeometryFactory geomFactory = new GeometryFactory( new PrecisionModel( PrecisionModel.FLOATING ), 4326 );
    Coordinate[] coords = new Coordinate[3];
    coords[0] = new Coordinate( 0, 0 );
    coords[1] = new Coordinate( 1, 1 );
    coords[2] = new Coordinate( 2, 2 );
    LineString jtsLineString = geomFactory.createLineString( coords );

    EncodedPolyline encodedLineString = PolylineEncoder.encode( jtsLineString );    
    System.out.println( encodedLineString.getPoints() );
    System.out.println( encodedLineString.getLevels() );               
}

public static void wktExample() throws UnsupportedGeometryTypeException{
    Geometry wktPolygon = WktUtil.wktToGeom( "POLYGON (( 0 0, 0 5, 5 5, 5 0, 0 0 ))" );
	 
    EncodedPolyline encodedPolygon = PolylineEncoder.encode( wktPolygon );    
    System.out.println( encodedPolygon.getPoints() );
    System.out.println( encodedPolygon.getLevels() );               
}
```
