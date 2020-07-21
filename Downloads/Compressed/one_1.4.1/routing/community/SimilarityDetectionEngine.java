/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.community;

import core.DTNHost;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Khusus Skripsi Fuzzy
 */
public interface SimilarityDetectionEngine {

    public ArrayList<Double> getCloseness();
    public ArrayList<Double> getVariance();
}
