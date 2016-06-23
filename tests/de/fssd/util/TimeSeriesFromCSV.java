package de.fssd.util;

import de.fssd.model.TimeSeries;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Reads from Csv File the timeseries.
 * Assumed Format is
 * 1st column: time
 * next columns are states in same order as varIdsOrdered
 * last columns are nodes (gates) with names corresponding to nodeID
 */
public class TimeSeriesFromCSV implements TimeSeries {

    private Map<Integer, List<Float>> varIdToSeriesMap;
    private Map<String, List<Float>> gateIdToSeriesMap;

    private int numberSamplingPoints = 0;

    public TimeSeriesFromCSV(File file, List<Integer> varIdsOrdered) throws IOException {
        varIdToSeriesMap = new HashMap<>();
        gateIdToSeriesMap = new HashMap<>();
        parseCSV(file, varIdsOrdered);
    }

    private void parseCSV(File file, List<Integer> varIdsOrdered) throws IOException {

        try (BufferedReader br = new BufferedReader(new FileReader(file))) { // Auto closeable, no explicit close required

            ICsvMapReader mapReader = new CsvMapReader(br, CsvPreference.STANDARD_PREFERENCE);
            // the header columns are used as the keys to the Map
            final String[] header = mapReader.getHeader(true);
            if(header.length <= varIdsOrdered.size()) {
                throw new AssertionError("See assumptions on format in doc of this class");
            }

            varIdsOrdered.forEach(id -> varIdToSeriesMap.put(id, new LinkedList<>()));
            for (int i = varIdsOrdered.size() + 1; i < header.length; i++) {
                gateIdToSeriesMap.put(header[i], new LinkedList<>());
            }

            System.out.println(Arrays.toString(header));

            Map<String, String> customerMap;
            while( (customerMap = mapReader.read(header)) != null ) {
                numberSamplingPoints++;
                for(int i = 0; i < varIdsOrdered.size(); i++) {
                    float value = Float.parseFloat(customerMap.get(header[i+1]));
                    List<Float> series = varIdToSeriesMap.get(varIdsOrdered.get(i));
                    series.add(value);
                }
                for (String nodeID : gateIdToSeriesMap.keySet()) {
                    float value = Float.parseFloat(customerMap.get(nodeID));
                    gateIdToSeriesMap.get(nodeID).add(value);
                }
            }
            System.out.println(varIdToSeriesMap);
            System.out.println(gateIdToSeriesMap);
        }
    }

    @Override
    public int getTimeseriesCount() {
        return numberSamplingPoints;
    }

    @Override
    public Stream<Float> getProbabilitySeries(int varID) {
        return varIdToSeriesMap.get(varID).stream();
    }

    public List<Float> getGateResult(String gateID) {
        return gateIdToSeriesMap.get(gateID);
    }
}
