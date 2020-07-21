/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.Settings;
import core.SimScenario;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.ResourceDetectionEngine;
import routing.community.SimilarityDetectionEngine;

/**
 * Provides the inter-contact duration data for making probability density
 * function
 *
 * @author Gregorius Bima, Sanata Dharma University
 */
public class ClosenessTiapNodeYangDitemuiReport extends Report {

    public static final String NODE_ID = "ToNodeID";
    private int nodeAddress;
//    private Map<DTNHost, List<Double>> bufferData;
    private Map<DTNHost, ArrayList<Double>> nodeComm;
//    private Map<DTNHost, List<Double>> avgBuffer;
    private Double max;
    private Double min;

    public ClosenessTiapNodeYangDitemuiReport() {
        super();
        Settings s = getSettings();
        
//        bufferData = new HashMap<>();
//        avgBuffer = new HashMap<>();
        nodeComm = new HashMap<>();
    }

    public void done() {
        List<DTNHost> nodes = SimScenario.getInstance().getHosts();

        for (DTNHost host : nodes) {
            MessageRouter router = host.getRouter();
            if (!(router instanceof DecisionEngineRouter)) {
                continue;
            }
            RoutingDecisionEngine de = ((DecisionEngineRouter) router).getDecisionEngine();
            if (!(de instanceof RoutingDecisionEngine)) {
                continue;
            }
            SimilarityDetectionEngine cd = (SimilarityDetectionEngine) de;
//            List<Double> history;
//            Map<DTNHost, List<Double>> temp = cd.getCloseness();
            nodeComm.put(host, cd.getCloseness());
//            for (Map.Entry<DTNHost, List<Double>> entry : temp.entrySet()) {
//                DTNHost key = entry.getKey();
//                List<Double> value = entry.getValue();
//                nodeComm.put(key, value);
//            }
//            if (host.getAddress() == nodeAddress) {
//                bufferData = nodeComm;
//                
//            }

        }


        for (Map.Entry<DTNHost, ArrayList<Double>> entry : nodeComm.entrySet()) {
            DTNHost key = entry.getKey();
            List<Double> value = entry.getValue();
            String print = "";
            for (Double double1 : value) {

                print = print + "\n" + double1;
            }
//            System.out.println(print);
            write(print);
//            write(value+"");

        }
//        write("Average Buffer  = " + avgValues);
        super.done();
    }

    private double avgBufferCalc(List<Double> bufferList) {
        Iterator<Double> i = bufferList.iterator();
        double jumlah = 0;
        while (i.hasNext()) {
            Double d = i.next();
            jumlah += d;
        }

        double avgDuration = jumlah / bufferList.size();
        return avgDuration;
    }
}
