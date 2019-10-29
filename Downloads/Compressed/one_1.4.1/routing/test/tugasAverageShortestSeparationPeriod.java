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
public class tugasAverageShortestSeparationPeriod implements RoutingDecisionEngine {

    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;
    protected Map<DTNHost, List<Double>> durasi;

    double encounterPeer;
    double encounterThis;

    public tugasAverageShortestSeparationPeriod(Settings s) {

    }

    public tugasAverageShortestSeparationPeriod(tugasAverageShortestSeparationPeriod t) {
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {

    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {

        double time = startTimestamps.get(peer);
        double etime = SimClock.getTime();

        // Find or create the connection history list
        List<Duration> history;
        if (!connHistory.containsKey(peer)) {
            history = new LinkedList<Duration>();
            connHistory.put(peer, history);

        } else {
//            history = connHistory.get(peer);
//            history.add(new Duration(time, etime));
            connHistory.get(peer).add(new Duration(time, etime));
        }

        // add this connection to the list
//        if (etime - time > 0) {
//            history.add(new Duration(time, etime));
//            
//        }
//        startTimestamps.remove(peer);
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        tugasAverageShortestSeparationPeriod de = this.getOtherDecisionEngine(peer);

        this.startTimestamps.put(peer, SimClock.getTime());
        de.startTimestamps.put(myHost, SimClock.getTime());

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
        tugasAverageShortestSeparationPeriod de = getOtherDecisionEngine(otherHost);
        if (de.connHistory.containsKey(dest)) {
            double hasil = 0;
            double closeness = 0;
//            if (de.connHistory.get(dest).size() > 1) {
            for (int i = 0; i < de.connHistory.get(dest).size(); i++) {
//                    System.out.println(de.connHistory.get(dest).size());
                hasil = hasil + (de.connHistory.get(dest).get(i).end) - (de.connHistory.get(dest).get(i).start);
//                }
            }
            
            encounterPeer = (SimClock.getTime() - hasil) / de.connHistory.get(dest).size();
        }
        if (this.connHistory.containsKey(dest)) {
            double hasil = 0;
//            if (this.connHistory.get(dest).size() > 1) {
            for (int i = 0; i < this.connHistory.get(dest).size(); i++) {
                hasil = hasil + (this.connHistory.get(dest).get(i).end) - (this.connHistory.get(dest).get(i).start);
            }
//            }
            encounterThis = (SimClock.getTime() - hasil) / this.connHistory.get(dest).size();

        }
        return encounterPeer < encounterThis;
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
        return new tugasAverageShortestSeparationPeriod(this);
    }

    private tugasAverageShortestSeparationPeriod getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (tugasAverageShortestSeparationPeriod) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }
}
