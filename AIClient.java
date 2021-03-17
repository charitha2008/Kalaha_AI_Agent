package ai;

import ai.Global;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import kalaha.*;

/**
 * This is the main class for your Kalaha AI bot. Currently
 * it only makes a random, valid move each turn.
 * 
 * @author Johan Hagelbäck
 */
public class AIClient implements Runnable
{
    private int player;
    private JTextArea text;
    
    private PrintWriter out;
    private BufferedReader in;
    private Thread thr;
    private Socket socket;
    private boolean running;
    private boolean connected;
    private int bestMove=-1;
    private int bestUtility;
    	
    /**
     * Creates a new client.
     */
    public AIClient()
    {
	player = -1;
        connected = false;
        
        //This is some necessary client stuff. You don't need
        //to change anything here.
        initGUI();
	
        try
        {
            addText("Connecting to localhost:" + KalahaMain.port);
            socket = new Socket("localhost", KalahaMain.port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            addText("Done");
            connected = true;
        }
        catch (Exception ex)
        {
            addText("Unable to connect to server");
            return;
        }
    }
    
    /**
     * Starts the client thread.
     */
    public void start()
    {
        //Don't change this
        if (connected)
        {
            thr = new Thread(this);
            thr.start();
        }
    }
    
    /**
     * Creates the GUI.
     */
    private void initGUI()
    {
        //Client GUI stuff. You don't need to change this.
        JFrame frame = new JFrame("My AI Client");
        frame.setLocation(Global.getClientXpos(), 445);
        frame.setSize(new Dimension(420,250));
        frame.getContentPane().setLayout(new FlowLayout());
        
        text = new JTextArea();
        JScrollPane pane = new JScrollPane(text);
        pane.setPreferredSize(new Dimension(400, 210));
        
        frame.getContentPane().add(pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setVisible(true);
    }
    
    /**
     * Adds a text string to the GUI textarea.
     * 
     * @param txt The text to add
     */
    public void addText(String txt)
    {
        //Don't change this
        text.append(txt + "\n");
        text.setCaretPosition(text.getDocument().getLength());
    }
    
    /**
     * Thread for server communication. Checks when it is this
     * client's turn to make a move.
     */
    public void run()
    {
        String reply;
        running = true;
        
        try
        {
            while (running)
            {
                //Checks which player you are. No need to change this.
                if (player == -1)
                {
                    out.println(Commands.HELLO);
                    reply = in.readLine();

                    String tokens[] = reply.split(" ");
                    player = Integer.parseInt(tokens[1]);
                    
                    addText("I am player " + player);
                }
                
                //Check if game has ended. No need to change this.
                out.println(Commands.WINNER);
                reply = in.readLine();
                if(reply.equals("1") || reply.equals("2") )
                {
                    int w = Integer.parseInt(reply);
                    if (w == player)
                    {
                        addText("I won!");
                    }
                    else
                    {
                        addText("I lost...");
                    }
                    running = false;
                }
                if(reply.equals("0"))
                {
                    addText("Even game!");
                    running = false;
                }

                //Check if it is my turn. If so, do a move
                out.println(Commands.NEXT_PLAYER);
                reply = in.readLine();
                if (!reply.equals(Errors.GAME_NOT_FULL) && running)
                {
                    int nextPlayer = Integer.parseInt(reply);

                    if(nextPlayer == player)
                    {
                        out.println(Commands.BOARD);
                        String currentBoardStr = in.readLine();
                        boolean validMove = false;
                        while (!validMove)
                        {
                            long startT = System.currentTimeMillis();
                            //This is the call to the function for making a move.
                            //You only need to change the contents in the getMove()
                            //function.
                            GameState currentBoard = new GameState(currentBoardStr);
                            int cMove = getMove(currentBoard);
                            
                            //Timer stuff
                            long tot = System.currentTimeMillis() - startT;
                            double e = (double)tot / (double)1000;
                            
                            out.println(Commands.MOVE + " " + cMove + " " + player);
                            reply = in.readLine();
                            if (!reply.startsWith("ERROR"))
                            {
                                validMove = true;
                                addText("Made move " + cMove + " in " + e + " secs");
                            }
                        }
                    }
                }
                
                //Wait
                Thread.sleep(100);
            }
	}
        catch (Exception ex)
        {
            running = false;
        }
        
        try
        {
            socket.close();
            addText("Disconnected from server");
        }
        catch (Exception ex)
        {
            addText("Error closing connection: " + ex.getMessage());
        }
    }
    
    /**
     * This is the method that makes a move each time it is your turn.
     * Here you need to change the call to the random method to your
     * Minimax search.
     * 
     * @param currentBoard The current board state
     * @return Move to make (1-6)
     */
    public int getMove(GameState currentBoard)
    {
        //int myMove = getRandom();
        int maxDepth=5;
        int maxPlayer=player;
        boolean max=true;
        if(maxPlayer==2){
            max=false;
        }
        int myMove=myMiniMax(currentBoard, maxDepth,Integer.MIN_VALUE, Integer.MAX_VALUE, max);
        bestMove=myMove;
        return bestMove;
    }
    
    
     public int myMiniMax(GameState currentBoard,int depth,int alpha,int beta,boolean maxPlayer){
 
        if(maxPlayer){
            bestUtility=Integer.MIN_VALUE;//initialising best utility for max player
            
             ArrayList<Integer> possibleMoves = new ArrayList<>();//array for possible moves
             for(int i=1;i<=6;i++){
                if(currentBoard.moveIsPossible(i)){//possible ambos/moves
                    possibleMoves.add(i);//saving possible ambos
                
                    if(depth==0 || currentBoard.gameEnded()){//check if it is a leaf node
                        bestUtility=utilityFunction(currentBoard);//evaluate the board
                        this.bestMove=i;
                        return bestUtility;
                    }                
                
                    for(Integer move:possibleMoves){//iterating the possible moves
                        GameState newState=currentBoard.clone();//makes a clone of the currentboard
                        newState.makeMove(move);//
                        int utility=myMiniMax(newState,--depth, alpha, beta,false);//recursively call the minmax function for all posiible ambos
                        
                        if(utility>bestUtility){//comparing node values
                            bestUtility=utility;
                            this.bestMove=move;//takes the best move
                            
                        }

                        if(utility>alpha){//assign maximum to alpha
                            alpha=utility;
                            this.bestMove=move;
                            
                        }
                        if(beta<=alpha){//pruning
                            break;
                        }
                       
                    }
                    
                    if(this.bestMove!=-1){// return the best move from the loop
                        return this.bestMove;
                    }
                    
                    return alpha;
                    }
            }
        }

        else{//2nd player
          
            bestUtility=Integer.MAX_VALUE;//initialising best utility for min player
            ArrayList<Integer> possibleMoves = new ArrayList<>();
            
            for(int i=6;i>=1;i--){
                if(currentBoard.moveIsPossible(i)){
                    possibleMoves.add(i);
                
                    if(depth==0 || currentBoard.gameEnded()){
                        bestUtility=utilityFunction(currentBoard);
                        this.bestMove=i;
                        return bestUtility;
                    }
                    
                    for(Integer move:possibleMoves){
                        GameState newState=currentBoard.clone();
                        newState.makeMove(move);
                        int utility=myMiniMax(newState, --depth, alpha, beta,true);
                        
                        if(utility<bestUtility){
                            bestUtility=utility;
                            this.bestMove=move;
                        }
                        
                        if(utility<beta){//assign minimum to beta
                            beta=utility;
                            this.bestMove=move;
                        }
                        if(beta<=alpha){
                            break;
                        }
                        
                       
                    }
                    
                    if(this.bestMove!=-1){
                        return this.bestMove;
                    }
                    
                    return beta;
                                
                }
            }
         
    
        }
        return bestMove;//return best move from the method
        
    }
     
    int utilityFunction(GameState currentboard){
        int utility;
        int player1=player;
        int player2=2;
        if(player1==2){
            player2=1;
        }
        utility=currentboard.getScore(player1)-currentboard.getScore(player2);

        return utility;
    }
  /**
     * Returns a random ambo number (1-6) used when making
     * a random move.
     * 
     * @return Random ambo number
     */
    public int getRandom()
    {
        return 1 + (int)(Math.random() * 6);
    }
}