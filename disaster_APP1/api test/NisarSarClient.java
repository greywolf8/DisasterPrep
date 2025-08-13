package com.example;
import ucar.nc2.*;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.ma2.Array;
import ucar.ma2.Index;
import java.io.IOException;

public class NisarSarClient {

    /**
     * Reads a SAR variable (e.g. coherence or displacement)
     * from a NISAR HDF5/NetCDF file.
     *
     * @param datasetUrl local path or OPeNDAP/S3 URL to .h5 file
     * @param variablePath full variable name in dataset (e.g. "science/LSAR/coherence")
     * @return 2D array of floats (lat × lon or pixel × pixel)
     * @throws IOException if file cannot be opened
     */
    public float[][] readSarMetric(String datasetUrl, String variablePath) throws IOException {
        try (NetcdfFile ncfile = NetcdfDataset.open(datasetUrl)) {
            Variable var = ncfile.findVariable(variablePath);
            if (var == null) {
                throw new IOException("Variable not found: " + variablePath);
            }
            Array data = var.read();
            int[] shape = data.getShape();
            // assuming 2D for simplicity
            int rows = shape[0], cols = shape[1];
            float[][] result = new float[rows][cols];
            Index index = data.getIndex();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    result[i][j] = data.getFloat(index.set(i, j));
                }
            }
            return result;
        }
    }

    public static void main(String[] args) {
        String url = "";
        String varPath = "science/LSAR/coherence";
        NisarSarClient client = new NisarSarClient();
        try {
            float[][] sar = client.readSarMetric(url, varPath);
            System.out.printf("SAR metric loaded: %d × %d%n", sar.length, sar[0].length);
            // Insert into ML feature store or database
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}