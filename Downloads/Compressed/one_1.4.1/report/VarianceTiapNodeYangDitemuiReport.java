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
import java.util.List;
import java.util.Map;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.VarianceDetectionEngine;

/**
 * Provides the inter-contact duration data for making probability density
 * function
 *
 * @author Gregorius Bima, Sanata Dharma University
 */
public class VarianceTiapNodeYangDitemuiReport extends Report {

    public static final String NODE_ID = "ToNodeID";
    private int nodeAddress;
    private Map<DTNHost, List<Double>> varianceData;
    private Map<DTNHost, Double> avgVariance;

    public VarianceTiapNodeYangDitemuiReport() {
        super();
        Settings s = getSettings();
        if (s.contains(NODE_ID)) {
            nodeAddress = s.getInt(NODE_ID);
        } else {
            nodeAddress = 0;
        }
        varianceData = new HashMap<>();
        avgVariance = new HashMap<>();
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
            VarianceDetectionEngine cd = (VarianceDetectionEngine) de;
            Map<DTNHost, List<Double>> nodeComm = cd.getVarianceMap();

            if (host.getAddress() == nodeAddress) {
                varianceData = nodeComm;

//                System.out.println(varianceData+"\n");
            }

        }

//        for (DTNHost node : nodes) {
//            if (varianceData.containsKey(node)) {
//                double avg = avgVarianceCalc(varianceData.get(node));
//                avgVariance.put(node, avg);
//            }
//        }
//        double values = 0;
//        for (Double avgEncounter : avgVariance.values()) {
//            values += avgEncounter;
//        }
//        double avgValues = values/avgVariance.size();
//        write("Variance Time To " +nodeAddress);
//        write("Nodes"+"\n"+"Closeness");
        for (Map.Entry<DTNHost, List<Double>> entry : varianceData.entrySet()) {

            DTNHost key = entry.getKey();
            List<Double> value = entry.getValue();
            String print = "";

            for (Double double1 : value) {

                print = print + "\n" + double1;
            }

            write(print);
//            write("\n" + value);
        }
//        write("Average Variance  = "+avgValues);
        super.done();
    }

    private double avgVarianceCalc(List<Double> varianceList) {
        Iterator<Double> i = varianceList.iterator();
        double jumlah = 0;
        while (i.hasNext()) {
            Double d = i.next();
            jumlah += d;
        }

        double avgDuration = jumlah / varianceList.size();
        return avgDuration;
    }
}
