/*******************************************************************************
 * 
 * @copyright IGN - 2018
 * 
 * This software is released under the licence CeCILL
 * see <a href="https://fr.wikisource.org/wiki/Licence_CeCILL_version_2">https://fr.wikisource.org/wiki/Licence_CeCILL_version_2</a>
 *
 ******************************************************************************/
package fr.ign.ignfab.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Settings panel.
 * 
 * @author M.-D. Van Damme
 */
public class SettingsPanel extends JPanel implements ActionListener {

  /** Default serial version id. */
  private static final long serialVersionUID = 1L;
  
  /** Settings component. */
  private JTextField keyField;
  private JRadioButton noAuthButton;
  private JRadioButton yesAuthButton;
  private JTextField userField;
  private JTextField passwdField;
  
  private JRadioButton noProxyButton;
  private JRadioButton yesProxyButton;
  private JTextField proxyHostField;
  private JTextField proxyPortField;

  
  /**
   * Default constructor.
   */
  public SettingsPanel(String key, boolean hasProxy, String proxyHost, String proxyPort, boolean hasAuth, String username, String passwd) {
    
    FormLayout layout = new FormLayout(
        "4dlu, pref, 4dlu, pref, 4dlu, pref, pref, 4dlu",
        "4dlu, pref, 4dlu, pref, pref, pref, 20dlu, pref, pref, pref, 10dlu");
    this.setLayout(layout);
    CellConstraints cc = new CellConstraints();
    
    // =========================================================================
    //   Geoportail key. 
    add(new JLabel("Input Geoportail key: ", SwingConstants.RIGHT), cc.xy(2, 2));
    keyField = new JTextField(30);
    keyField.setText(key);
    add(keyField, cc.xyw(4, 2, 4));
    
    add(new JLabel("Auth with key: ", SwingConstants.RIGHT), cc.xy(2, 4));
    
    noAuthButton = new JRadioButton("No");
    noAuthButton.setMnemonic(KeyEvent.VK_B);
    noAuthButton.setActionCommand("No");
    noAuthButton.addActionListener(this);
    
    yesAuthButton = new JRadioButton("Yes");
    yesAuthButton.setMnemonic(KeyEvent.VK_B);
    yesAuthButton.setActionCommand("Yes");
    yesAuthButton.addActionListener(this);
    
    ButtonGroup groupAuth = new ButtonGroup();
    groupAuth.add(noAuthButton);
    groupAuth.add(yesAuthButton);
    
    add(noAuthButton, cc.xy(4, 4));
    add(yesAuthButton, cc.xy(4, 5));
    
    add(new JLabel("Username: ", SwingConstants.RIGHT), cc.xy(6, 5));
    userField = new JTextField(20);
    userField.setText(username);
    add(userField, cc.xy(7, 5));
    
    add(new JLabel("Password: ", SwingConstants.RIGHT), cc.xy(6, 6));
    passwdField = new JTextField(5);
    passwdField.setText(passwd);
    add(passwdField, cc.xy(7, 6));
    
    if (hasAuth) {
        yesAuthButton.setSelected(true);
        userField.setEnabled(true);
        passwdField.setEnabled(true);
    } else {
        noAuthButton.setSelected(true);
        userField.setEnabled(false);
        passwdField.setEnabled(false);
    }
    
    // =========================================================================
    //   Proxy. 
    add(new JLabel("Proxy: ", SwingConstants.RIGHT), cc.xy(2, 8));
    
    noProxyButton = new JRadioButton("No");
    noProxyButton.setMnemonic(KeyEvent.VK_B);
    noProxyButton.setActionCommand("No");
    noProxyButton.addActionListener(this);
    
    yesProxyButton = new JRadioButton("Yes");
    yesProxyButton.setMnemonic(KeyEvent.VK_B);
    yesProxyButton.setActionCommand("Yes");
    yesProxyButton.addActionListener(this);
    
    ButtonGroup groupProxy = new ButtonGroup();
    groupProxy.add(noProxyButton);
    groupProxy.add(yesProxyButton);
    
    add(noProxyButton, cc.xy(4, 8));
    add(yesProxyButton, cc.xy(4, 9));
    
    // --
    
    add(new JLabel("Host: ", SwingConstants.RIGHT), cc.xy(6, 9));
    proxyHostField = new JTextField(20);
    proxyHostField.setText(proxyHost);
    add(proxyHostField, cc.xy(7, 9));
    
    add(new JLabel("Port: ", SwingConstants.RIGHT), cc.xy(6, 10));
    proxyPortField = new JTextField(5);
    proxyPortField.setText(proxyPort);
    add(proxyPortField, cc.xy(7, 10));
    
    if (hasProxy) {
        yesProxyButton.setSelected(true);
        proxyHostField.setEnabled(true);
        proxyPortField.setEnabled(true);
    } else {
        noProxyButton.setSelected(true);
        proxyHostField.setEnabled(false);
        proxyPortField.setEnabled(false);
    }
    
    
  }
  
  
  /**
   * 
   */
  public void actionPerformed(ActionEvent evt) {
      Object source = evt.getSource();
      if (source == noProxyButton) {
          proxyHostField.setEnabled(false);
          proxyPortField.setEnabled(false);
      }
      if (source == yesProxyButton) {
          proxyHostField.setEnabled(true);
          proxyPortField.setEnabled(true);
      }
    
      if (source == noAuthButton) {
          userField.setEnabled(false);
          passwdField.setEnabled(false);
      }
      if (source == yesAuthButton) {
          userField.setEnabled(true);
          passwdField.setEnabled(true);
      }
  }
  
  
  public String getKey() {
    return this.keyField.getText();
  }
  
  
  public Boolean hasProxy() {
    if (yesProxyButton.isSelected()) {
      return true;
    } else {
      return false;
    }
  }
  
  
  public String getProxyHost() {
    return proxyHostField.getText();
  }
  
  public String getProxyPort() {
    return proxyPortField.getText();
  }
  
  public Boolean hasAuth() {
      if (yesAuthButton.isSelected()) {
          return true;
      } else {
          return false;
      }
  }

  public String getUser() {
      return userField.getText();
  }
    
  public String getPasswd() {
      return passwdField.getText();
  }
  
}
