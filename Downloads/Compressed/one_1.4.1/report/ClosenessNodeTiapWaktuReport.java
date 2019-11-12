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
import routing.community.ClosenessDecisionEngine;
import routing.community.VarianceDecisionEngine;

public class ClosenessNodeTiapWaktuReport extends Report implements UpdateListener {

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
//    private Map<DTNHost, List<Double>> closenessCounts = new HashMap<DTNHost, List<Double>>();
    private Map<DTNHost, List<Double>> perWaktu = new HashMap<DTNHost, List<Double>>();
    private int updateCounter = 0;  //new added
    private String print;  //new added

    public ClosenessNodeTiapWaktuReport() {
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
            Map<DTNHost, List<Double>> nodeComm = null;
            for (DTNHost ho : hosts) {
                MessageRouter r = ho.getRouter();
                if (!(r instanceof DecisionEngineRouter)) {
                    continue;
                }
                RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
                if (!(de instanceof ClosenessDecisionEngine)) {
                    continue;
                }
                ClosenessDecisionEngine cd = (ClosenessDecisionEngine) de;
                nodeComm = cd.getCloseness();

            }
//            write("TimePer" + lastRecord);
            for (Map.Entry<DTNHost, List<Double>> entry : nodeComm.entrySet()) {
                DTNHost key = entry.getKey();
                List<Double> value = entry.getValue();
                double temp = 0;
                for (Double double1 : value) {
                    temp += double1;
                }
                List<Double> hasil;
                if (!perWaktu.containsKey(key)) {
                    hasil = new LinkedList<Double>();

                } else {
                    hasil = perWaktu.get(key);

                }

                hasil.add(temp / value.size());
                perWaktu.put(key, hasil);
//                write(key + " " + temp / value.size());
            }

            this.lastRecord = simTime - simTime % interval;
        }
    }

    /**
     * Prints a snapshot of the average buffer occupancy
     *
     * @param hosts The list of hosts in the simulation
     */
    @Override
    public void done() {
        int time = interval;
//        write("" + time);
        for (Map.Entry<DTNHost, List<Double>> entry : perWaktu.entrySet()) {
            DTNHost key = entry.getKey();
            List<Double> value = entry.getValue();

            write(key + " " + value);

        }
//        time += interval;
        super.done();
    }

}
