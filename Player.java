/*
Program Name: Player
Author: Barak Jacob
Student Number:100235615
Date:April 8, 2015
Course: CPSC 1181
Compiler: JDK 1.7
*/



import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.ImageIcon;
import java.awt.Font;
import java.awt.Color;
/**
 * A Class that creates a client for the game
* @author Barak Jacob
 */
public class Player extends JFrame implements Runnable,GameConstants
{
   public static final int FRAME1_WIDTH = 800;
   public static final int FRAME1_HEIGHT = 800;
   public static final int NOT_EMPTY = 1;
   public static final int WINNER_FONT_SIZE = 22;
   public static final int ERROR_MATCH_FONT_SIZE = 18;
   public static final int HOW_MANY_TURNS = 2;
   public static final ImageIcon cover = new ImageIcon("cover.jpg");
   
   //boolean array to keep track of which cards are revealed
	private boolean[] revealed;
	//images array for the front of the cards
	private ImageIcon[] images;
   //image for the back of the cards
   //array that stores shuffled numbers 0-5
	private int[] indexImg;
   private PlayerCard[] cards;
	private JLabel errorLabel;
	private int matchedPairs;
   private int cardValue;
   private JLabel matchLabel;
   //contains the player number
   private int youAre;
   private CardListener m;
   private QuitListener q;
   private JButton quit;
   private int turnCount;
   private DataOutputStream toServer;
   private DataInputStream fromServer;
   private Socket socket;
   /**
      * This method creates a new instance of the client class in order for it to run.
      * @param args - an array of arguments entered by the user - help,the server address, and images directory.
      */
   public static void main(String[] args)
   {
	   //default values
      String host = "localhost";
      String dir = "puppies\\";

	   boolean errorFound = false;
	   if (args.length==0)
		   errorFound = true;
	   int i=0;
	   while(!errorFound && i<args.length)
	   {
		   if(args[i].equals("-help"))
         {
			   errorFound = true;
         }
		   else if(args[i].equals("-server"))
		   {
			   i++;
			   if(args.length==i)
				   break;
			   else
			   {
				   host = args[i];
               i++;
			   }
		   }
         else if(args[i].equals("-img"))
         {
            i++;
            if(args.length==i)
               break;
            else
            {
               dir = args[i];
               i++;               
            }
      
	      }
      }
      if(errorFound = true)
         System.out.println(usageStr());
   
	   Player client = new Player(host,dir);
	
      
   }
   
   /**
      * This method creates a usage string for the the -help argument
      * @return the usage string
      */
   private static String usageStr()
   {
      String progName = "Player";
      String str = "Usage: ";
      str+="\tjava "+progName + "[-help]";
      str+="\t-server hostAddr serverAddress";
      return str;
   }
   
    /*
      * Constructs a client the connects to a server.
      */
   public Player(String host, String dir)
   {
      images = new ImageIcon[NUM_OF_CARDS_IN_GAME/PAIR];
      for(int i=0;i<images.length;i++)
      {
         //use the given directory and given ip address (or default)
         images[i] = new ImageIcon(dir+i+".jpg");   
      }

	   m = new CardListener();
	   q = new QuitListener();
	   buildGUI();
      connectToServer(host);
      Thread t = new Thread(this);
      t.start();      
   }
   
   /**
      * This method establishes the connection between the client and the server.
      * @param host - the address of the server
      */
   public void connectToServer(String host)
   {
      try
      {
         socket = new Socket(host,PORT);   
         toServer = new DataOutputStream(socket.getOutputStream());
         fromServer = new DataInputStream(socket.getInputStream());
      }
      
      catch(IOException e)
      {
         e.printStackTrace(System.err);  
      }      
   }
   
   /**
      * This method creates the connection between the server and the clients, and helps pass info between them.
      */
   public void run()
   {
      
      boolean isContinue = true;
      try
      {
         try
         {
            while(isContinue)
            {
               int action = fromServer.readInt();
               if(action == SETPLAYER)
               {
                  youAre = fromServer.readInt();
		            for(int i=0;i<cards.length;i++)
		            {
			            cards[i].setEnabled(true);
		            }
               }
               else if(action==SHUFFLE)
               {
                  for(int i=0;i<indexImg.length;i++)
                  { 
                     //get shuffled numbers from server
			            indexImg[i] = fromServer.readInt();
                  }                  
               }
		
               
               else if(action==PLAYING)
               {
                  int turn = fromServer.readInt();
                  if(turn != youAre)
                  {
                     
                     errorLabel.setText("It is not your turn. Please wait for you opponent to play.");
                     for(int i=0;i<cards.length;i++)
                     {
                        cards[i].removeActionListener(m);
                     }
                     quit.removeActionListener(q);
                  }
                  else
                  {
                     turnCount=0;
                     errorLabel.setText("It's your turn!!!!");
                     for(int i=0;i<cards.length;i++)
                     {
                        cards[i].addActionListener(m);
                     }
                     quit.addActionListener(q);
                     
                  }
		         }
               
               else if(action == WIN)
               {
                  quit.removeActionListener(q);
                  for(int i=0;i<cards.length;i++)
                  {
                     cards[i].removeActionListener(m);   
                  }
                  
                  int winnerNum = fromServer.readInt(); 
                  errorLabel.setFont(new Font("Arial", Font.ITALIC, WINNER_FONT_SIZE));
                  errorLabel.setForeground(Color.RED);
                  if(winnerNum == youAre)
                  {
                      errorLabel.setText("Congrats, you won!!");
                  }
				
			         else
				         errorLabel.setText("Sadly, you lost.");
			         isContinue = false;
               }
	            else if(action==REVEAL)
               {
                  int cardNum = fromServer.readInt();
                  //use shuffled array to find the index for the correct image
                  cards[cardNum].setIcon(images[indexImg[cardNum]]);
                  //mark that the card is currently revealed
                  revealed[cardNum] = true;
                  
               }
               else if(action==COVER)
               {
                  int cardNum = fromServer.readInt();
                  int cardNum2 = fromServer.readInt();
                  cards[cardNum].setIcon(cover);
                  cards[cardNum2].setIcon(cover);
                  //mark that the cards are currently covered
                  revealed[cardNum] = false;
                  revealed[cardNum2] = false;
               }
               else if(action==MATCH)
               {
                  matchedPairs++;
                  matchLabel.setText("NUMBER OF MATCHING PAIRS: "+matchedPairs);
               }
               
            }
         }

         finally
         {
            socket.close();
            toServer.close();
            fromServer.close();            
         }         
      }
         

      catch(IOException e)
      {
         e.printStackTrace(System.err);     
      }      
   
   }
   
    /**
      * This method creates the GUI for the matching card game.
      */
   public void buildGUI()
   {
	   revealed = new boolean[NUM_OF_CARDS_IN_GAME];
	   quit = new JButton("QUIT");
	   indexImg = new int[NUM_OF_CARDS_IN_GAME];
	   errorLabel = new JLabel("WAITING FOR AN OPPONENT...");
	   JPanel cardPanel = new JPanel(); 
      JPanel matchPanel = new JPanel();
      JPanel bottomPanel = new JPanel();
      //create the frame
      JFrame frame1 = new JFrame();
      final int ROWS = 3;
      final int COLS = 4;
      cardPanel.setLayout(new GridLayout(ROWS,COLS));
	   errorLabel.setFont(new Font("Arial",Font.ITALIC,ERROR_MATCH_FONT_SIZE)); 
      matchLabel = new JLabel("NUMBER OF MATCHING CARDS: " + matchedPairs);
      matchLabel.setFont(new Font("Arial",Font.ITALIC,ERROR_MATCH_FONT_SIZE)); 
      cards = new PlayerCard[NUM_OF_CARDS_IN_GAME]; 
      for(int i=0;i<NUM_OF_CARDS_IN_GAME;i++)
      {
         cards[i] = new PlayerCard(cover);
         cardPanel.add(cards[i]);         
      }      
      matchPanel.add(matchLabel);
      bottomPanel.setLayout(new BorderLayout());
      JPanel winnerPanel = new JPanel();
      bottomPanel.add(quit,BorderLayout.EAST);
      bottomPanel.add(matchPanel,BorderLayout.WEST);
      frame1.setLayout(new BorderLayout());
      frame1.add(cardPanel,BorderLayout.CENTER);
      frame1.add(bottomPanel,BorderLayout.SOUTH);
      frame1.add(errorLabel,BorderLayout.NORTH); 
      
      frame1.setSize(FRAME1_WIDTH,FRAME1_HEIGHT);
      frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame1.setVisible(true);
      
      for(int i=0;i<cards.length;i++)
		{
			cards[i].setEnabled(false);
		}
    
   }
   
   /**
         * This inner class will process the card pressed by the user and will send a command to the server
         */
   class CardListener implements ActionListener
   {
       /**
               * This method contains the action to be performed when the user presses the card 
               * @param e - an ActionEvent object.
               */
      public void actionPerformed(ActionEvent e)
      {
         try
         {
            PlayerCard c = (PlayerCard)e.getSource();
            //if the card pressed is currently revealed - ignore the call
            if(revealed[c.getNum()])
            {
               return;
            }
            //if player tries to make more than two moves in a turn - ignore the call
            if(turnCount>=HOW_MANY_TURNS)
            {
               return;
            }
            toServer.writeInt(TRY);
            toServer.writeInt(c.getNum());
            toServer.flush();
            turnCount++;
         
         }
         catch(IOException io)
         {
            io.printStackTrace(System.err);
         }
	   }
   }

   /**
      * This inner class will process the quit command sent by the client
      */
   class QuitListener implements ActionListener
   {
      /**
               * This method contains the action to be performed when the user presses the quit button
               * @param e - an ActionEvent object.
               */
      public void actionPerformed(ActionEvent e)
      {
         try
         {
            toServer.writeInt(QUIT);
            toServer.flush();
         }
         catch(IOException io)
         {
            io.printStackTrace(System.err);
         }
         
      }
   }
}