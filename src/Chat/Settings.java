/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chat;

/**
 *
 * @author hexfox
 */
public class Settings
{
    private int serverPort;
    private int maxCountMessages;
    private int maxCountClients;
    
    public String getAllSettings()
    {
        return "Port: " + this.serverPort + "\n"
                + "Count messages: " + this.maxCountMessages + "\n"
                + "Max count clients: " + this.maxCountClients;
    }
    
    public void setServerPort( int serverPort )
    {
        this.serverPort = serverPort;
    }
    
    public void setMaxCountMessages( int maxCountMessages )
    {
        this.maxCountMessages = maxCountMessages;
    }
    
    public void setMaxCountClients( int maxCountClients )
    {
        this.maxCountClients = maxCountClients;
    }
    
    public int getServerPort()
    {
        return this.serverPort;
    }
    
    public int getMaxCountMessages()
    {
        return this.maxCountMessages;
    }
    
    public int getMaxCountClients()
    {
        return this.maxCountClients;
    }
}
