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
public class Record
{
    private int id;
    private String user;
    private String pass;
    
    public void setId( int id )
    {
        this.id = id;
    }
    
    public int getId()
    {
        return this.id;
    }
    
    public void setUser( String user )
    {
        this.user = user;
    }
    
    public void setPass( String pass )
    {
        this.pass = pass;
    }
    
    public String getUser()
    {
        return this.user;
    }
    
    public String getPass()
    {
        return this.pass;
    }
}
