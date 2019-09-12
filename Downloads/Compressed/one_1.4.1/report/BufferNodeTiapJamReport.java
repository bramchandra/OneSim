/* 
 * 
 * 
 */
package report;

/**
 * Records the average buffer occupancy and its variance with format:
 * <p>
 * <Simulation time> <average buffer occupancy % [0..100]> <variance>
 * </p>
 *
 *
 */
import java.util.*;
//import java.util.List;
//import java.util.Map;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.Settings;
import core.SimClock;
import core.UpdateListener;

public class BufferNodeTiapJamReport extends Report implements UpdateListener {

    /**
     * Record occupancy every nth second -setting id ({@value}). Defines the
     * interval how often (seconds) a new snapshot of buffer occupancy is taken
     * previous:5
     */
    public static final String BUFFER_REPORT_INTERVAL = "occupancyInterval";
    /**
     * Default value for the snapshot interval
     */
    public static final int DEFAULT_BUFFER_REPORT_INTERVAL = 3600;

    private double lastRecord = Double.MIN_VALUE;
    private int interval;

    private Map<DTNHost, List<Double>> bufferCounts = new HashMap<DTNHost, List<Double>>();
    private int updateCounter = 0;  //new added
    private String print;  //new added

    public BufferNodeTiapJamReport() {
        super();

        Settings settings = getSettings();
        if (settings.contains(BUFFER_REPORT_INTERVAL)) {
            interval = settings.getInt(BUFFER_REPORT_INTERVAL);
        } else {
            interval = -1;
            /* not found; use default */
        }

        if (interval < 0) {
        /* not found or invalid value -> use default */
        interval = DEFAULT_BUFFER_REPORT_INTERVAL;
        }
    }

    public void updated(List<DTNHost> hosts) {

        if (isWarmup()) {
            return;
        }

        if (SimClock.getTime() - lastRecord >= interval) {
            lastRecord = SimClock.getTime();
            for (DTNHost ho : hosts) {
                double temp = ho.getBufferOccupancy();
                temp = (temp <= 100.0) ? (temp) : (100.0);

                if (bufferCounts.containsKey(ho)) {
                    List bebas = bufferCounts.get(ho);
                    bebas.add(temp);
                    bufferCounts.put(ho, bebas);
                } else {
                    List<Double> bebas = new LinkedList();
                    bufferCounts.put(ho, bebas);
                }

            }

        }
    }

    /**
     * Prints a snapshot of the average buffer occupancy
     *
     * @param hosts The list of hosts in the simulation
     */
    @Override
    public void done() {
//        write("Nodes\tPersen Tiap Jam")
        for (Map.Entry<DTNHost, List<Double>> entry : bufferCounts.entrySet()) {
            DTNHost key = entry.getKey();
            List value = entry.getValue();
            write(key+"\t"+value);

        }
        super.done();
    }
}
