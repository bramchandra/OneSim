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
import net.sourceforge.jFuzzyLogic.FIS;
import routing.community.ClosenessDecisionEngine;
//import routing.community.VarianceDecisionEngine;


/**
 *
 * @author jarkom
 */
public class FuzzyBasedRouting implements RoutingDecisionEngine,ClosenessDecisionEngine {

    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, Double> ratarata;
    protected Map<DTNHost, List<Duration>> connHistory;
    protected Map<DTNHost, Double> closeness;

    double encounterPeer;
    double encounterThis;

    public FuzzyBasedRouting(Settings s) {

    }

    public FuzzyBasedRouting(FuzzyBasedRouting t) {
        startTimestamps = new HashMap<DTNHost, Double>();
        closeness = new HashMap<DTNHost,  Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
    }
//    private FIS loadFcl(String loc){
//        String fileName = "fcl/tipper.fcl";
//        FIS fis = FIS.load(fileName,true);
//
//        // Error while loading?
//        if( fis == null ) { 
//            System.err.println("Can't load file: '" + fileName + "'");
//            return null;
//        }
//        return fis;
//    }

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
        FuzzyBasedRouting de = this.getOtherDecisionEngine(peer);
        startTimestamps.put(peer, SimClock.getTime());
    }
    

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        
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
//    private double defuzzyfication(DTNHost nodes){
//        double closeness = getCloseness(nodes);
//        double variance = getVariance(nodes);
//        FIS fcl = loadFcl("fcl/FuzzyControlLanguage.fcl");     
//        fcl.setVariable("closeness", closeness);
//        fcl.setVariable("variance", variance);
//        fcl.evaluate();
//        
//        
//    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        
       
        return m.getTo() != thisHost;
    }
//    public DTNHost getPeer(DTNHost hosts){
//        
//    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        if (m.getTo() == otherHost) {
            return true;
        }
        
         
        DTNHost dest = m.getTo();
        FuzzyBasedRouting de = getOtherDecisionEngine(otherHost);
        encounterThis=this.getClosenessOfNodes(dest);
        encounterPeer=de.getClosenessOfNodes(dest);               
        closeness.put(dest, this.getClosenessOfNodes(dest));
        
//        System.out.println(closeness);
//        fcl.setVariable("closeness", this.getCloseness(dest));
//        fcl.setVariable("closeness", de.getCloseness(dest));
//        fcl.evaluate();
//        System.out.println("Peer"+encounterPeer);
//        System.out.println("This"+encounterThis);
        return encounterPeer > encounterThis;
    }


    public double getVarianceOfNodes(DTNHost nodes) {
        List<Duration> list = getList(nodes);
        Iterator<Duration> duration = list.iterator();
        double temp = 0;
        double mean = getAverageShortestSeparationOfNodes(nodes);
        while (duration.hasNext()) {
            Duration d = duration.next();
            temp += ((d.end-d.start) - mean) * ((d.end-d.start) - mean);
        }

        return temp / list.size();
    }

    public List<Duration> getList(DTNHost nodes) {
        if (connHistory.containsKey(nodes)) {
            return connHistory.get(nodes);
        } else {
            List<Duration> d = new LinkedList<>();
            return d;
            
        }
    }

    private double getClosenessOfNodes(DTNHost nodes) {
        double rataShortestSeparation = getAverageShortestSeparationOfNodes(nodes);
        double variansi = getVarianceOfNodes(nodes);

        return Math.pow(2.71828, -Math.pow(rataShortestSeparation, 2) / (2 * variansi));
    }

    public double getAverageShortestSeparationOfNodes(DTNHost nodes) {
        List<Duration> list = getList(nodes);
        Iterator<Duration> duration = list.iterator();
        double hasil = 0;
        while (duration.hasNext()) {
            Duration d = duration.next();
            hasil += (d.end - d.start);
        }
        return hasil / list.size();
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
        return new FuzzyBasedRouting(this);
    }

    private FuzzyBasedRouting getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (FuzzyBasedRouting) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public Map<DTNHost, Double> getCloseness() {
        return closeness;
    }




}