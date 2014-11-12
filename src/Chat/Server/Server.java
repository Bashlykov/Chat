/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chat.Server;

import Chat.Settings;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 *
 * @author hexfox
 */
 
public class Server
{
    /**
     * @throws java.io.IOException
     */
    public static int countUsers;
    private static final String FILE_PATH_CONFIG = "src/Chat/config.xml";
    
    /* Create default config file with settings */
    private static void createConfigFile() throws XMLStreamException, IOException
    {        
        File file = new File( FILE_PATH_CONFIG );
        if ( !file.exists() ) 
        {
            XMLOutputFactory outFactory = XMLOutputFactory.newInstance(); 
            XMLStreamWriter writer = outFactory.createXMLStreamWriter( new FileWriter( FILE_PATH_CONFIG ) );

            writer.writeStartDocument();
                writer.writeStartElement( "ServerSettings" );

                        writer.writeStartElement( "serverPort" );
                            writer.writeCharacters( "1234" );
                        writer.writeEndElement();

                        writer.writeStartElement( "maxCountMessages" );
                            writer.writeCharacters( "10" );
                        writer.writeEndElement();

                        writer.writeStartElement( "maxCountClients" );
                            writer.writeCharacters( "10" );
                        writer.writeEndElement();

                writer.writeEndElement();
            writer.writeEndDocument();

            writer.flush();
            writer.close();
        }
    }
    
    private static void readConfigFile( Settings settings ) throws XMLStreamException, FileNotFoundException
    {
        String text = null;
        
        XMLInputFactory inFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = 
                inFactory.createXMLStreamReader( new FileInputStream( new File( FILE_PATH_CONFIG ) ) );         
        
        while ( reader.hasNext() )
        {
            int event = reader.next();
            
            switch( event )
            { 
                case XMLStreamConstants.CHARACTERS:
                {
                    text = reader.getText().trim();
                    break;
                }
                case XMLStreamConstants.END_ELEMENT: 
                {
                    switch ( reader.getLocalName() )
                    {
                        case "serverPort": 
                            settings.setServerPort( Integer.parseInt( text ) );
                            break;
                        case "maxCountMessages": 
                            settings.setMaxCountMessages( Integer.parseInt( text ) );
                            break;
                        case "maxCountClients": 
                            settings.setMaxCountClients( Integer.parseInt( text ) );
                            break;                            
                    }
                    break;
                }
            }
        }
    }
        
    @SuppressWarnings( "fallthrough" )
    public static void main( String[] args ) throws IOException, XMLStreamException, ParserConfigurationException, SAXException, TransformerException
    {                              
        Server.createConfigFile();
       
        Settings settings = new Settings();
        
        Server.readConfigFile( settings );
        
        System.out.println( settings.getAllSettings() );
        
        RunServer runServer = new RunServer( settings.getServerPort(), settings.getMaxCountMessages() );
        ServerSocket server = runServer.getServerSocket();
        countUsers = 0;
        
        while( true )
        {
            Socket client = server.accept();
            if ( Server.countUsers < settings.getMaxCountClients() )
            {
                runServer.addClient( new ClientThread( runServer, client )  );
            }
            else
            {
                System.out.println( "Max connected users = " + settings.getMaxCountClients() );
                client.close();
            }
        }
    }
    
}
