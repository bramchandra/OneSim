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
import routing.RoutingDecisionEngine;

/**
 *
 * @author Khusus Skripsi Fuzzy
 */
public class NewClass implements RoutingDecisionEngine{

    public NewClass(NewClass t){
        
    }
    public NewClass(Settings t){
        
    }
    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
       
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        
    }

    @Override
    public boolean newMessage(Message m) {
        return false;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo()==aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return m.getTo() != thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        return true;
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
        return new NewClass(this);
    }

    @Override
    public void update(DTNHost thishost) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
