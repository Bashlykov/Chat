/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chat.Client;

import Chat.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hexfox
 */
public class Client
{
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    private volatile static boolean socketOpen;
    private static String userName;
    private static String userPassword;
    private static String userStatus;
    private static final BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );  
    private static String tmpUserName;
    private static String tmpUserPassword;
    
    private static String userName()
    {      
        String name = null;
        try
        {
            System.out.println( "[Input your name:]" );
            name = sysin.readLine();
        }
        catch ( IOException ex )
        {
            Logger.getLogger( Client.class.getName() ).log( Level.SEVERE, null,
                    ex );
        }  
        return name;
    }  
    
    private static String userPassword( String printMsg )
    {
        String pass = null;
        try
        {      
            System.out.println( printMsg );
            pass = sysin.readLine();            
        }
        catch ( IOException ex )
        {
            Logger.getLogger( Client.class.getName() ).log( Level.SEVERE, null,
                    ex );
        }
        return pass;
    }
    
    private static void userStatus()
    {
        try
        {       
            System.out.print( "[Input your status:]\n1 - sleep\n2 - eat\n3 - working\n");
            
            int readNum = sysin.read();
            
            switch ( readNum )             
            {
                case '1': userStatus = "sleep";
                        break;
                case '2': userStatus = "eat";
                        break;
                case '3': userStatus = "work";
                        break;
                default: userStatus = "online";
                        break;                
            }
        }
        catch ( IOException ex )
        {
            Logger.getLogger( Client.class.getName() ).log( Level.SEVERE, null,
                    ex );
        }
    }
        
    public static void main( String[] args ) throws IOException
    {
        String host = "localhost";
        int port = 1234;      
        
        Socket socket = new Socket( host, port ); 
               
        System.out.println( "Connected to server..." );       
      
        //Client.userName = Client.userName();

        Client.userStatus();
                           
        socketOpen = true;
                                                      
        new Thread () // input thread
        {
            String date = null;
            String ip = null;
            String name = null;
            String msg = null;
            String status = null;
            
            @Override
            public void run()
            {
                try
                {
                    ObjectInputStream inObj = null;    
                    
                    if ( inObj == null )
                    {                          
                        inObj = new ObjectInputStream( socket.getInputStream() ); 
                    }
                    
                    while( socketOpen )
                    {
                        Thread.sleep(10);
                       
                        Message message = ( Message ) inObj.readObject();
                        
                        switch ( message.getMessage() )
                        {
                            case "<UE>":
                                System.out.println( "[Username and password are correct]");
                                Client.userName = Client.tmpUserName;
                                Client.userPassword = Client.tmpUserPassword;
                                continue;
                                
                            case "<NUE>":
                                System.out.println( "[Username and password are NOT correct]");
                                continue;
                                
                            case "<PCH>":
                                System.out.println( "[Password changed successfully]" );
                                Client.userPassword = Client.tmpUserPassword;
                                continue;
                                
                            case "<NPCH>":
                                System.out.println( "[Username or password are NOT correct]" );
                                continue;
                        }
                                
                        date = message.getDate() != null ? message.getDate() : "";
                        ip = message.getIp() != null ? message.getIp() + "@" : "";
                        name = message.getUserName() != null ? message.getUserName() + ": " : "";
                        msg = message.getMessage() != null ? message.getMessage() : "";
                        status = message.getUserStatus() != null ? "[" + message.getUserStatus() + "]" : "";
                        
                        System.out.println( date + status + ip + name + msg );
                    }                                      
                }   
                catch ( java.io.EOFException ex )
                {
                    System.out.println( "[Disconnected]" );
                    System.exit( MIN_PRIORITY );
                }
                catch ( IOException | InterruptedException | ClassNotFoundException ex )
                {
                    Logger.getLogger(Client.class.getName() ).log( Level.SEVERE,
                            null, ex );
                }
            }
        }.start();
                       
        new Thread () // output thread
        {
            @Override
            public void run()
            {
                try
                { 
                    String sendText;
                    ObjectOutputStream outObj = null;
                    
                    if ( outObj == null )
                    {                          
                        outObj = new ObjectOutputStream( socket.getOutputStream() );
                    }  
                    
                    while( socketOpen ) 
                    {                     
                        Thread.sleep(100);
                        //System.out.print( "> " );
                        sendText = sysin.readLine();
                        
                        if ( sendText.equalsIgnoreCase("!s" ) ) // switch status
                        {
                            Client.userStatus();
                            continue;
                        }
                        
                        if ( sendText.equalsIgnoreCase( "!n" ) ) // switch user name
                        {                          
                            outObj.writeObject( new Message( sendText ) );
                            outObj.flush(); 
                            
                            Client.tmpUserName = Client.userName();
                            Client.tmpUserPassword = Client.userPassword( "[Input your password:]" );
                             
                            Message msg = new Message();
                            
                            msg.setUserName( Client.tmpUserName );
                            msg.setUserPass( Client.tmpUserPassword );
                            
                            outObj.writeObject( msg );
                            outObj.flush();
                            
                            continue;
                        }
 
                        if ( sendText.equalsIgnoreCase( "!e" ) ) // edit user password
                        {                          
                            outObj.writeObject( new Message( sendText ) );
                            outObj.flush(); 
                            
                            Client.tmpUserPassword = Client.userPassword( "[Input your NEW password:]" );
                             
                            Message msg = new Message();
                            
                            msg.setUserName( Client.userName );
                            msg.setOldUserPass( Client.userPassword );
                            msg.setUserPass( Client.tmpUserPassword );
                            
                            outObj.writeObject( msg );
                            outObj.flush();
                            
                            continue;
                        }
                        
                        if ( sendText.equalsIgnoreCase( "!r" ) ) // registration new name
                        {                           
                            outObj.writeObject( new Message( sendText ) );
                            outObj.flush();   
                            
                            Client.userPassword = Client.userPassword( "[Registration your name]" );
                            
                            outObj.writeObject( new Message( Client.userPassword ) );
                            outObj.flush();
                            
                            continue;
                        }
                                                
                        SimpleDateFormat dateFormat = new SimpleDateFormat("[dd.MM.yyyy][hh:mm]");                       
                        
                        outObj.writeObject( new Message(    dateFormat.format( new Date() ), 
                                                                socket.getLocalAddress().toString(), 
                                                                userName, 
                                                                sendText,
                                                                userStatus
                                                            ) );
                        outObj.flush();                                                           
                    }                    
                }
                catch ( java.net.SocketException ex )
                {
                    System.out.println( "[Server is down]" );
                    System.exit( MIN_PRIORITY );
                }
                catch ( IOException | InterruptedException ex )
                {
                    Logger.getLogger(Client.class.getName() ).log( Level.SEVERE,
                          null, ex );
                }          
            }
        }.start();
        
    }    
}

