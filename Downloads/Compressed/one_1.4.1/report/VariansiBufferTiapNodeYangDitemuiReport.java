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
import routing.community.ResourceDetectionEngine;

/**
 * Provides the inter-contact duration data for making probability density
 * function
 *
 * @author Gregorius Bima, Sanata Dharma University
 */
public class VariansiBufferTiapNodeYangDitemuiReport extends Report {

    public static final String NODE_ID = "ToNodeID";
    private int nodeAddress;
    private Map<DTNHost, List<Double>> bufferData;
    private Map<DTNHost, List<Double>> nodeComm;
    private Map<DTNHost, Double> avgBuffer;
    private Double max;
    private Double min;

    public VariansiBufferTiapNodeYangDitemuiReport() {
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
            ResourceDetectionEngine cd = (ResourceDetectionEngine) de;
            nodeComm.put(host, cd.getVariansiBuffer());
//            nodeComm.
//            if (host.getAddress() == nodeAddress) {
//                bufferData = nodeComm;
//                
//            }

        }

        
        for (Map.Entry<DTNHost, List<Double>> entry : nodeComm.entrySet()) {
            DTNHost key = entry.getKey();
            List<Double> value = entry.getValue();
            String print = "";
            for (Double double1 : value) {
             
                print = print + "\n" + double1;
            }
//            System.out.println(print);
            write(print);
//            write(value + "");

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
