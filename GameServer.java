/*
Program Name: GameServer
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
 * A Class that creates a server for the game.
* @author Barak Jacob
 */
public class GameServer extends JFrame implements GameConstants
{
   public static final int FRAME_WIDTH = 700;
   public static final int FRAME_HEIGHT = 600;
   private JTextArea msgArea;
   
   /**
      * This method creates a new instance of the server class in order for it to run.
      * @param args - Unused
      */
   public static void main(String[]args)
   {
      new GameServer();   
   }         
   /*
      * Constructs a server with a log.
      */
   public GameServer()
   {
      
      buildGUI();
      ServerSocket serverSocket = null;
      try
      {
         serverSocket = new ServerSocket(PORT); 
         report("Server started at "+new Date());
         int clientNum=0;
         //Server is always available
         report("waiting for 2 players to connect...");
         while(true) 
         {
            Socket socket0 = serverSocket.accept();
            clientNum++;
            report("Player 0 connected, waiting for another player...");
            Socket socket1 = serverSocket.accept();
            clientNum++;  
            report("Two players are connected. Game is starting.");
            GameService game = new GameService(socket0,socket1,msgArea);
            
            Thread t = new Thread(game);
            t.start();   
         }            
      }
      
      catch(IOException e)
      {
         e.printStackTrace(System.err);   
      } 
      
      finally
      {
         try
         {
            serverSocket.close();
            report("Closed off the server socket.\n");
            report("Hasta la vista!\n");
         }
         catch (IOException e)
         {
            report("difficulties with closing the server socket\n");
            System.err.println("Oh well, I tried but failed to close the server socket");
            e.printStackTrace(System.err);
         }
      
      }
   }
   
    /**
      * This method displays a message in the log of the server.
      * @param msg - the message to be displayed.
      */
   private void report(String msg)
   {
      msgArea.append(msg+"\n");   
   }
   
   /**
      * This method creates the GUI for the server log.
      */
   private void buildGUI()
   {
      msgArea = new JTextArea();
      setSize(FRAME_WIDTH,FRAME_HEIGHT); 
      setLayout(new BorderLayout());
      add(new JScrollPane(msgArea),BorderLayout.CENTER);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setVisible(true);      
   }
   
   
   
   
}