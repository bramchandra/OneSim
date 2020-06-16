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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Khusus Skripsi Fuzzy
 */
public class AverageNumberOfHopsPerMessageReport extends Report implements MessageListener, ConnectionListener {

    public static final String TOTAL_CONTACT_INTERVAL = "perTotalContact";
    public static final int DEFAULT_CONTACT_COUNT = 100;
    private int interval;
    private int hops;
    private int totalContact;
    private int lastRecord;
    private List<Message> connHistory;
    private Map<Integer, Double> nrofHops;

    public AverageNumberOfHopsPerMessageReport() {
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
        nrofHops = new HashMap<>();
        connHistory = new ArrayList();
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
        if (!connHistory.contains(m)) {
            connHistory.add(m);
        }
    }

    @Override
    public void done() {
        String statsText = "Hops\tNrofDelivered\n";
        for (Map.Entry<Integer, Double> entry : nrofHops.entrySet()) {
            Integer key = entry.getKey();
            Double value = entry.getValue();
            statsText += key + "\t" + value + "\n";
        }
        write(statsText);
        super.done();
    }

    @Override
    public void hostsConnected(DTNHost host1, DTNHost host2) {
        totalContact++;
        if (totalContact - lastRecord >= interval) {
            lastRecord = totalContact;
            double temp = 0;
            for (int i = 0; i < connHistory.size(); i++) {
                temp += connHistory.get(i).getHopCount();
            }
            double hasil = temp / connHistory.size();
            nrofHops.put(lastRecord, hasil);
        }
    }

    @Override
    public void hostsDisconnected(DTNHost host1, DTNHost host2) {

    }
}
