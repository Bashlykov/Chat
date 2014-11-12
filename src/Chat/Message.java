/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chat;

import java.io.Serializable;

/**
 *
 * @author hexfox
 */
public class Message implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String date = null;
    private String userName = null;
    private String userPass = null;
    private String userOldPass = null;
    private String userIp = null;
    private String message = null;
    private String userStatus = null;
    
    public Message( String date, 
                    String userIp, 
                    String userName, 
                    String message, 
                    String userStatus )
    {
        this.date = date;
        this.userIp = userIp;
        this.userName = userName;       
        this.message = message;
        this.userStatus = userStatus;
    }

    public Message()
    {
       
    }
        
    public Message( String message )
    {
        this.message = message;
    }
 
    public void setUserName( String userName )
    {
        this.userName = userName;
    }
 
    public void setUserPass( String userPass )
    {
        this.userPass = userPass;
    }
    
    public void setOldUserPass( String userOldPass )
    {
        this.userOldPass = userOldPass;
    }   
    
    public String getDate()
    {
        return  this.date;
    }        
    
    public String getIp()
    {
        return this.userIp;
    }
        
    public String getUserName()
    {
        return this.userName;
    }

    public String getUserPass()
    {
        return this.userPass;
    }
    
    public String getOldUserPass()
    {
        return this.userOldPass;
    }  
    
    public String getMessage()
    {
        return this.message;
    }
    
    public String getUserStatus()
    {
        return this.userStatus;
    }
 }

