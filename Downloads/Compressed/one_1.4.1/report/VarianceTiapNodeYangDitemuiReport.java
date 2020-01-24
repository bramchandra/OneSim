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
import routing.community.VarianceDetectionReport;

/**
 * Provides the inter-contact duration data 
 * for making probability density function
 * @author Gregorius Bima, Sanata Dharma University
 */
public class VarianceTiapNodeYangDitemuiReport extends Report{
    public static final String NODE_ID = "varianceToNodeID";
    private int nodeAddress;
    private Map<DTNHost, List<Double>> varianceData ;
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
            VarianceDetectionReport cd = (VarianceDetectionReport) de;
            Map<DTNHost, List<Double>> nodeComm = cd.getVarianceMap();
                   
            if (host.getAddress() == nodeAddress) {
                varianceData = nodeComm;
            }

        }
        
        for (DTNHost node : nodes) {
            if (varianceData.containsKey(node)) {
                double avg = avgVarianceCalc(varianceData.get(node));
                avgVariance.put(node, avg);
            }
        }
        double values = 0;
        for (Double avgEncounter : avgVariance.values()) {
            values += avgEncounter;
        }
        
        double avgValues = values/avgVariance.size();

        write("Variance Time To " +nodeAddress);
        write("Nodes"+"\t"+"Variance");
        for (Map.Entry<DTNHost, Double> entry : avgVariance.entrySet()) {
            DTNHost key = entry.getKey();
            Double value = entry.getValue();
            write(key+"\t"+ value );
        }
        write("Average Variance  = "+avgValues);
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
