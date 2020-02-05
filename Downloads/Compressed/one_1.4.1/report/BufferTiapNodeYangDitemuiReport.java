/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.Settings;
import core.SimScenario;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.BufferDetectionEngine;

/**
 * Provides the inter-contact duration data for making probability density
 * function
 *
 * @author Gregorius Bima, Sanata Dharma University
 */
public class BufferTiapNodeYangDitemuiReport extends Report {

    public static final String NODE_ID = "ToNodeID";
    private int nodeAddress;
    private Map<DTNHost, List<Integer>> bufferData;
//    private Map<DTNHost, Double> avgBuffer;

    public BufferTiapNodeYangDitemuiReport() {
        super();
        Settings s = getSettings();
        if (s.contains(NODE_ID)) {
            nodeAddress = s.getInt(NODE_ID);
        } else {
            nodeAddress = 0;
        }
        bufferData = new HashMap<>();
//        avgBuffer = new HashMap<>();
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
            BufferDetectionEngine cd = (BufferDetectionEngine) de;
            Map<DTNHost, List<Integer>> nodeComm = cd.getBufferMap();

            if (host.getAddress() == nodeAddress) {
                bufferData = nodeComm;
            }

        }

//        for (DTNHost node : nodes) {
//            if (bufferData.containsKey(node)) {
//                double avg = avgBufferCalc(bufferData.get(node));
//                avgBuffer.put(node, avg);
//            }
//        }
//        double values = 0;
//        for (Double avgEncounter : avgBuffer.values()) {
//            values += avgEncounter;
//        }
//
//        double avgValues = values / avgBuffer.size();
//
//        write("Buffer Time To " + nodeAddress);
//        write("Nodes" + "\t" + "Buffer");
        for (Map.Entry<DTNHost, List<Integer>> entry : bufferData.entrySet()) {
            DTNHost key = entry.getKey();
            List<Integer> value = entry.getValue();
            String print = "";
            for (Integer double1 : value) {
                print = print + "\n" + double1;
            }
            write(print);

        }
//        write("Average Buffer  = " + avgValues);
        super.done();
    }

//    private double avgBufferCalc(List<Double> bufferList) {
//        Iterator<Double> i = bufferList.iterator();
//        double jumlah = 0;
//        while (i.hasNext()) {
//            Double d = i.next();
//            jumlah += d;
//        }
//
//        double avgDuration = jumlah / bufferList.size();
//        return avgDuration;
//    }
}
