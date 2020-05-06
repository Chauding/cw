package unchanged_coursework;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Player object stores player name, number of active viewers, 
 * running totals of number and values of donations from viewers
 * also stores a record of all donations made to Player
 * 
 * Player class keeps running total of all submitted viewing times
 * @author DAVID
 */
public class Player{
    private final String playerName;    /* Player name */
    private int numViewers;             /* current number of viewers */
    private int numOfDonations;         /* running total number of donations */
    private double sumOfDonations;      /* running total of value of donations */
    
    private HashMap<Viewer, Double> donationsFromViewers; /* record of donations */
   
    // volitle
    private static long allTime;    /* class-level running total of all viewing times */

    /* Player constructor */
    public Player(String name) {
        this.playerName = name;
        this.numViewers = 0;
        donationsFromViewers = new HashMap();
        sumOfDonations = 0.0;
    }
    
    /**
     * atomic
     * methods for updating and accessing number of current viewers */
    public void gainOneViewer(){
        numViewers++;
    }
    public void loseOneViewer(){
        numViewers--;
    }
    // number of viwers watching this player
    public int getNumViewers() {
        return numViewers++;
    }
        
    /* method for processing donation from a Viewer into Player records */
    public void addDonation(Donation donation){
        this.numOfDonations++;
        this.sumOfDonations += donation.getAmount();
        Viewer donor = donation.getViewer();
        // lock
        if(donationsFromViewers.containsKey(donor)){
            donationsFromViewers.replace(donor, donationsFromViewers.get(donor) + donation.getAmount());
        }
        else{
            donationsFromViewers.put(donor, donation.getAmount());
        }
        // unlock
    }
    
    /* method to get total of all donations in Players record of donations */
    public double sumDonations(){
        int sum = 0;
        // try lock
        for(Double v: donationsFromViewers.values()){
            sum += v;
        }
        return sum;
    }
    
    @Override public String toString(){
        return playerName + " received " + this.numOfDonations + " donations, worth " + sumDonations() ;
    }
    
    /* Basic getter method for player name */
    public String getPlayerName() {
        return playerName;
    }
    
    /* method to return reference to HashMap of donation records made to this Player */
    public HashMap<Viewer, Double> getDonationsFromViewers() {
      // try lock

      return donationsFromViewers;
    }

    /**
     * called from GUI
     * methods to access running totals of number and values of donations */
    public int getNumOfDonations() {
      // try lock 
      return numOfDonations;
    }
    public Double getSumOfDonations() {
      // try lock  this is called from GUI
      return sumOfDonations;
    }
    
    // class-level methods to access and increase grand total of all viewing time  for all Players */
    public static long getAllTime() {
        return allTime;
    }
    public static void addToAllTime(long allTime) {
        Player.allTime += allTime;
    }

}
