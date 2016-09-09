/*
Program Name: PlayerCard
Author: Barak Jacob
Student Number:100235615
Date:April 1, 2015
Course: CPSC 1181
Compiler: JDK 1.7
*/

import javax.swing.ImageIcon;
import javax.swing.JButton;
 /**
 * A Class that extends JButton and create the cards for the matching game.
* @author Barak Jacob
 */
public class PlayerCard extends JButton
{ 
	private static int lastAssignedNumber = -1;
   private int specialNum;

   /**
      * Constructs a card with default text.
      */
   public PlayerCard(ImageIcon i)
   {
      super();
      setIcon(i);
	   lastAssignedNumber++;
	   specialNum = lastAssignedNumber;
   } 
   
   /**
      * This method returns the number of the button
      *@return the special number of the button
      */
   public int getNum()
   {
	   return specialNum;
   }
     
}