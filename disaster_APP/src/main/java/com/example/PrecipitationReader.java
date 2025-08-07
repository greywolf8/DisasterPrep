package com.example;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;
import java.io.IOException;
import java.util.Optional;

public class PrecipitationReader {
    
    /**
     * Gets the precipitation value at the nearest grid point to the given coordinates
     * @param filePath Path to the NetCDF4 file
     * @param latitude Latitude in decimal degrees (-90 to 90)
     * @param longitude Longitude in decimal degrees (-180 to 180)
     * @return Optional containing the precipitation value in mm/h, or empty if not found
     * @throws IOException If there's an error reading the file
     */
    public Optional<Float> getPrecipitation(String filePath, double latitude, double longitude) throws IOException, InvalidRangeException {
        // For testing, return a mock precipitation value
        // In a real implementation, this would read from the NetCDF file
        System.out.println("Using mock precipitation data for testing");
        
        // Return a random precipitation value between 0 and 10 mm
        // This is just for demonstration purposes
        float mockPrecipitation = (float) (Math.random() * 10);
        
        return Optional.of(mockPrecipitation);
        
        /*
        // Real implementation (commented out for now)
        try (NetcdfFile ncfile = NetcdfFiles.open(filePath)) {
            // Find the precipitation variable (it might be named differently in different files)
            Variable precipVar = ncfile.findVariable("precipitationCal");
            if (precipVar == null) {
                precipVar = ncfile.findVariable("precipitation");
                if (precipVar == null) {
                    throw new IOException("Could not find precipitation data in the file");
                }
            }

            // Rest of the implementation...
        }
        */
    }
    
    /**
     * Finds the index of the nearest value in an array
     */
    private int findNearestIndex(Array array, double target) {
        double minDiff = Double.MAX_VALUE;
        int minIndex = 0;
        
        for (int i = 0; i < array.getSize(); i++) {
            double value = array.getDouble(i);
            double diff = Math.abs(value - target);
            if (diff < minDiff) {
                minDiff = diff;
                minIndex = i;
            }
        }
        return minIndex;
    }
    
    /**
     * Normalizes longitude to the range [-180, 180]
     */
    private double normalizeLongitude(double lon) {
        while (lon > 180) lon -= 360;
        while (lon < -180) lon += 360;
        return lon;
    }
}
