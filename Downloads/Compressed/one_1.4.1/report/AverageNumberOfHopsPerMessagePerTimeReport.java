/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.ConnectionListener;
import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.Settings;
import core.SimClock;
import core.UpdateListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Khusus Skripsi Fuzzy
 */
public class AverageNumberOfHopsPerMessagePerTimeReport extends Report implements MessageListener, UpdateListener {

    public static final String TOTAL_CONTACT_INTERVAL = "perTotalContact";
    public static final int DEFAULT_CONTACT_COUNT = 3600;
    private int interval;
    private int hops;
    private int totalContact;
    private Double lastRecord;
//    private List<Message> connHistory;
    private Map<Message, Integer> nrofHops;
    private HashMap<Double, Double> nrofAverageHops;

    public AverageNumberOfHopsPerMessagePerTimeReport() {
        init();
        Settings s = getSettings();
        if (s.contains(TOTAL_CONTACT_INTERVAL)) {
            interval = s.getInt(TOTAL_CONTACT_INTERVAL);
        } else {
            interval = DEFAULT_CONTACT_COUNT;
        }
    }

    public void init() {
        super.init();
        this.interval = 0;
        this.lastRecord = 0.0;
        this.totalContact = 0;
        this.nrofHops = new HashMap<>();
        this.nrofAverageHops = new HashMap<>();
    }

    @Override
    public void newMessage(Message m) {

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
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
        if (firstDelivery) {
            nrofHops.put(m, m.getHopCount());
        }
    }

    @Override
    public void done() {
        String statsText = "Hops\tNrofDelivered\n";
        for (Map.Entry<Double, Double> entry : nrofAverageHops.entrySet()) {
            Double key = entry.getKey();
            Double value = entry.getValue();
            statsText += key + "\t" + value + "\n";
        }
        write(statsText);
        super.done();
    }

    @Override
    public void updated(List<DTNHost> hosts) {
        
        
        if (SimClock.getTime() - lastRecord >= interval) {
            double totalMsg = 0;
            double totalHopCounts = 0;
            for (Map.Entry<Message, Integer> entry : nrofHops.entrySet()) {
                totalMsg++;
                Integer value = entry.getValue();
                totalHopCounts+=value;                
            }
            double averagePerMsg = totalHopCounts/totalMsg;
            nrofAverageHops.put(SimClock.getTime(), averagePerMsg);
            lastRecord = SimClock.getTime();
        }
    }
}
