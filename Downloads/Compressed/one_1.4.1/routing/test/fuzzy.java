/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.test;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

import routing.community.Duration;

/**
 *
 * @author jarkom
 */
public class fuzzy implements RoutingDecisionEngine {

    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, Double> ratarata;
    protected Map<DTNHost, List<Duration>> connHistory;
    protected Map<DTNHost, List<Double>> durasi;

    double encounterPeer;
    double encounterThis;

    public fuzzy(Settings s) {

    }

    public fuzzy(fuzzy t) {
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        // Find or create the connection history list
        double time = 0;
        if (startTimestamps.containsKey(peer)) {
            time = startTimestamps.get(peer);
        }

        double etime = SimClock.getTime();
        List<Duration> history;
        if (!connHistory.containsKey(peer)) {
            history = new LinkedList<Duration>();
//            connHistory.put(peer, history);

        } else {
            history = connHistory.get(peer);

        }

//         add this connection to the list
        if (etime - time > 0) {
            history.add(new Duration(time, etime));

        }
        connHistory.put(peer, history);
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        fuzzy de = this.getOtherDecisionEngine(peer);
        startTimestamps.put(peer, SimClock.getTime());
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
//        DTNHost myHost = con.getOtherNode(peer);
//        fuzzy de = this.getOtherDecisionEngine(peer);

    }

    @Override
    public boolean newMessage(Message m) {
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return m.getTo() != thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        if (m.getTo() == otherHost) {
            return true;
        }
        DTNHost dest = m.getTo();
        fuzzy de = getOtherDecisionEngine(otherHost);
        encounterThis=this.getVariance(getList(dest));
        encounterPeer=de.getVariance(getList(dest));
        
        System.out.println("Peer"+encounterPeer);
        System.out.println("This"+encounterThis);
        return encounterPeer > encounterThis;
    }


    private double getVariance(List<Duration> nodes) {
        Iterator<Duration> duration = nodes.iterator();
        double temp = 0;
        double mean = getAverageShortestSeparation(nodes);
        while (duration.hasNext()) {
            Duration d = duration.next();
            temp += ((d.end-d.start) - mean) * ((d.end-d.start) - mean);
        }

        return temp / nodes.size();
    }

    private List<Duration> getList(DTNHost nodes) {
        if (connHistory.containsKey(nodes)) {
            return connHistory.get(nodes);
        } else {
            List<Duration> d = new LinkedList<>();
            return d;
        }
    }

    private double getCloseness(DTNHost nodes) {
        double rataShortestSeparation = getAverageShortestSeparation(getList(nodes));
        double variansi = getVariance(getList(nodes));

        return Math.pow(2.71828, -Math.pow(rataShortestSeparation, 2) / (2 * variansi));
    }

    private double getAverageShortestSeparation(List<Duration> nodes) {
        Iterator<Duration> duration = nodes.iterator();
        double hasil = 0;
        while (duration.hasNext()) {
            Duration d = duration.next();
            hasil += (d.end - d.start);
        }
        return hasil / nodes.size();
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return m.getTo() == otherHost;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return true;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new fuzzy(this);
    }

    private fuzzy getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (fuzzy) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }
}
