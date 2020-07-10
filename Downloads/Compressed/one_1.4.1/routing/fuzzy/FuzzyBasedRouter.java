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
public class FuzzyBasedRouter implements RoutingDecisionEngine, BufferDetectionEngine {

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
    private int interval = 300;
    public DTNHost thisBuffer;
    private FIS fclSimilarity;
    private FIS fclResource;
    private FIS fclFinal;
//    private Map<DTNHost, Double> rata2;
    private LinkedList<Double> sampelBuffer;        
    private LinkedList<Double> sampelVariansi;        
    private Double rata2buffer;
    private Double variansibuffer;
    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;
    public static final String INTERVAL_UPDATE = "intervalUpdate";
    protected Map<DTNHost, List<Double>> bufferMap;

    public FuzzyBasedRouter(Settings s) {
//         Settings settings = getSettings();
        String fclStringSimilarity = s.getSetting(FCL_NAMES_Similarity);
        String fclStringResource = s.getSetting(FCL_NAMES_Resource);
        String fclString = s.getSetting(FCL_NAMES_Final);
        fclSimilarity = FIS.load(fclStringSimilarity);
        fclResource = FIS.load(fclStringResource);
        fclFinal = FIS.load(fclString);
        if (s.contains(INTERVAL_UPDATE)) {
            interval = s.getInt(INTERVAL_UPDATE);
        } else {
            interval = 86400;
            /* not found; use default */
        }

    }

    public FuzzyBasedRouter(FuzzyBasedRouter t) {
        this.fclSimilarity = t.fclSimilarity;
        this.fclResource = t.fclResource;
        this.fclFinal = t.fclFinal;
        this.rata2buffer = 0.0;
        this.variansibuffer = 0.0;
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();       
        sampelBuffer = new LinkedList<>();
        sampelVariansi = new LinkedList<>();
//        varianceMap = new HashMap<DTNHost, List<Double>>();
        bufferMap = new HashMap<DTNHost, List<Double>>();

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
        List<Double> Buffhistory;
        List<Duration> history;

        if (!connHistory.containsKey(peer)) {
            history = new LinkedList<Duration>();
        } else {
            history = connHistory.get(peer);
        }

//         add this connection to the list
        if (currentTime - getLastDisconnect > 0) {
            history.add(new Duration(getLastDisconnect, currentTime));
        }
//        Semakin Tinggi Semakin Penuh

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
        Double me = this.rata2buffer;
        Double peer = de.rata2buffer;
        List<Double> buffer;

        if (!bufferMap.containsKey(peer)) {
            buffer = new LinkedList<Double>();
        } else {
            buffer = bufferMap.get(peer);
        }
        buffer.add(me);
        bufferMap.put(otherHost, buffer);
        return me > peer;
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

    private double DefuzzificationBuffer() {
        if (rata2buffer.equals(null) && variansibuffer.equals(null)) {
            return 0.0;
        }
        double averageBufferValue = this.rata2buffer;
        double varianceValue = this.variansibuffer;
//        System.out.println(rata2buffer);
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
        double buffer = DefuzzificationBuffer();
        FunctionBlock functionBlock = fclFinal.getFunctionBlock(null);

        functionBlock.setVariable(FUZZYSIMILARITY, similarity);
        functionBlock.setVariable(FUZZYBUFFER, buffer);
        functionBlock.evaluate();

        Variable tou = functionBlock.getVariable(TRANSFER_OF_UTILITY);

        return tou.getValue();
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

    public void getNormalizedVarianceBufferOfNodes() {
        double k = sampelVariansi.size();
        double N = 0;
        double sigmf = 0;
        Iterator<Double> iterator = sampelVariansi.iterator();
        while (iterator.hasNext()) {
            Double buffer = iterator.next();
            double timeDuration = buffer;
            N += timeDuration;
            sigmf += Math.pow(timeDuration, 2);
        }
        variansibuffer = (k * (Math.pow(N, 2) - sigmf)) / (Math.pow(N, 2) * (k - 1));
//        if (d.isNaN()) {
//            d = 0.0;
//        }

    }

    public void getAverageBuffer() {

        double rata2 = 0;
        for (Double sampel : sampelBuffer) {
            rata2 += sampel;
        }
//        rata2buffer.add(rata2 / sampelBuffer.size());
        rata2buffer = rata2 / sampelBuffer.size();

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
        double variansi = getVarianceOfNodes(nodes);

        Double c = Math.exp(-(Math.pow(rataShortestSeparation, 2) / (2 * variansi)));
//        System.out.println(c);
        if (c.isNaN()) {
            c = 0.0;
        }
        return c;
    }

    public void getVarianceBufferOfNodes() {
        // semakin tinggi semakin jelek karena tersebar
        List<Double> list = sampelBuffer;
        Iterator<Double> buffer = list.iterator();
        double temp = 0;
        double mean = rata2buffer;
        while (buffer.hasNext()) {
            Double d = buffer.next();
            temp += Math.pow(d - mean, 2);
        }
        variansibuffer = temp / list.size();
//        return temp / list.size();
    }

    public double getVarianceOfNodes(DTNHost nodes) {
        List<Duration> list = getListDuration(nodes);
        Iterator<Duration> duration = list.iterator();
        double temp = 0;
        double mean = getAverageShortestSeparationOfNodes(nodes);
        while (duration.hasNext()) {
            Duration d = duration.next();
            temp += Math.pow((d.end - d.start) - mean, 2);
        }
        return temp / list.size();
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

    @Override
    public Map<DTNHost, List<Double>> getBufferMap() {
        return bufferMap;
    }
//    @Override
//    public Map<DTNHost, List<Double>> getVarianceMap() {
//        return varianceMap;
//    }

    @Override
    public void update(DTNHost thishost) {
        double simTime = SimClock.getTime();
        if (simTime - lastRecord >= interval) {
            sampelBuffer.add(thishost.getBufferOccupancy());
//            sampelVariansi.add(thishost.getBufferOccupancy());
            
            if (sampelBuffer.size() == 5) {
                getAverageBuffer();
//                getVarianceBufferOfNodes();
                sampelBuffer.remove(0);
            }

        }
        this.lastRecord = simTime - simTime % interval;
    }
}
