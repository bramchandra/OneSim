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
import core.SimScenario;
import core.UpdateListener;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.VarianceDetectionEngine;

public class VarianceNodeTiapWaktuReport extends Report implements UpdateListener {

    /**
     * Record occupancy every nth second -setting id ({@value}). Defines the
     * interval how often (seconds) a new snapshot of buffer occupancy is taken
     * previous:5
     */
    public static final String BUFFER_REPORT_INTERVAL = "occupancyInterval";
    /**
     * Default value for the snapshot interval
     */
    public static final int DEFAULT_BUFFER_REPORT_INTERVAL = 10000;
    private double lastRecord = Double.MIN_VALUE;
    private int interval;
    private Map<DTNHost, List<Double>> variancePerWaktu = new HashMap<DTNHost, List<Double>>();
    private int updateCounter = 0;  //new added
    private String print;  //new added

    public VarianceNodeTiapWaktuReport() {
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

    @Override
    public void updated(List<DTNHost> hosts) {
        double simTime = getSimTime();
        if (isWarmup()) {
            return;
        }

        if (simTime - lastRecord >= interval) {
            Map<DTNHost, List<Double>> varianceMap = new HashMap<DTNHost, List<Double>>();
            for (DTNHost ho : hosts) {
                MessageRouter r = ho.getRouter();
                if (!(r instanceof DecisionEngineRouter)) {
                    continue;
                }
                RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
                if (!(de instanceof VarianceDetectionEngine)) {
                    continue;
                }
                VarianceDetectionEngine vd = (VarianceDetectionEngine) de;
                varianceMap = vd.getVarianceMap();
                if(ho.getAddress()==0){
                    
                }
             
            }
            for (Map.Entry<DTNHost, List<Double>> entry : varianceMap.entrySet()) {
                DTNHost key = entry.getKey();
                List<Double> value = entry.getValue();
                double temp = 0;
                for (Double double1 : value) {
                    temp += double1;
                }
                List<Double> hasil;
                if (!variancePerWaktu.containsKey(key)) {
                    hasil = new LinkedList<Double>();

                } else {
                    hasil = variancePerWaktu.get(key);

                }
                hasil.add(temp / value.size());
                variancePerWaktu.put(key, hasil);
            }
        }

        this.lastRecord = simTime - simTime % interval;
    }

    /**
     * Prints a snapshot of the average buffer occupancy
     *
     * @param hosts The list of hosts in the simulation
     */
    @Override
    public void done() {
        for (Map.Entry<DTNHost, List<Double>> entry : variancePerWaktu.entrySet()) {
            DTNHost key = entry.getKey();
            List<Double> value = entry.getValue();

            write(key + " " + value);

        }

        super.done();
    }

}
