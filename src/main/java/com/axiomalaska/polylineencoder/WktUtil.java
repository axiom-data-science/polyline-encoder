package com.axiomalaska.polylineencoder;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class WktUtil {
    public static Geometry wktToGeom( String wkt ) throws ParseException{ 
        return new WKTReader().read( wkt );
    }
    
    public static Geometry wktToGeom( String wkt, int epsg ) throws ParseException{ 
        GeometryFactory geomFactory = new GeometryFactory( new PrecisionModel( PrecisionModel.FLOATING ), epsg );
        return new WKTReader( geomFactory ).read( wkt );
    }    
}
