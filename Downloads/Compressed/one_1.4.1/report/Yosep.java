/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;
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
import routing.community.VarianceDetectionEngine;

/**
 * Provides the inter-contact duration data for making probability density
 * function
 *
 * @author Gregorius Bima, Sanata Dharma University
 */
public class Yosep extends Report implements MessageListener {

    public static final String NODE_ID = "ToNodeID";
    private String pesan;
    private Map<DTNHost, List<Double>> varianceData;
    private Map<DTNHost, Double> avgVariance;
    private int nrofDelivered;
    private int nrofCreated;
    private Map<String, Double> creationTimes;
    private List<Double> latencies;

    public Yosep() {
        init();
        Settings s = getSettings();
        if (s.contains(NODE_ID)) {
            pesan = s.getSetting(NODE_ID);
        } else {
            pesan = "P";
        }

    }

    @Override
    public void init() {
        super.init();
        this.nrofCreated = 0;
        this.nrofDelivered = 0;
        latencies = new LinkedList<>();
        creationTimes = new HashMap<>();
    }

    public void done() {
        double deliveryProb = 0; // delivery probability
        double responseProb = 0; // request-response success probability
//        double overHead = Double.NaN;	// overhead ratio

        if (this.nrofCreated > 0) {
            deliveryProb = (1.0 * this.nrofDelivered) / this.nrofCreated;
        }
        String output = "Pesan" + pesan + "\n";
        output += "Delivery Probability = " + deliveryProb+"\n";
        output += "Latency = " + getAverage(latencies);
        write(output);
        super.done();
    }

  
    @Override
    public void newMessage(Message m) {
        if (String.valueOf(m.getId().charAt(0)).equals(pesan)) {
            this.creationTimes.put(m.getId(), getSimTime());
            this.nrofCreated++;
        }

    }

    @Override
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {

    }

    @Override
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {

    }

    @Override
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {

    }

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean finalTarget) {
        if (finalTarget) {
            if (String.valueOf(m.getId().charAt(0)).equals(pesan)) {
                this.nrofDelivered++;
                this.latencies.add(getSimTime() - this.creationTimes.get(m.getId()));
            }

        }

    }
}
