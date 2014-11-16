/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chat.Server;

import Chat.Message;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author hexfox
 */
public class RunServer
{
    private int port;      
    private ServerSocket server;  
    private ArrayList < ClientThread > listClients;
    private LinkedBlockingDeque < String > poolMessages;
    private int maxCountMessages;
    private final String FILE_PATH_CLIENTS = "src/Chat/accounts.xml";
    

    public RunServer( int port, int maxCountMessages )
    {
        this.listClients = new ArrayList<>();
        this.poolMessages = new LinkedBlockingDeque<>();
        
        try
        {
            this.port = port;
            
            this.server = new ServerSocket( this.port );
            
            this.maxCountMessages = maxCountMessages;
            
            System.out.println( "Server is run ..." );                      
        }
        catch ( IOException ex )
        {
            Logger.getLogger( RunServer.class.getName() ).log( Level.SEVERE,
                    null, ex );
        }              
    }
    
    public ServerSocket getServerSocket()
    {
        return this.server;
    }
    
    public void addClient( ClientThread newClient )
    {
        this.listClients.add( newClient );
    }
 
    public boolean delClient( ClientThread obj )
    {
        return this.listClients.remove( obj );
    } 
            
    public ClientThread getClient( int indx )
    {
        return this.listClients.get( indx );
    }
        
    public int getCountClients()
    {
        return this.listClients.size();
    }
        
    public void broadcastMessage( Message message ) throws IOException // message to all clients 
    {
        for( ClientThread client : this.listClients ) //this.listClients.stream().forEach( ( client ) -> {...} );
        {
            client.clientMessage( message );
        } 
    }
    
    public void addMessage( Message message ) // add message for output for new connecting client 
    {
        String date;
        String ip;
        String user;
        String msg;
        String status;
        
        user = message.getUserName() + ": "; 
        date = message.getDate() != null ? message.getDate() : "";
        ip = message.getIp() != null ? message.getIp() + "@" : "";
        msg = message.getMessage() != null ? message.getMessage() : "";  
        status = message.getUserStatus() != null ? "[" + message.getUserStatus() + "]" : "";
        
        poolMessages.addLast( date + status + ip + user + msg );
        
        if ( poolMessages.size() > this.maxCountMessages )
        {
            poolMessages.removeFirst();
        }
    }
    
    public void getMessages( ObjectOutputStream out ) throws IOException // output last 10 messages for new client //PrintWriter
    {
        Iterator < String > iter = poolMessages.iterator();
        
        out.writeObject( new Message( "Hello!\nNumber of connected clients: "
                                    + this.getCountClients() ) );
        
        if ( poolMessages.size() > 0 )
        {
            //out.writeObject( new Message( "size = " + poolMessages.size() ) );
            
            while( iter.hasNext() )
            {
                String msg = iter.next();
                out.writeObject( new Message( msg ) );
            }
        }
    }
    
    public void appendXmlRecord( int index, String userName, String userPass ) throws ParserConfigurationException, SAXException, IOException, TransformerException
    {
        DocumentBuilderFactory docBuildFact = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuild = docBuildFact.newDocumentBuilder();

        Document document = docBuild.parse( FILE_PATH_CLIENTS );
        
        Node accounts = document.getElementsByTagName( "accounts" ).item( 0 );       
        Element record = document.createElement( "record" );
        record.setAttribute( "id", Integer.toString( index ) );
        accounts.appendChild( record );
           
        Element user = document.createElement( "user" );
        user.appendChild( document.createTextNode( userName ) );

        Element pass = document.createElement( "password" );
        pass.appendChild( document.createTextNode( userPass ) );
        
        record.appendChild( user );    
        record.appendChild( pass );

        DOMSource source = new DOMSource( document );
        StreamResult result = new StreamResult( new File( FILE_PATH_CLIENTS ) );
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform( source, result );
    }
    
    public String getXmlRecordLastId() throws SAXException, IOException, ParserConfigurationException
    {
        DocumentBuilderFactory docBuildFact = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuild = docBuildFact.newDocumentBuilder();

        Document document = docBuild.parse( FILE_PATH_CLIENTS );        
        
        Node accounts = document.getElementsByTagName( "accounts" ).item( 0 );
            
        Node record = accounts.getLastChild();
        NamedNodeMap attr = record.getAttributes();  
        Node nodeAttr;
        
        try
        {
            nodeAttr = attr.getNamedItem( "id" );
        }
        catch( java.lang.NullPointerException ex )
        {
            return "-1";
        }
        
        return nodeAttr.getTextContent();
    }
    
    public Boolean checkXmlRecord( String userName, String userPass ) throws SAXException, IOException, ParserConfigurationException
    {
        DocumentBuilderFactory docBuildFact = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuild = docBuildFact.newDocumentBuilder();

        Document document = docBuild.parse( FILE_PATH_CLIENTS );        
        
        NodeList recordList = document.getElementsByTagName( "record" );
        NodeList userList = document.getElementsByTagName( "user" );
        NodeList passList = document.getElementsByTagName( "password" );
        
        for ( int i = 0; i < recordList.getLength(); i++ )
        {
            Node userNode = userList.item( i );           
            Node passNode = passList.item( i );
            
            if ( userName.equals( userNode.getTextContent() ) 
                    && userPass.equals( passNode.getTextContent() ) )
            {
                return true;
            }
        }    
        return false;        
    }

    public Boolean editXmlRecord( String userName, String userOldPass, String userNewPass ) throws SAXException, IOException, ParserConfigurationException, TransformerException
    {
        DocumentBuilderFactory docBuildFact = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuild = docBuildFact.newDocumentBuilder();

        Document document = docBuild.parse( FILE_PATH_CLIENTS );        
        
        NodeList recordList = document.getElementsByTagName( "record" );
        NodeList userList = document.getElementsByTagName( "user" );
        NodeList passList = document.getElementsByTagName( "password" );
        
        for ( int i = 0; i < recordList.getLength(); i++ )
        {
            Node userNode = userList.item( i );           
            Node passNode = passList.item( i );
            
            if ( userName.equals( userNode.getTextContent() ) 
                    && userOldPass.equals( passNode.getTextContent() ) )
            {
                passNode.setTextContent( userNewPass );
                
                DOMSource source = new DOMSource( document );
                StreamResult result = new StreamResult( new File( FILE_PATH_CLIENTS ) );

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.transform( source, result );
                
                return true;
            }
        }    
        return false; 
    }
        
    public Boolean delXmlRecord( String userName, String userPass ) throws SAXException, IOException, ParserConfigurationException, TransformerConfigurationException, TransformerException
    {
        DocumentBuilderFactory docBuildFact = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuild = docBuildFact.newDocumentBuilder();

        Document document = docBuild.parse( FILE_PATH_CLIENTS );        
        
        Node accounts = document.getElementsByTagName( "accounts" ).item( 0 );
        NodeList recordList = document.getElementsByTagName( "record" );
        NodeList userList = document.getElementsByTagName( "user" );
        NodeList passList = document.getElementsByTagName( "password" );
        
        for ( int i = 0; i < recordList.getLength(); i++ )
        {
            Node userNode = userList.item( i );           
            Node passNode = passList.item( i );
            
            try
            {
                if ( userName.equals( userNode.getTextContent() ) 
                        && userPass.equals( passNode.getTextContent() ) )
                {
                    Node record = recordList.item( i );
                    accounts.removeChild( record );
                    
                    DOMSource source = new DOMSource( document );
                    StreamResult result = new StreamResult( new File( FILE_PATH_CLIENTS ) );

                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    transformer.transform( source, result );
                    
                    return true;
                }
            }
            catch( java.lang.NullPointerException ex )
            {
                continue;
            }
        }       
        return false; 
    }
}
