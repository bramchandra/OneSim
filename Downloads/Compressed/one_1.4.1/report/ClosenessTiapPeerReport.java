/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.SimScenario;
import java.util.List;
import java.util.Map;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.Duration;
//import routing.community.FrequencyDecisionEngine;
//import routing.community.VarianceDecisionEngine;
import routing.community.ClosenessDetectionEngine;

/**
 *
 * @author jarkom
 */
public class ClosenessTiapPeerReport extends Report{
    public ClosenessTiapPeerReport(){
        init();
    }
    public void done(){
        
        List<DTNHost> nodes = SimScenario.getInstance().getHosts();        
        for (DTNHost h : nodes) {
            MessageRouter r = h.getRouter();
            if(!(r instanceof DecisionEngineRouter)){
                continue;
            }
            RoutingDecisionEngine de = ((DecisionEngineRouter)r).getDecisionEngine();
            if(!(de instanceof ClosenessDetectionEngine)){
                continue;
            }
            
            ClosenessDetectionEngine cd =(ClosenessDetectionEngine)de;
//            Map<DTNHost, Double> nodeComm = cd.getCloseness();
//            write(h+" ");
//            write(h+" "+nodeComm.get(h));
        }
        super.done();
    }
}
