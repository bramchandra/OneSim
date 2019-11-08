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
    public static final int DEFAULT_BUFFER_REPORT_INTERVAL = 500;

    private double lastRecord = Double.MIN_VALUE;
    private int interval;

    private Map<DTNHost, List<Double>> closenessCounts = new HashMap<DTNHost, List<Double>>();
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

    public void updated(List<DTNHost> hosts) {

        if (isWarmup()) {
            return;
        }
        
        if (SimClock.getTime() - lastRecord >= interval) {
            lastRecord = SimClock.getTime();
            
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
                Map<DTNHost, Double> nodeComm = cd.getCloseness();

                Map<DTNHost, Double> temp = nodeComm;
                System.out.println("Node="+ho+"Closeness=" + temp.get(ho));      

                if (closenessCounts.containsKey(ho)) {
//                    System.out.println("BISA");
                    List bebas = closenessCounts.get(ho);
                    bebas.add(temp);
                    closenessCounts.put(ho, bebas);
                } else {
//                    System.out.println("BARU");
                    List<Double> bebas = new LinkedList();
                    closenessCounts.put(ho, bebas);
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
        for (Map.Entry<DTNHost, List<Double>> entry : closenessCounts.entrySet()) {
            DTNHost key = entry.getKey();
            List value = entry.getValue();
            write(key + "\t" + value);

        }
        super.done();
    }

}
