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
    private Map<DTNHost, Map<DTNHost, Double>> closenessCounts = new HashMap<DTNHost, Map<DTNHost, Double>>();
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
                System.out.println(ho.getAddress() +" "+cd.getCloseness());
                closenessCounts.put(ho, cd.getCloseness());

            }
            write("Closeness per time : " +lastRecord);
            for (Map.Entry<DTNHost, Map<DTNHost, Double>> entry : closenessCounts.entrySet()) {
                DTNHost key = entry.getKey();
                write("Closeness to Node Id : " +key.getAddress());
                for (Map.Entry<DTNHost , Double> entry1 : entry.getValue().entrySet()) {
                    DTNHost key1 = entry1.getKey();
                    double value1 = entry1.getValue();
                    write(key1+" "+value1);
                }
//                String value = entry.getValue().toString();
                
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
//        write("Nodes\tPersen Tiap Jam")
//        List<DTNHost> host = SimScenario.getInstance().getHosts();
//        for (DTNHost node : host) {
//            
//            for (Map.Entry<DTNHost, Double> entry : closenessCounts.entrySet()) {
//                DTNHost key = entry.getKey();
//                Double value = entry.getValue();
//                write(key+" "+' '+value);
//            }
//        }
        super.done();
    }

}
