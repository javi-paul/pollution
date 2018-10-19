package noneTest;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import api.GUI;
import api.ProtocolHelper;
import api.Tools;
import mbcap.logic.MBCAPText;

/** This class generates the dialog to input the configuration of the MBCAP protocol. */

public class Nonev2ConfigDialog extends JDialog {

  private static final long serialVersionUID = 1L;
  private final JPanel contentPanel = new JPanel();
  private Nonev2ConfigDialogPanel panel;

  public Nonev2ConfigDialog() {
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setLayout(new FlowLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    {
      panel = new Nonev2ConfigDialogPanel();
      contentPanel.add(panel);
    }
    {
      JPanel buttonPane = new JPanel();
      buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      {
        JButton okButton = new JButton(MBCAPText.OK);
        okButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if(Nonev2Helper.isValidProtocolConfiguration(panel)) {
              Nonev2Helper.storeProtocolConfiguration(panel);
              Tools.setProtocolConfigured();
              SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					dispose();
				}
			});
              
            }
          }
        });
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);
      }
    }
    
    //  If the dialog is closed, then it closes the whole program
    this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        dispose();
        System.gc(); // Needed to avoid the error: Exception while removing reference.
        System.exit(0);
      }
    });
    
    GUI.addEscapeListener(this);
    
    this.setTitle(ProtocolHelper.selectedProtocol + " " + MBCAPText.CONFIGURATION);
    this.pack();
    this.setModal(true);
    this.setResizable(false);
    this.setLocationRelativeTo(null);
    this.setVisible(true);
  }
  
}