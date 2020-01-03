package com.axiomalaska.polylineEncoder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.axiomalaska.polylineencoder.EncodedPolyline;
import com.axiomalaska.polylineencoder.PolylineEncoder;
import com.axiomalaska.polylineencoder.UnsupportedGeometryTypeException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;

public class PolylineEncoderTest extends TestCase {
    private Polygon testPolygon;
    private LineString testLineString;
    
    protected void setUp() throws ParseException, IOException{
    	BufferedReader reader = new BufferedReader( new FileReader( "src/test/resources/testPolygonWKT.txt" ) );
        WKTReader wktReader = new WKTReader();
        testPolygon = (Polygon) wktReader.read( reader );
        
        GeometryFactory geomFactory = new GeometryFactory( new PrecisionModel( PrecisionModel.FLOATING ), 4326 );
        Coordinate[] coords = new Coordinate[3];
        coords[0] = new Coordinate( 0, 0 );
        coords[1] = new Coordinate( 1, 1 );
        coords[2] = new Coordinate( 2, 2 );
        testLineString = geomFactory.createLineString( coords );
    }  
    
    protected void tearDown(){        
    }
    
    public void testPolygonEncoding() throws ParseException, UnsupportedGeometryTypeException{
        EncodedPolyline ep = PolylineEncoder.encode( testPolygon );
        System.out.println( ep.getPoints() );
        System.out.println( ep.getLevels() );
        assertTrue( ep.getPoints()!= null );
        assertTrue( ep.getLevels()!= null );

        EncodedPolyline elp = PolylineEncoder.encode( testLineString );
        System.out.println( elp.getPoints() );
        System.out.println( elp.getLevels() );
        assertTrue( elp.getPoints()!= null );
        assertTrue( elp.getLevels()!= null );
    }
    
    public void testPolygonDumbEncoding() throws ParseException, UnsupportedGeometryTypeException{
        EncodedPolyline ep = PolylineEncoder.dumbEncode( testPolygon );
        System.out.println( ep.getPoints() );
        System.out.println( ep.getLevels() );
        assertTrue( ep.getPoints()!= null );
        assertTrue( ep.getLevels()!= null );

        EncodedPolyline elp = PolylineEncoder.dumbEncode( testLineString );
        System.out.println( elp.getPoints() );
        System.out.println( elp.getLevels() );
        assertTrue( elp.getPoints()!= null );
        assertTrue( elp.getLevels()!= null );    
    }
    
}
