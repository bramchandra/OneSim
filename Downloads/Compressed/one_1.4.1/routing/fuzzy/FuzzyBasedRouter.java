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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.Duration;
import routing.DecisionEngineRouter;
import routing.community.ResourceDetectionEngine;
import routing.community.SimilarityDetectionEngine;

/**
 *
 * @author Afra Rian Yudianto, Sanata Dharma University
 */
public class FuzzyBasedRouter implements RoutingDecisionEngine,ResourceDetectionEngine{

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
    private FIS fclSimilarity;
    private FIS fclResource;
    private FIS fclFinal;
    private LinkedList<Double> sampelBuffer;
    private LinkedList<Double> sampelVariansi;
    private Double rata2buffer;
    private Double variansibuffer;
    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;
    protected Map<DTNHost, ArrayList<Double>> ambildata;
    protected Map<DTNHost, ArrayList<Double>> ambildata2;
//    protected ArrayList<Double> variance;
    public static final String INTERVAL_UPDATE = "intervalUpdate";

    public FuzzyBasedRouter(Settings s) {
//         Settings settings = getSettings();
        String fclStringSimilarity = s.getSetting(FCL_NAMES_Similarity);
        String fclStringResource = s.getSetting(FCL_NAMES_Resource);
        String fclString = s.getSetting(FCL_NAMES_Final);
        fclSimilarity = FIS.load(fclStringSimilarity);
        fclResource = FIS.load(fclStringResource);
        fclFinal = FIS.load(fclString);
//        if (s.contains(INTERVAL_UPDATE)) {
//            interval = s.getInt(INTERVAL_UPDATE);
//        } else {
//            interval = 86400;
//            /* not found; use default */
//        }

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
        ambildata = new HashMap<>();
        ambildata2 = new HashMap<>();
//        variance = new ArrayList<>();
//        varianceMap = new HashMap<DTNHost, List<Double>>();

    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        // Find or create the connection history list

        double getLastDisconnect = 0;
        if (startTimestamps.containsKey(peer)) {
            getLastDisconnect = startTimestamps.get(peer);
        }
        double currentTime = SimClock.getTime();

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
        Double me = this.variansibuffer;
        Double peer = de.variansibuffer;
//        System.out.println(me);
        ArrayList<Double> data;
        if (!ambildata.containsKey(otherHost)) {
            data = new ArrayList<Double>();
        } else {
            data = ambildata.get(otherHost);
        }
        data.add(me);
        ambildata.put(otherHost, data);
//        ArrayList<Double> data2;
//        if (!ambildata2.containsKey(otherHost)) {
//            data2 = new ArrayList<Double>();
//        } else {
//            data2 = ambildata2.get(otherHost);
//        }
//        data2.add(this.variansibuffer);
//        ambildata2.put(otherHost, data2);
//        variance.add(this.getNormalizedVarianceOfNodes(dest));
        return me>peer;
    }

    private double DefuzzificationSimilarity(DTNHost dest) {
        double closenessValue = getClosenessOfNodes(dest);
        double varianceValue = getNormalizedVarianceOfNodes(dest);
        FunctionBlock functionBlock = fclSimilarity.getFunctionBlock("haggle3Infocom5ku");
//        FunctionBlock functionBlock = fclSimilarity.getFunctionBlock("realityku");
        functionBlock.setVariable(CLOSENESS, closenessValue);
        functionBlock.setVariable(VARIANCE, varianceValue);
        functionBlock.evaluate();

        Variable tou = functionBlock.getVariable(TRANSFER_OF_UTILITY);

        return tou.getValue();
    }

    private double DefuzzificationBuffer() {

        double averageBufferValue = rata2buffer;
        double varianceValue = variansibuffer;
//        System.out.println(rata2buffer);
//        FunctionBlock functionBlock = fclResource.getFunctionBlock("reality");
        FunctionBlock functionBlock = fclResource.getFunctionBlock("haggle3Infocom5");
//        System.out.println(averageBufferValue+"\t"+varianceValue);
        functionBlock.setVariable(AVERAGEBUFFER, averageBufferValue);
        functionBlock.setVariable(VARIANCEBUFFER, varianceValue);
        functionBlock.evaluate();

        Variable tou = functionBlock.getVariable(TRANSFER_OF_UTILITY);

        return tou.getValue();
    }

    private double DefuzzificationFinal(DTNHost dest) {
        double similarity = DefuzzificationSimilarity(dest);
        double buffer = DefuzzificationBuffer();
//        FunctionBlock functionBlock = fclFinal.getFunctionBlock("reality");
        FunctionBlock functionBlock = fclFinal.getFunctionBlock("haggle3Infocom5");

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
//        System.out.println(d);
        return d;
    }

    public double getNormalizedVarianceBufferOfNodes(List<Double> list) {
        double k = list.size();
        double N = 0;
        double sigmf = 0;
        Iterator<Double> iterator = list.iterator();
        while (iterator.hasNext()) {
            Double buffer = iterator.next();
            double timeDuration = buffer;
            N += timeDuration;
            sigmf += Math.pow(timeDuration, 2);
        }
        Double hasil = (k * (Math.pow(N, 2) - sigmf)) / (Math.pow(N, 2) * (k - 1));

        return hasil;
    }

    public double getAverageBuffer(List<Double> list) {

        double rata2 = 0;
        for (Double sampel : list) {
            rata2 += sampel;
        }
//        rata2buffer.add(rata2 / sampelBuffer.size());
        double hasil = rata2 / list.size();

        return hasil;
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

        return c;
    }

    public double getVarianceBufferOfNodes(List<Double> list) {
        // semakin tinggi data semakin tersebar
        List<Double> list2 = list;
        Iterator<Double> buffer = list.iterator();
        double temp = 0;
        double mean = getAverageBuffer(list);
        while (buffer.hasNext()) {
            Double d = buffer.next();
            temp += Math.pow(d - mean, 2);
        }
//        variansibuffer = temp / list.size();

        return temp / list.size();
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

    @Override
    public void update(DTNHost thishost) {
        double simTime = SimClock.getTime();
        if (simTime - lastRecord >= interval) {
            sampelBuffer.add(thishost.getBufferOccupancy());
            variansibuffer = getVarianceBufferOfNodes(sampelBuffer);
            if (sampelBuffer.size() > 4) {
                List temp = sampelBuffer.subList(sampelBuffer.size() - 5, sampelBuffer.size() - 1);
                rata2buffer = getAverageBuffer(temp);
            }

        }
        this.lastRecord = simTime - simTime % interval;
    }

    //FOR REPORT PURPOSE
    @Override
    public Map<DTNHost,ArrayList<Double>> getBuffer() {
        return ambildata;
    }

    @Override
    public Map<DTNHost,ArrayList<Double>> getVariansiBuffer() {
        return ambildata;
    }
//    @Override
//    public Map<DTNHost,ArrayList<Double>> getCloseness() {
//        return ambildata;
//    }
////
//    @Override
//    public Map<DTNHost,ArrayList<Double>> getVariance() {
//        return ambildata2;
//    }

}
