/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.fuzzy;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.Duration;
import routing.DecisionEngineRouter;
import routing.community.BufferDetectionEngine;
import routing.community.VarianceDetectionEngine;

/**
 *
 * @author Afra Rian Yudianto, Sanata Dharma University
 */
public class FuzzyBasedRouter implements RoutingDecisionEngine {

    public static final String FCL_NAMES_Similarity = "fclSimilarity";
    public static final String FCL_NAMES_Resource = "fclResource";
    public static final String FCL_NAMES_Final = "fclFinal";
    public static final String CLOSENESS = "closeness";
    public static final String VARIANCE = "variance";
    public static final String AVERAGEBUFFER = "averageBuffer";
    public static final String VARIANCEBUFFER = "varianceBuffer";
    public static final String FUZZYBUFFER = "fuzzyBuffer";
    public static final String FUZZYSIMILARITY = "fuzzySimilarity";
    public static final String TRANSFER_OF_UTILITY = "hasil";
    private double lastRecord = Double.MIN_VALUE;
    private int interval;
    public DTNHost thisBuffer;
    private FIS fclSimilarity;
    private FIS fclResource;
    private FIS fclFinal;
//    private Map<DTNHost, Double> rata2;
    private Double rata2;
    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;
    protected Map<DTNHost, List<Integer>> connBuffHistory;
    public static final String INTERVAL_UPDATE = "intervalUpdate";

    public FuzzyBasedRouter(Settings s) {
        String fclStringSimilarity = s.getSetting(FCL_NAMES_Similarity);
        String fclStringResource = s.getSetting(FCL_NAMES_Resource);
        String fclString = s.getSetting(FCL_NAMES_Final);
        fclSimilarity = FIS.load(fclStringSimilarity);
        fclResource = FIS.load(fclStringResource);
        fclFinal = FIS.load(fclString);
        if (s.contains(INTERVAL_UPDATE)) {
            interval = s.getInt(INTERVAL_UPDATE);
        } else {
            interval = 3600;
            /* not found; use default */
        }
    }

    public FuzzyBasedRouter(FuzzyBasedRouter t) {
        this.fclSimilarity = t.fclSimilarity;
        this.fclResource = t.fclResource;
        this.fclFinal = t.fclFinal;
        this.rata2 = 0.0;
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
        connBuffHistory = new HashMap<DTNHost, List<Integer>>();
//        varianceMap = new HashMap<DTNHost, List<Double>>();
//        bufferMap = new HashMap<DTNHost, List<Double>>();

    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        // Find or create the connection history list

        thisBuffer = thisHost;
        double getLastDisconnect = 0;
        if (startTimestamps.containsKey(peer)) {
            getLastDisconnect = startTimestamps.get(peer);
        }
        double currentTime = SimClock.getTime();

        List<Duration> history;
        List<Integer> Buffhistory;
        if (!connHistory.containsKey(peer)) {
            history = new LinkedList<Duration>();
        } else {
            history = connHistory.get(peer);
        }
        if (!connBuffHistory.containsKey(peer)) {
            Buffhistory = new LinkedList<Integer>();
        } else {
            Buffhistory = connBuffHistory.get(peer);
        }

//         add this connection to the list
        if (currentTime - getLastDisconnect > 0) {
            history.add(new Duration(getLastDisconnect, currentTime));
        }
        Buffhistory.add(thisHost.getRouter().getFreeBufferSize());
        connBuffHistory.put(peer, Buffhistory);
        connHistory.put(peer, history);
        this.startTimestamps.remove(peer);

    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        this.startTimestamps.put(peer, SimClock.getTime());
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
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

        return true;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        if (m.getTo() == otherHost) {

            return true;
        }
        DTNHost dest = m.getTo();
        FuzzyBasedRouter de = getOtherDecisionEngine(otherHost);
        Double me = this.DefuzzificationFinal(dest, otherHost);
        Double peer = de.DefuzzificationFinal(dest, thisBuffer);
//        Double me = this.DefuzzificationBuffer(otherHost);
//        Double peer = de.DefuzzificationBuffer(thisBuffer);
//        boolean dcsn = me<peer;
//        System.out.println("Me = " + me + " Peer = " + peer);
//        List<Double> history;
//        if (!bufferMap.containsKey(otherHost)) {
//            history = new LinkedList<Double>();
//        } else {
//            history = bufferMap.get(otherHost);
//        }
////        System.out.println(dest.getRouter().getBufferSize());
//        history.add(me);
//        bufferMap.put(otherHost, history);
        return me < peer;
    }

    private double DefuzzificationSimilarity(DTNHost dest) {
        double closenessValue = getClosenessOfNodes(dest);
        double varianceValue = getNormalizedVarianceOfNodes(dest);
        FunctionBlock functionBlock = fclSimilarity.getFunctionBlock(null);

        functionBlock.setVariable(CLOSENESS, closenessValue);
        functionBlock.setVariable(VARIANCE, varianceValue);
        functionBlock.evaluate();

        Variable tou = functionBlock.getVariable(TRANSFER_OF_UTILITY);

        return tou.getValue();
    }

    private double DefuzzificationBuffer(DTNHost nodes) {
        double averageBufferValue = getAverageBuffer1(nodes);
        double varianceValue = getNormalizedVarianceBufferOfNodes(nodes);
        FunctionBlock functionBlock = fclResource.getFunctionBlock(null);
//        System.out.println(averageBufferValue+"\t"+varianceValue);
        functionBlock.setVariable(AVERAGEBUFFER, averageBufferValue);
        functionBlock.setVariable(VARIANCEBUFFER, varianceValue);
        functionBlock.evaluate();

        Variable tou = functionBlock.getVariable(TRANSFER_OF_UTILITY);

        return tou.getValue();
    }

    private double DefuzzificationFinal(DTNHost dest, DTNHost nodes) {
        double similarity = DefuzzificationSimilarity(dest);
        double buffer = DefuzzificationBuffer(nodes);
        FunctionBlock functionBlock = fclFinal.getFunctionBlock(null);

        functionBlock.setVariable(FUZZYSIMILARITY, similarity);
        functionBlock.setVariable(FUZZYBUFFER, buffer);
        functionBlock.evaluate();

        Variable tou = functionBlock.getVariable(TRANSFER_OF_UTILITY);

        return tou.getValue();
    }

    public double getVarianceBufferOfNodes(DTNHost nodes) {
        List<Integer> list = getListBuffer(nodes);
        Iterator<Integer> buffer = list.iterator();
        double temp = 0;
        double mean = getAverageShortestSeparationOfNodes(nodes);
        while (buffer.hasNext()) {
            Integer d = buffer.next();
            temp += Math.pow(d - mean, 2);
        }
        return temp / list.size();
    }

    public double getNormalizedVarianceOfNodes(DTNHost nodes) {
        double k = getListDuration(nodes).size();
        double N = 0;
        double sigmf = 0;
        Iterator<Duration> iterator = getListDuration(nodes).iterator();
        while (iterator.hasNext()) {
            Duration duration = iterator.next();
            double timeDuration = (duration.end - duration.start);
            N += timeDuration;
            sigmf += Math.pow(timeDuration, 2);
        }
        Double d = (k * (Math.pow(N, 2) - sigmf)) / (Math.pow(N, 2) * (k - 1));
//        if (d.isNaN()) {
//            d = 0.0;
//        }
        return d;
    }

//    public double getAverageBuffer2(DTNHost nodes) {
//        double tmp = 0;
//        double tmpSekarang = 0;
//        double rata = 0;
//        if (rata2.get(nodes) == 0) {
//            return nodes.getRouter().getFreeBufferSize();
//        }
//        tmp = rata2.get(nodes);
//        tmpSekarang = (double) nodes.getRouter().getFreeBufferSize();
//        rata = (tmp + (double) nodes.getRouter().getFreeBufferSize()) / 2;
//        rata2.put(nodes, rata);
//        return rata2.get(nodes);
//    }

    public double getAverageBuffer1(DTNHost nodes) {
        double size = getListBuffer(nodes).size();
        Iterator<Integer> iterator = getListBuffer(nodes).iterator();
//        Iterator<Integer> buffer = list.iterator();
        Integer hasil = 0;
        while (iterator.hasNext()) {
            Integer buffer = iterator.next();
            Integer bufferSize = buffer;
            hasil += bufferSize;
        }

        return hasil / size;
    }

    public double getNormalizedVarianceBufferOfNodes(DTNHost nodes) {
        double k = getListBuffer(nodes).size();

        double N = 0;
        double sigmf = 0;
        Iterator<Integer> iterator = getListBuffer(nodes).iterator();
        while (iterator.hasNext()) {
            Integer buffer = iterator.next();
            Integer bufferSize = buffer;
            N += bufferSize;
            sigmf += Math.pow(bufferSize, 2);
        }
        double pembilang = k * (Math.pow(N, 2) - sigmf);
//        System.out.println(k+"\t"+N);
        double penyebut = Math.pow(N, 2) * (k - 1);
        Double d = pembilang / penyebut;
//        System.out.println(pembilang+"/"+penyebut+"="+d);
//        if (d.isNaN()) {
//            d = 0.0;
//        }
        return d;
    }

    public List<Integer> getListBuffer(DTNHost nodes) {
        if (connBuffHistory.containsKey(nodes)) {
            return connBuffHistory.get(nodes);
        } else {
            List<Integer> d = new LinkedList<>();
            return d;
        }
    }

    public List<Duration> getListDuration(DTNHost nodes) {
        if (connHistory.containsKey(nodes)) {
            return connHistory.get(nodes);
        } else {
            List<Duration> d = new LinkedList<>();
            return d;
        }
    }

    private Double getClosenessOfNodes(DTNHost nodes) {
        double rataShortestSeparation = getAverageShortestSeparationOfNodes(nodes);
        double variansi = getVarianceBufferOfNodes(nodes);

        Double c = Math.exp(-(Math.pow(rataShortestSeparation, 2) / (2 * variansi)));
//        System.out.println(c);
        if (c.isNaN()) {
            c = 0.0;
        }
        return c;
    }

    public double getAverageShortestSeparationOfNodes(DTNHost nodes) {
        List<Duration> list = getListDuration(nodes);
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
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return false;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new FuzzyBasedRouter(this);
    }

    private FuzzyBasedRouter getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (FuzzyBasedRouter) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }
//FOR REPORT PURPOSE

//    @Override
//    public Map<DTNHost, List<Double>> getBufferMap() {
//        return bufferMap;
//    }
//    @Override
//    public Map<DTNHost, List<Double>> getVarianceMap() {
//        return varianceMap;
//    }
    @Override
    public void update(DTNHost thishost) {
        double simTime = SimClock.getTime();
        if (simTime - lastRecord >= interval) {
            List<Integer> buffer = getListBuffer(thishost);
            rata2 = getAverageBuffer1(thishost);
        }

    }
}
