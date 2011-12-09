package com.axiomalaska.polylineencoder;

public class PolylineEncoderSettings {
    private int numLevels = 18;
    private int zoomFactor = 2;
    private double verySmall = 0.00001;
    private boolean forceEndpoints = true;
    private double[] zoomLevelBreaks;

    public PolylineEncoderSettings(int numLevels, int zoomFactor, double verySmall, boolean forceEndpoints) {
        this.numLevels = numLevels;
        this.zoomFactor = zoomFactor;
        this.verySmall = verySmall;
        this.forceEndpoints = forceEndpoints;
        initZoomLevelBreaks();
    }

    public PolylineEncoderSettings() {
        initZoomLevelBreaks();
    }

    private void initZoomLevelBreaks(){
        this.zoomLevelBreaks = new double[numLevels];

        for (int i = 0; i < numLevels; i++) {
            this.zoomLevelBreaks[i] = verySmall * Math.pow( this.zoomFactor, numLevels - i - 1);
        }        
    }
    
    public int getNumLevels() {
        return numLevels;
    }

    public void setNumLevels(int numLevels) {
        this.numLevels = numLevels;
    }

    public int getZoomFactor() {
        return zoomFactor;
    }

    public void setZoomFactor(int zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    public double getVerySmall() {
        return verySmall;
    }

    public void setVerySmall(double verySmall) {
        this.verySmall = verySmall;
    }

    public boolean isForceEndpoints() {
        return forceEndpoints;
    }

    public void setForceEndpoints(boolean forceEndpoints) {
        this.forceEndpoints = forceEndpoints;
    }

    public double[] getZoomLevelBreaks() {
        return zoomLevelBreaks;
    }

    public void setZoomLevelBreaks(double[] zoomLevelBreaks) {
        this.zoomLevelBreaks = zoomLevelBreaks;
    }
}
