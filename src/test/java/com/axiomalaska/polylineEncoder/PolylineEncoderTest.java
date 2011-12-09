package com.axiomalaska.polylineEncoder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.axiomalaska.polylineencoder.EncodedPolyline;
import com.axiomalaska.polylineencoder.PolylineEncoder;
import com.axiomalaska.polylineencoder.UnsupportedGeometryTypeException;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import junit.framework.TestCase;

public class PolylineEncoderTest extends TestCase {
    private Polygon testPolygon;
    
    protected void setUp() throws ParseException, IOException{
    	BufferedReader reader = new BufferedReader( new FileReader( "src/test/resources/testPolygonWKT.txt" ) );
        WKTReader wktReader = new WKTReader();
        testPolygon = (Polygon) wktReader.read( reader );
    }  
    
    protected void tearDown(){        
    }
    
    public void testPolygonEncoding() throws ParseException, UnsupportedGeometryTypeException{
        EncodedPolyline ep = PolylineEncoder.encode( testPolygon );
        System.out.println( ep.getPoints() );
        System.out.println( ep.getLevels() );
        assertTrue( ep.getPoints()!= null );
        assertTrue( ep.getLevels()!= null );
    }
    
    public void testPolygonDumbEncoding() throws ParseException, UnsupportedGeometryTypeException{
        EncodedPolyline ep = PolylineEncoder.dumbEncode( testPolygon );
        System.out.println( ep.getPoints() );
        System.out.println( ep.getLevels() );
        assertTrue( ep.getPoints()!= null );
        assertTrue( ep.getLevels()!= null );
    }
    
}
