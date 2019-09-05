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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;


/**
 *
 * @author BramChandra
 */
public class Latihan extends Report implements ConnectionListener,MessageListener{
    
    private Map<String,Double> creationtime;
    private int pesan;
    private int konek;
    private List<Double> latencies;
    
    public Latihan(){
        init();
    }
    
    public void init(){
        super.init();
        this.pesan = 0;
        this.konek = 0;
        this.creationtime = new HashMap<String,Double>();
        this.latencies = new ArrayList<Double>();
    }
    
    @Override
    public void hostsConnected(DTNHost host1, DTNHost host2) {
        konek++;
    }

    @Override
    public void hostsDisconnected(DTNHost host1, DTNHost host2) {
        
    }

    @Override
    public void newMessage(Message m) {
        pesan++;
        creationtime.put(m.getId(),getSimTime());
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
        
    }
    public void done(){
        write("Host Yang Terkoneksi : "+konek);
        write("Jumlah Pesan : "+pesan);
        write("Pesan ID\tWaktu Dibuat");
        for (Map.Entry<String, Double> entry : creationtime.entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();
            write(key+"\t"+value);
            
        }
        super.done();
    }

}
