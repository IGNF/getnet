/*******************************************************************************
 * 
 * @copyright IGN - 2018
 * 
 * This software is released under the licence CeCILL
 * see <a href="https://fr.wikisource.org/wiki/Licence_CeCILL_version_2">https://fr.wikisource.org/wiki/Licence_CeCILL_version_2</a>
 *
 ******************************************************************************/
package fr.ign.ignfab.util;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * 
 * 
 *
 */
public class AuthIGNGeoportal extends Authenticator {
    
    private String username;
    private String password;
    
    public AuthIGNGeoportal(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    protected PasswordAuthentication getPasswordAuthentication() {
      return new PasswordAuthentication(username, password.toCharArray());
    }
  

}
