package unchanged_coursework;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Viewer class extends Thread to run in parallel to Game
 * Viewer sends updates of donations made to players, 
 * and time spent watching each player. 
 * @author DAVID
 */
public class Viewer extends Thread{
    private final String id;            // id for viewer
    
    private Game game;                  // reference to Game associated with Viewer
    private Player viewedPlayer;        // Player that Viwere is currently watching
    private long startedViewingPlayer;  // stores nanosecond level start of viewing time of current Player 
    
    private Random random;
    private int numberActions;          // number of switches between Players that Viewer can make (make this volital)
    private int intervalBetweenActions; // time in milliseconds between switches
    
    /* class-level data tracking totals for all Viewers */
    private static long totalViewingTimeRecordedByViewers;
    
    /**
     * This is to populate the Donation history box
     * this is a volatile static ArrayList: Keeps the number of donation made by viewer
     */
    private volatile static ArrayList<Donation> donationsMade = new ArrayList();
    
    /***
     * Monitor variables
     */
    ReentrantLock lock = new ReentrantLock();
    
    /* Viewer constructor */
    public Viewer(String id, Game f, int actions, int interval){
        this.id = id;
        this.game = f;
        this.numberActions = actions;
        this.intervalBetweenActions = interval;
        this.viewedPlayer = game.getRandomPlayer();        
        this.random = new Random();
        this.viewedPlayer = null; getRandomPlayer();
        this.startedViewingPlayer = System.nanoTime();
    }

    /* Task for Viewer object, in a loop
     * to perform specified number of switches between Players
     * randomly donation around 10% of the time */
    @Override public void run(){
      // Critical Section
        // calls game to add new viewer
        game.viewerJoins(this);
        int actionsRemaining = this.numberActions;
        while(game.isRunning() && actionsRemaining > 0){
            try {
                Thread.sleep(this.intervalBetweenActions);
                /* randomly (10% of time) donate a ranom amount (0.01 to 5.00  to current Player*/
                if(Math.random() < 0.1){
                    donate(0.01 + 1.0*random.nextInt(500)/100.0);
                }
                getRandomPlayer();
                actionsRemaining--;
            } catch (InterruptedException ex) {System.out.println("Viewer " + id + " run method exception");}
        }
        // why are they leaving so quick
        game.viewerLeaves(this);
        switchPlayer(null);   
    }
    
    /**
     * lock 
     *  so that only 1 thread can make a donate at 1 time
     * method to process a donation for a Player */
    public void donate(double amount){
        Donation donation = new Donation(this, viewedPlayer, amount);
        /**
         * you don't mind the creation of a donation above you only want to make the 
         * bits that are being written mutually exclusive
         * lock here:
         */
        lock.lock();
        this.game.processDonation(donation);
        /// This should then update the GUI.
        Viewer.donationsMade.add(donation);
        // unlcok here
        lock.unlock();
    }
    
    /**
     * 
     * Method used to switch between Players */
    public void switchPlayer(Player newPlayerToWatch){
        if(viewedPlayer != null){ /* will be null when Viewer first starts */
            long timeViewed = System.nanoTime() - startedViewingPlayer;
            // Lock here
            lock.lock();
            game.recordViewingTime(viewedPlayer, timeViewed);
            Viewer.totalViewingTimeRecordedByViewers += timeViewed;
            this.viewedPlayer.loseOneViewer();
            Player.addToAllTime(timeViewed);
            // unlock
            lock.unlock();
        }        
        this.viewedPlayer = newPlayerToWatch;
        if(viewedPlayer != null){ /* will be null here if Viewer is finished viewing */
          // calling player class 
          // lock
          lock.lock();
          this.viewedPlayer.gainOneViewer();
          this.startedViewingPlayer = System.nanoTime();
          // unlock
          lock.unlock();
        }
    }
    
    /**
     * Rename this to getRandomPlayerForSwitch
     * convenient method for switching to random Player,calls general method above */
    public void getRandomPlayer(){
        Player randomPlayer = game.getRandomPlayer();
        System.out.println("Viwer watching: " + randomPlayer.getPlayerName());
        // call above switch player
        switchPlayer(randomPlayer);
    }
    

       
    /* class-level methods for accessing totals of viewing time, 
     * and number and value of all Viewers */   
    public static long getTimeViewers() {
        return Viewer.totalViewingTimeRecordedByViewers;
    }   
    public static int getTotalNumberOfDonations(){
        return donationsMade.size();
    }   
    public static double getTotalValueOfDonations(){
        double total = 0;
        for(Donation donation: donationsMade) total += donation.getAmount();
        return total;
    }
    
}
