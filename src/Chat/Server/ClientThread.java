/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chat.Server;

import Chat.Message;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 *
 * @author hexfox
 */
public class ClientThread implements Runnable 
{    
    private final RunServer runServer;
    private final ServerSocket server;
    private final Socket client;
    private String clientName;
    private final Thread thread;  
    private ObjectInputStream inObj = null;
    private ObjectOutputStream outObj = null;
    private String userPassword;
        
    private boolean connected = true;
    
    public ClientThread( RunServer runServer, 
                         Socket client ) throws IOException
    {        
        this.runServer = runServer;
        this.server = runServer.getServerSocket();
        this.client = client;
        
        this.thread = new Thread( this );
        this.clientName = "client_" + this.thread.getId(); 
        
        inObj = new ObjectInputStream( this.client.getInputStream() ); 
        outObj = new ObjectOutputStream( this.client.getOutputStream() );
        
        Server.countUsers++;
        
        thread.start();
    }
   
    private boolean loopHandler() throws IOException, ParserConfigurationException, SAXException, TransformerException
    {                           
        String date;
        String ip;
        String user;
        String msg;
        String status;
        
        while ( connected )
        {         
            try
            {                                                       
                Message message = ( Message ) inObj.readObject();                
                
                date = message.getDate() != null ? message.getDate() : "";
                ip = message.getIp() != null ? message.getIp() + "@" : "";
                msg = message.getMessage() != null ? message.getMessage() : "";  
                status = message.getUserStatus() != null ? "[" + message.getUserStatus() + "]" : "";
                        
                if ( message.getUserName() != null && !message.getUserName().equals( "" ) )
                {
                    user = message.getUserName() + ": "; 
                    this.clientName = message.getUserName();
                }
                else
                {
                    user = this.clientName + ": ";
                    message.setUserName( this.clientName );
                }   
                
                runServer.addMessage( message ); 
                
                runServer.broadcastMessage( message );
                
                if ( message.getMessage().equalsIgnoreCase( "!d" ) )  // disconnect from server
                {
                    System.out.println( this.clientName + " disconnected ..." );                     
                    outObj.writeObject( new Message( "Bye bye " + this.clientName ) );
                    outObj.flush();

                    runServer.delClient( this );                   
                    client.close();
                    connected = false;
                    Server.countUsers--;
                    
                    continue;
                }              
                
                if ( message.getMessage().equalsIgnoreCase( "!l" ) ) // list all users connected
                {                                       
                    for( int i = 0; i < runServer.getCountClients(); i++ )
                    {
                        outObj.writeObject( new Message( runServer.getClient( i ).clientName ) );
                        outObj.flush();
                    }                    
                }              

                if ( message.getMessage().equalsIgnoreCase( "!r" ) ) // registration new name
                {
                    message = ( Message ) inObj.readObject(); 
                    this.userPassword = message.getMessage();
                    this.runServer.appendXmlRecord( Integer.parseInt( runServer.getXmlRecordLastId() ) + 1, 
                                                                        this.clientName, 
                                                                        this.userPassword );
                    
                    outObj.writeObject( new Message( "Your name " 
                                                    + this.clientName 
                                                    + " registered\nPassword: " 
                                                    + this.userPassword ) );
                    outObj.flush(); 
                    continue;
                }   

                if ( message.getMessage().equalsIgnoreCase( "!n" ) ) // switch user name
                {                                       
                    message = ( Message ) inObj.readObject(); 
                    String tmpName = message.getUserName();
                    String tmpPass = message.getUserPass();                   
                    
                    Boolean userExists = runServer.checkXmlRecord( tmpName, tmpPass );
                    
                    if ( userExists )
                    {
                        outObj.writeObject( new Message( "<UE>" ) );   
                        outObj.flush();
                        this.clientName = tmpName;
                        this.userPassword = tmpPass;             
                        System.out.println( "Username and password are correct");
                    }
                    else
                    {
                        outObj.writeObject( new Message( "<NUE>" ) );
                        outObj.flush();
                        System.out.println( "Username and password are NOT correct");
                    }
                    continue;
                }   

                if ( message.getMessage().equalsIgnoreCase( "!e" ) ) // edit password
                {                                       
                    message = ( Message ) inObj.readObject(); 
                    String tmpName = message.getUserName();
                    String tmpOldPass = message.getOldUserPass();
                    String tmpNewPass = message.getUserPass();
                    
                    Boolean userExists = runServer.editXmlRecord( tmpName, tmpOldPass, tmpNewPass );
                    
                    if ( userExists )
                    {
                        outObj.writeObject( new Message( "<PCH>" ) );   
                        outObj.flush();

                        this.userPassword = tmpNewPass;             
                        System.out.println( "Password changed successfully");
                    }
                    else
                    {
                        outObj.writeObject( new Message( "<NPCH>" ) );
                        outObj.flush();
                        System.out.println( "Username or password are NOT correct");
                    }
                    continue;
                }
                
                if ( message.getMessage().equalsIgnoreCase( "!rm" ) ) // remove user
                {                                       
                    message = ( Message ) inObj.readObject(); 
                    String tmpName = message.getUserName();
                    String tmpPass = message.getUserPass();                   
                    
                    Boolean userExists = runServer.delXmlRecord( tmpName, tmpPass );
                    
                    if ( userExists )
                    {
                        outObj.writeObject( new Message( "<RM>" ) );   
                        outObj.flush();            
                        System.out.println( "User is removed");
                    }
                    else
                    {
                        outObj.writeObject( new Message( "<NRM>" ) );
                        outObj.flush();
                        System.out.println( "User is NOT removed");
                    }
                    continue;
                } 
                
                System.out.println( date + status + ip + user + msg );
                outObj.writeObject( new Message( "OK" ) );
                outObj.flush();               
            }
            catch ( ClassNotFoundException ex )
            {
                Logger.getLogger( ClientThread.class.getName() ).log( Level.SEVERE,
                        null, ex );
            }
            catch ( EOFException ex )
            {
                System.out.println( this.clientName  + " closed ..." ); 
                
                runServer.delClient( this );                
                client.close();
                connected = false;
                Server.countUsers--;
              
                //System.out.println( ex.getMessage() );
            }
        }
        
        return connected;
    }    
   
    public void clientMessage( Message message ) throws IOException  
    {    
        
        if ( outObj == null )
        {
            outObj = new ObjectOutputStream( this.client.getOutputStream() );
        } 
        
        outObj.writeObject( message );
        outObj.flush();
    }
 
    @Override
    public void run()
    {            
        boolean clientLive = true;
        
        while ( clientLive && !this.server.isClosed() )
        {
            try
            {
                if ( this.client.isConnected() )
                {
                    System.out.println( clientName + " connected ..." );

                    this.runServer.getMessages( outObj );
                    
                    outObj.flush();
                }
                
                clientLive = loopHandler();
            }
            catch ( IOException | ParserConfigurationException | SAXException | TransformerException ex )
            {
                Logger.getLogger( ClientThread.class.getName() ).log( Level.SEVERE,
                        null, ex );
            }
        }    
    }
}
