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
import java.util.Map;

/**
 *
 * @author Khusus Skripsi Fuzzy
 */
public class TotalNumberOfFowardPerContactReport extends Report implements MessageListener, ConnectionListener {

    private int hops;
    private Map<Integer, Integer> nrofFoward;
    private int nrofRelayed;

    public static final String TOTAL_CONTACT_INTERVAL = "perTotalContact";
    public static final int DEFAULT_CONTACT_COUNT = 500;
    private int interval;
    private int totalContact;
    private int lastRecord;

    public TotalNumberOfFowardPerContactReport() {
        init();
        Settings s = getSettings();
        if (s.contains(TOTAL_CONTACT_INTERVAL)) {
            interval = s.getInt(TOTAL_CONTACT_INTERVAL);
        } else {
            interval = DEFAULT_CONTACT_COUNT;
        }
    }

    @Override
    protected void init() {
        super.init();
        this.nrofRelayed = 0;
        this.nrofFoward = new HashMap<>();

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
        nrofRelayed++;
    }

    @Override
    public void done() {
        String statsText = "Contact\tFoward\n";
        for (Map.Entry<Integer, Integer> entry : nrofFoward.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
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
            int foward = nrofRelayed;

            nrofFoward.put(lastRecord, foward);
        }
    }

    @Override
    public void hostsDisconnected(DTNHost host1, DTNHost host2) {

    }
}
