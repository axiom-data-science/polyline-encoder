package com.axiomalaska.polylineencoder;

public class UnsupportedGeometryTypeException extends Exception{
    private static final long serialVersionUID = 1L;

    public UnsupportedGeometryTypeException(String message) {
        super(message);
    }

    public UnsupportedGeometryTypeException(Throwable cause){
        super(cause);
    }

    public UnsupportedGeometryTypeException(String message, Throwable cause){
        super(message, cause);
    }
    
    public UnsupportedGeometryTypeException(String unsupportedType, String[] supportedTypes){
        super("Geometry type " + unsupportedType + " not supported. Expected one of the following: " + arrayToString( supportedTypes ) );
    }
 
    private static String arrayToString( String[] a ) {
        return arrayToString( a, ", " );
    }
    
    private static String arrayToString( String[] a, String separator) {
        if (a == null || separator == null) {
            return null;
        }
        StringBuffer result = new StringBuffer();
        if (a.length > 0) {
            result.append(a[0]);
            for (int i=1, n = a.length; i < n; i++) {
                result.append( separator );
                result.append( a[i] );
            }
        }
        return result.toString();
    }
}