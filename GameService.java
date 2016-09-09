/*
Program Name: GameService
Author: Barak Jacob
Student Number:100235615
Date:April 8, 2015
Course: CPSC 1181
Compiler: JDK 1.7
*/

import java.util.Date;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;

/**
 * A Class that creates a service from the server to the clients and vice versa.
* @author Barak Jacob
 */
public class GameService implements Runnable,GameConstants
{
   public static final int PLAYER_0 = 0;
   public static final int PLAYER_1 = 1;
   public static final int RESET_VALUE = -1;
   public static final int NUM_PLAYERS = 2;
   public static final int TIME_BEFORE_COVER = 1000;
   private int[] matchCounter;
   private Socket socket0;
   private Socket socket1;
   private JTextArea msgArea;
   private DataInputStream fromClient0;
   private DataInputStream fromClient1;
   //shuffled array containing numbers from 0-5
   private int[]cards;
   private DataOutputStream[] clients;
   private Random randomGenerator;

      
    /*
      * Constructs a service for two clients
      */
   public GameService(Socket s0,Socket s1,JTextArea area)
   {
      socket0 = s0;
      socket1 = s1;
      msgArea = area;
      cards = new int[NUM_OF_CARDS_IN_GAME];
      randomGenerator = new Random();
   }
   
   /**
      * This method displays a message in the log of the server.
      * @param msg - the message to be displayed.
      */
   private void report(String msg)
   {
      msgArea.append(msg +"\n");   
   }
   
   /**
      * This method creates the connection between the server and the clients, and helps pass info between them.
      */
   public void run()
   {
      try
      {
         try
         {
            fromClient0 = new DataInputStream(socket0.getInputStream());
            fromClient1 = new DataInputStream(socket1.getInputStream());
            clients = new DataOutputStream[NUM_PLAYERS];
            clients[PLAYER_0] = new DataOutputStream(socket0.getOutputStream()); 
            clients[PLAYER_1] = new DataOutputStream(socket1.getOutputStream());
            executeCommands();            
         }         
         
         finally
         {
            fromClient0.close();
            fromClient1.close();
            for(int i=0;i<clients.length;i++)
            {
               clients[i].close();   
            }
            socket0.close();
            socket1.close();
         }
      }
      
      catch(Exception e)
      {
	      report("problems in server " + e.toString() + "\n");
         e.printStackTrace(System.err);   
      }         
      
   }
   
   /**
      * This method sets the player number for each of the clients playing the game.
      */
   public void setPlayers()throws IOException
   {
      for(int i=0;i<clients.length;i++)
      {
         clients[i].writeInt(SETPLAYER);
         clients[i].writeInt(i);//player number
         clients[i].flush();
      }
   }

   /**
      * This method infroms the clients which player is playing.
      */
   public void playing(int whosPlaying)throws IOException
   {
      for(int i=0;i<clients.length;i++)
      {
         clients[i].writeInt(PLAYING);
         clients[i].writeInt(whosPlaying);
         clients[i].flush();   
      }

   }
   
   /**
      * This method infroms the which player won the game
      *@param whoWon - the player number of the client who won the game.
      */
   public void win(int whoWon)throws IOException
   {
      for(int i=0;i<clients.length;i++)
      {
         clients[i].writeInt(WIN);
         clients[i].writeInt(whoWon);
         clients[i].flush();
      }
	
   }
   
   /**
      * This method sends a reveal command to the client, to reveal a certain card on the board.
      *@param whatCard - the card number of the card to be revealed.
      */
   public void reveal(int whatCard)throws IOException
   {
      for(int i=0;i<clients.length;i++)
      {
         clients[i].writeInt(REVEAL);
         clients[i].writeInt(whatCard);
         clients[i].flush();
      }
   }
   
   /**
      * This method sends a match command to the client that got a match.
      *@param whatPlayer- the player number of the player who go the match.
      */
   public void match(int whatPlayer)throws IOException
   {
      matchCounter[whatPlayer]++;
      clients[whatPlayer].writeInt(MATCH);
      clients[whatPlayer].flush();
   }

   /**
      * This method sends a cover command to the client to cover two certain cards.
      *@param firstCard - the first card to be covered.
      *@param secondCard - the second card to be covered.
      */
   public void cover(int firstCard, int secondCard)throws IOException
   {
      for(int i=0;i<clients.length;i++)
      {
         clients[i].writeInt(COVER);
         clients[i].writeInt(firstCard);
         clients[i].writeInt(secondCard);
         clients[i].flush();
      }
   }

   /**
      * This method sends a shuffle command to the client, and will also send the elements of the shuffled array.
      */
   public void sendShuffle()throws IOException
   {
      for(int i=0;i<clients.length;i++)
      {
         clients[i].writeInt(SHUFFLE);
         clients[i].flush();
         for(int j=0;j<cards.length;j++)
         {
            clients[i].writeInt(cards[j]);   
            clients[i].flush();    
         }      
      }

   }
   
   /**
      * This method sends a win command to the player who won in case of a quit.
      *@param whoWon - the player number of the client who won.
      */
   public void quit(int whoWon)throws IOException
   {
      report("Player 0 has sent a QUIT command.");
      report("player 1 won");
      win(whoWon);
      report("Game is over.");
      //game is over, can break out of the loop
   }
   /**
      * This maethod simulates the actual information passage between the server and the clients playing the game.
      */
   public void executeCommands()throws IOException
   {
	   resetArray();
	   shuffle();
	   //will count the matches
	   matchCounter = new int[NUM_PLAYERS];
      sendShuffle();
      setPlayers();
      boolean player0Playing = true;
      while(true)
      {
         if (player0Playing)
         {
            playing(PLAYER_0);
            int player0Action = fromClient0.readInt();
            if(player0Action == QUIT)
            { 
               quit(PLAYER_1);
               return;
            }
            else if(player0Action==TRY)
            {
               int card1 = fromClient0.readInt();
               report("player 0 is trying card "+card1);
               reveal(card1);
               int action2;
               int card2 = RESET_VALUE;
               
               action2 = fromClient0.readInt();
               if(action2 == TRY)
               {
                  card2 = fromClient0.readInt();
                  report("player 0 is trying card "+card2);
                  reveal(card2);
                  
                  if(cards[card1] == cards[card2])//same shuffled number
                  {
                     report("we have a match!");
                     match(PLAYER_0);
                  }
                  else
                  {
                     try
                     {
                        //let players see the cards for a second
                        Thread.sleep(TIME_BEFORE_COVER);
                     }
                     catch(InterruptedException e)
                     {
                        e.printStackTrace(System.err);
                     }
                     report("will cover "+card1+" "+card2);
                     cover(card1,card2);
                  }
               }
               else if(action2==QUIT)
               {
                  quit(PLAYER_1);
                  return;                  
               }
               
            }
            
            else
               report("Unkown command");
         }
         
		   else
		   {
            playing(PLAYER_1);
            int player1Action = fromClient1.readInt();
            
            if(player1Action == QUIT)
            {
               quit(PLAYER_0);
               return;
            }
            else if(player1Action==TRY)
            {
          
               int card1 = fromClient1.readInt();
               report("player 1 is trying card "+card1);
               reveal(card1);
               int action2;
               int card2 = RESET_VALUE;
               action2 = fromClient1.readInt();
               if(action2 == TRY)
               {
                  card2 = fromClient1.readInt();
                  report("player 1 is trying card "+card2);
                  reveal(card2);
                  
                  if(cards[card1] == cards[card2])//same shuffled number
                  {
                     report("we have a match!");
                     match(PLAYER_1);
                  }
                  else
                  {
                     try
                     {
                        Thread.sleep(TIME_BEFORE_COVER);
                     }
                     catch(InterruptedException e)
                     {
                        e.printStackTrace(System.err);
                     }
                     report("will cover "+card1+" "+card2);
                     cover(card1,card2);
                  }
               }
               else if(action2==QUIT)
               {
                  quit(PLAYER_0); 
                  return;                  
               }
            }
            
            else
               report("Unkown command");
         }
		
		//check if the game is over at the end of every turn
         if(matchCounter[PLAYER_0]+matchCounter[PLAYER_1] == 6)
         {
            if(matchCounter[PLAYER_0] >= matchCounter[PLAYER_1])
            {
               report("player 0 won");
               win(PLAYER_0);
            }
            else
            {
               report("player 1 won");
               win(PLAYER_1);
            }
            report("Game is over.");
            //game is over, can break out of the loop
            return;
         }
            
         //alternate turns
         player0Playing = !player0Playing;
	
      }         
   }
   
   /**
      * This method shuffles an array of numbers from 0-5 using a Random object.
      */
   public void shuffle()
   {
      int i = 0;
      int count = 0;
      int whatImage = 0;
      int index = randomGenerator.nextInt(NUM_OF_CARDS_IN_GAME); 
      while(i<NUM_OF_CARDS_IN_GAME)
      {
         if(cards[index]!= -1)
            index = randomGenerator.nextInt(NUM_OF_CARDS_IN_GAME); 
         else
         {
            cards[index] = whatImage;
            i++;
            count++;
            if(count==PAIR)
            {
               whatImage++;
               count=0;
            }
            index = randomGenerator.nextInt(NUM_OF_CARDS_IN_GAME); 
         }
      }
   }
   
   /**
      * This method resets the array to be shuffled to a default value.
      */
   public void resetArray()
   {  
      for(int i=0;i<cards.length;i++)
      {
         cards[i] = RESET_VALUE;   
      }
   }
   
}