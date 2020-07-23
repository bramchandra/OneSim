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
    private Map<DTNHost, ArrayList<Double>> nodeComm;
    private Map<DTNHost, List<Double>> avgBuffer;
    private List<Map<DTNHost, ArrayList<Double>>> data;
    private List<Map<DTNHost, Double>> datarata;

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
        data = new LinkedList<>();
        datarata = new LinkedList<>();
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
            Map<DTNHost, ArrayList<Double>> temp = cd.getVariance();
            data.add(temp);

        }
        for (int i = 0; i < data.size(); i++) {
            for (DTNHost node : nodes) {
                if (data.get(i).containsKey(node)) {
                    Double Buffer = avgBufferCalc(data.get(i).get(node));
                    Map<DTNHost, Double> temp = new HashMap<>();
                    temp.put(node, Buffer);
                    datarata.add(temp);
                }
            }

        }


//        for (Map<DTNHost, ArrayList<Double>> map : data) {
//            for (Map.Entry<DTNHost, ArrayList<Double>> entry : map.entrySet()) {
//                DTNHost key = entry.getKey();
//                ArrayList<Double> val = entry.getValue();
//                for (int i = 0; i < val.size(); i++) {
//                    write(val.get(i)+"");                    
//                    
//                }
//                
//            }
//        }
        for (Map<DTNHost, Double> map : datarata) {
            for (Map.Entry<DTNHost, Double> entry : map.entrySet()) {
                DTNHost key = entry.getKey();
                Double val = entry.getValue();
                write(val+"");
                
            }
        }

//        write("Average Buffer  = " + avgValues);
        super.done();
    }

    private double avgBufferCalc(List<Double> bufferList) {
        Iterator<Double> i = bufferList.iterator();
        double jumlah = 0;
        while (i.hasNext()) {
            Double d = i.next();
            if(d.isNaN()){
                continue;
            }
            jumlah += d;
        }

        double avgDuration = jumlah / bufferList.size();
        return avgDuration;
    }
}
