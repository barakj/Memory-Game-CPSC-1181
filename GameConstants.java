/**
 * Useful constants for the game
 * @author Barak Jacob
 */

public interface GameConstants
{
   int PAIR = 2;
   int NUM_OF_CARDS_IN_GAME = 12;
   int PORT = 1181;
   
   // SETPLAYER is sent by the server to the client
   //    SETPLAYER p
   // where p is an int
   int SETPLAYER = 0;
   
   // PLAYING is sent by the server to the client
   //    PLAYING n
   // where n is an int(player's number)
   int PLAYING = 1;
   
   // TRY is sent by the client to the server
   //    TRY c
   // where c is an int(card number)
   int TRY = 2;
   
   // COVER is sent by the server to the client
   //    COVER c,c2
   // where c and c2 are integers(card numbers)
   int COVER = 3;
   
   // MATCH is sent by the server to the client
   //     MATCH
   int MATCH = 4;
   
    // REVEAL is sent by the server to the client
   //    REVEAL r
   // where r is an int(card number)
   int REVEAL = 5;
   
   // QUIT is sent by the client to the server 
   //     QUIT
   int QUIT = 6;
   
   // WIN is sent by the server to the client
   //    WIN w
   // where n is an int(player's number)
   int WIN = 7;
   
   // SHUFFLE is sent by the server to the client
   //    SHUFFLE a[] 
   // where a is an array of 12 integers(shuffled cards)
   
   int SHUFFLE = 8;
   /**
	* This arrray CMD is a convenience when debuggingd
	*/
   String[] cmd = {
      "SETPLAYER",
      "PLAYING",
      "TRY",
      "COVER",
      "MATCH",
      "REVEAL",
      "QUIT",
      "WIN",
      "SHUFFLE"};
   
   
}