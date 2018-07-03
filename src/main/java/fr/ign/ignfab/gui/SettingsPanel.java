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
  private JRadioButton noButton;
  private JRadioButton yesButton;
  private JTextField proxyHostField;
  private JTextField proxyPortField;
  private JTextField distanceField;
  
  /**
   * Default constructor.
   */
  public SettingsPanel() {
    
    FormLayout layout = new FormLayout(
        "4dlu, pref, 4dlu, pref, 4dlu, pref, pref, 4dlu",
        "4dlu, pref, 10dlu, pref, pref, pref, 10dlu, pref, 4dlu");
    this.setLayout(layout);
    CellConstraints cc = new CellConstraints();
    
    // =========================================================================
    //   Geoportail key. 
    add(new JLabel("Input Geoportail key: ", SwingConstants.RIGHT), cc.xy(2, 2));
    keyField = new JTextField(30);
    add(keyField, cc.xyw(4, 2, 4));
    
    
    // =========================================================================
    //   Proxy. 
    add(new JLabel("Proxy: ", SwingConstants.RIGHT), cc.xy(2, 4));
    
    noButton = new JRadioButton("No");
    noButton.setMnemonic(KeyEvent.VK_B);
    noButton.setActionCommand("No");
    noButton.setSelected(true);
    noButton.addActionListener(this);
    
    yesButton = new JRadioButton("Yes");
    yesButton.setMnemonic(KeyEvent.VK_B);
    yesButton.setActionCommand("Yes");
    yesButton.addActionListener(this);
    
    ButtonGroup group = new ButtonGroup();
    group.add(noButton);
    group.add(yesButton);
    
    add(noButton, cc.xy(4, 4));
    add(yesButton, cc.xy(4, 5));
    
    // --
    
    add(new JLabel("Host: ", SwingConstants.RIGHT), cc.xy(6, 5));
    proxyHostField = new JTextField(20);
    proxyHostField.setEnabled(false);
    add(proxyHostField, cc.xy(7, 5));
    
    add(new JLabel("Port: ", SwingConstants.RIGHT), cc.xy(6, 6));
    proxyPortField = new JTextField(5);
    proxyPortField.setEnabled(false);
    add(proxyPortField, cc.xy(7, 6));
    
    // =========================================================================
    //   Distance.
    
    add(new JLabel("Distance: ", SwingConstants.RIGHT), cc.xy(2, 8));
    distanceField = new JTextField(25);
    add(distanceField, cc.xyw(4, 8, 3));
  }
  
  
  /**
   * 
   */
  public void actionPerformed(ActionEvent evt) {
    Object source = evt.getSource();
    if (source == noButton) {
      proxyHostField.setEnabled(false);
      proxyPortField.setEnabled(false);
    }
    if (source == yesButton) {
      proxyHostField.setEnabled(true);
      proxyPortField.setEnabled(true);
    }
  }
  
  
  public String getKey() {
    return this.keyField.getText();
  }
  
  
  public String getDistance() {
    return this.distanceField.getText();
  }

}
