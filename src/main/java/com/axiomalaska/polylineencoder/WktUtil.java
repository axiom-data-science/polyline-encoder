package com.axiomalaska.polylineencoder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class WktUtil {
    public static Geometry wktToGeom( String wkt ) throws ParseException{ 
        return new WKTReader().read( wkt );
    }
    
    public static Geometry wktToGeom( String wkt, int epsg ) throws ParseException{ 
        GeometryFactory geomFactory = new GeometryFactory( new PrecisionModel( PrecisionModel.FLOATING ), epsg );
        return new WKTReader( geomFactory ).read( wkt );
    }    
}
