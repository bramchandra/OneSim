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
            ratarata.put(peer, 0.0);
        } else {
//            history = connHistory.get(peer);
//            history.add(new Duration(time, etime));
            connHistory.get(peer).add(new Duration(time, etime));
            ratarata.put(peer, (ratarata.get(peer) + (etime - time)));
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
        fuzzy de = this.getOtherDecisionEngine(peer);

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
        fuzzy de = getOtherDecisionEngine(otherHost);
        if (de.connHistory.containsKey(dest)) {
            double hasil = 0;
            double closeness = 0;
            double temp = 0;
            double variansi = 0;
            
            //RATARATA SEPARATION

            //CARI VARIANSI
            double mean = (ratarata.get(dest) / de.connHistory.get(dest).size());
            for (int i = 0; i < de.connHistory.get(dest).size(); i++) {
                double data = (de.connHistory.get(dest).get(i).end) - de.connHistory.get(dest).get(i).start;
                temp += (data - mean) * (data - mean);
            }
            variansi = temp / de.connHistory.get(dest).size();
            //CARI VARIANSI

            //CARI CLOSENESS / NORMALISASI AVG
            double rataShortestSeparation = (SimClock.getTime() - mean) / de.connHistory.get(dest).size();
            closeness = Math.pow(2.71828, -Math.pow(rataShortestSeparation, 2) / 2 * variansi);
            //CARI CLOSENESS / NORMALISASI AVG

        }
        if (this.connHistory.containsKey(dest)) {
            double hasil = 0;
            double closeness = 0;
            double temp = 0;
            double variansi = 0;
            //CARI VARIANSI
            double mean = (ratarata.get(dest) / this.connHistory.get(dest).size());
            for (int i = 0; i < this.connHistory.get(dest).size(); i++) {
                double data = (this.connHistory.get(dest).get(i).end) - this.connHistory.get(dest).get(i).start;
                temp += (data - mean) * (data - mean);
            }
            variansi = temp / de.connHistory.get(dest).size();
            //CARI VARIANSI

            //CARI CLOSENESS / NORMALISASI AVG
            double rataShortestSeparation = (SimClock.getTime() - hasil) / this.connHistory.get(dest).size();
            closeness = Math.pow(2.71828, -Math.pow(rataShortestSeparation, 2) / 2 * variansi);
            //CARI CLOSENESS / NORMALISASI AVG

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
        return new fuzzy(this);
    }

    private fuzzy getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (fuzzy) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }
}
