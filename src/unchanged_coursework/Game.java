package unchanged_coursework;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * Game class stores lists of Players, Viewers, Donations and viewing times
 *
 * @author DAVID
 */
public class Game {

  /* Collections stored within Game object */
  private ArrayList<Player> players;
  private ArrayList<Viewer> viewers;
  private ArrayList<Donation> donations;
  private HashMap<Player, ArrayList<Long>> times;

  DecimalFormat round = new DecimalFormat("0.00");

  /* Properties of the Game */
  private String name;
  private boolean running;
  /**
   * volatile variable
   */
  private double totalDonationsRecordedByGame;
  private long totalViewingTimeRecordedByGame;
  // atomic
  private int totalNumberViewing;

  private Random random;
  private GUI gui;

  /* Game construtor */
  public Game(GUI gui, String name) {
    this.gui = gui;
    this.name = name;
    this.players = new ArrayList();
    this.viewers = new ArrayList();
    this.donations = new ArrayList();
    this.times = new HashMap();
    this.totalDonationsRecordedByGame = 0;
    this.random = new Random();
    /**
     * When a game object is created call initialise() You can only create 1
     * game so no need for synchronise
     */
    initialise();
  }

  /* Method to assign players to Game, called by constructor */
  public void initialise() {
    players.add(new Player("Scarlett"));
    players.add(new Player("David"));
    players.add(new Player("Thor"));
    players.add(new Player("Cleo"));
    System.out.println("Game" + players);
    // for each player in players copy it into the times Hashmap with a corresponding arraylist
    for (Player p : players) {
      times.put(p, new ArrayList());
    }
    running = true;
  }

  /* User by viewers when switching Players they are viewing */
  public Player getRandomPlayer() {
    return players.get(random.nextInt(players.size()));
  }

  /**
   * this doesn't need anything applied as the only place it is being effected
   * is already locked
   *
   * @param donation processes a donation that comes from a Viewer
   */
  public void processDonation(Donation donation) {
    totalDonationsRecordedByGame += donation.getAmount();
    donations.add(donation);
    // get the selected player form donation obj
    // ad the donation to the player in the player clas
    donation.getPlayer().addDonation(donation);
  }

  /**
   * Synchronise this method too
   *
   * @param p, @param time processes a record of time spent viewing a Player
   * that comes from a Viewer
   */
  public void recordViewingTime(Player p, long time) {
    totalViewingTimeRecordedByGame += time;
    times.get(p).add(time);
  }
  // 

  /* Basic methods for accessing Game properties */
  public ArrayList<Player> getPlayers() {
    return players;
  }

  /**
   * Synchronise this method
   */
  public ArrayList<String> getPlayersNames() {
    ArrayList<String> names = new ArrayList();
    for (Player p : players) {
      names.add(p.getPlayerName());
    }
    return names;
  }

  public ArrayList<Viewer> getViewers() {
    return viewers;
  }

  public boolean isRunning() {
    return running;
  }

  public void stopGame() {
    this.running = false;
  }

  public void startGame() {
    this.running = true;
  }

  /**
   * but it's not shared data....
   *
   * @return
   */
  public ArrayList<Donation> getDonations() {
    return donations;
  }

  /**
   * @return
   */
  public double getTotalDonationsRecordedByGame() {
    return totalDonationsRecordedByGame;
  }

  /**
   * This should be synchronised as only 1 thread should be able to access this
   * 1 at a time
   *
   * @return
   */

  public long getTotalViewingTimeRecordedByGame() {
    return totalViewingTimeRecordedByGame;
  }

  public HashMap<Player, ArrayList<Long>> getTimes() {
    return times;
  }

  public int getTotalNumberViewing() {
    // return totalNumberViewing.get()
    return totalNumberViewing;
  }

  /**
   * adds a viewer to the game
   *
   * @param v
   */
  public void viewerJoins(Viewer v) {
    // totalNumberViewing.getAndInecrement();
    // lock
    totalNumberViewing++;
    viewers.add(v);
    // unlock
    System.out.println("TotalNumberViewing in: " + getTotalNumberViewing());
  }

  /**
   * Removes a viewer from game
   *
   * @param v
   */
  public void viewerLeaves(Viewer v) {
    // totalNumberViewing.getAndDecrement();
    // lock
    totalNumberViewing--;
    viewers.remove(v);
    // unlock
    System.out.println("TotalNumberViewing out: " + getTotalNumberViewing());
  }

  /**
   * this is double check at the end. No threads would actually be running here
   * only EDT Method that checks for consistency of viewing time data Totals of
   * recorded running total in Game, total of times in HashMap, and total of
   * times sent by Viewers should all agree if all data is processed safely
   */
  public void countTimes() {
    String timeResults = "";
    long sumOfPlayerTotalTimes = 0;
    for (Player p : times.keySet()) {
      long totalTimeForPlayer = 0;
      for (Long time : times.get(p)) {
        totalTimeForPlayer += time;
        sumOfPlayerTotalTimes += time;
      }
      timeResults += p + " and had " + totalTimeForPlayer / 1000000 + "ms of views" + "\n";
    }

    timeResults += "\n Total viewing times recorded by Players: "
            + sumOfPlayerTotalTimes / 1000000 + "ms \n"
            + "Total viewing times recorded by Game:    "
            + totalViewingTimeRecordedByGame / 1000000 + "ms\n"
            + "Total viewing times recorded by Viewers: "
            + Viewer.getTimeViewers() / 1000000 + "ms\n";
    System.out.println(timeResults);
    // updates the gui
    gui.updateReport(timeResults);
    
  }

  /**
   * Method that checks for consistency of donations Totals of recorded
   * donations in Game, total of donations held by Player, and total of
   * donations sent by Viewers should all agree if all data is processed safely
   */
  public void checkDonations() {
    String report = "";
    /* Donations counted by the Game class */
    double totalOfDonations = 0;
    for (Donation d : donations) {
      totalOfDonations += d.getAmount();
    }
    report = "Donations counted by Game:    Number = "
            + donations.size() + " Value = "
            + round.format(totalOfDonations) + "\n";

    /* Sum of Donations counted by the Players */
    int numberDonationsToPlayers = 0;
    double totalOfDonationsToPlayers = 0;
    for (Player p : players) {
      numberDonationsToPlayers += p.getNumOfDonations();
      totalOfDonationsToPlayers += p.sumDonations();
    }
    report += "Donations counted by Players: Number = "
            + numberDonationsToPlayers + " Value = "
            + round.format(totalOfDonationsToPlayers) + "\n";

    /* Sum of Donations sent by the Viewer class */
    report += "Donations counted by Viewers: Number = "
            + Viewer.getTotalNumberOfDonations() + " Value = "
            + round.format(Viewer.getTotalValueOfDonations()) + "\n\n";

    System.out.println(report);
    // updates the gui
    // shouldn't this do a repaint?
    gui.updateReport(report);
  }

}
