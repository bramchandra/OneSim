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
public class VarianceTiapNodeYangDitemuiReport extends Report {

    public static final String NODE_ID = "ToNodeID";
    private int nodeAddress;
    private Map<DTNHost, List<Double>> bufferData;
    private Map<DTNHost, List<Double>> nodeComm;
    private Map<DTNHost, List<Double>> avgBuffer;
    private Double max;
    private Double min;

    public VarianceTiapNodeYangDitemuiReport() {
        super();
        Settings s = getSettings();
        if (s.contains(NODE_ID)) {
            nodeAddress = s.getInt(NODE_ID);
        } else {
            nodeAddress = 0;
        }
        bufferData = new HashMap<>();
        avgBuffer = new HashMap<>();
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
            nodeComm.put(host, cd.getVariance());
//            Map<DTNHost, List<Double>> temp = cd.getVariance();
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
//        for (DTNHost node : nodes) {
//            for (int i = 0; i < nodeComm.size(); i++) {
//                List<Double> avg;
//                if (!nodeComm.get(i).containsKey(node)) {
//                    avg = new LinkedList<>();
//                } else {
//                    avg = nodeComm.get(i).get(node);
//                }
//                avg.add(avgBufferCalc(nodeComm.get(i).get(node)));
//                avgBuffer.put(node, avg);
//            }
//        }
//        for (int i = 0; i < nodeComm.size(); i++) {
//            for (DTNHost node : nodes) {
//                if (nodeComm.get(i).containsKey(node)) {
//                    System.out.println(node);
////                    double avg = avgBufferCalc(nodeComm.get(i).get(node));
//                    
//                }
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

        for (Map.Entry<DTNHost, List<Double>> entry : nodeComm.entrySet()) {
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

//    private void findMaxMin(Map<DTNHost, List<Integer>> data) {
//        ArrayList<Integer> allValues = new ArrayList();
//        for (Map.Entry<DTNHost, List<Integer>> entry : data.entrySet()) {
//            DTNHost key = entry.getKey();
//            List<Integer> value = entry.getValue();
//            for (int i = 0; i < value.size(); i++) {
//                allValues.add(value.get(i));
//            }
//
//        }
//        for (int i = 0; i < allValues.size(); i++) {
//            min = allValues.get(0);
//            max = allValues.get(0);
//            if (allValues.get(i) < min) {
//                min = allValues.get(i);
//            }
//            if (allValues.get(i) > max) {
//                max = allValues.get(i);
//            }
//        }
//    }
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
