package client.ui;

//Project Imports:
import time.*;
import client.*;

//Java Imports:
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.logging.*;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JOptionPane;

/**
 * Login frame for client of WhatsUp chat application.
 */
public class LoginFrame extends javax.swing.JFrame {
    
    //Instance Variables:
    
    public Client client;
    
    //Constructor:
    
    /**
     * Constructs login frame.
     * @throws IOException 
     */
    public LoginFrame() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException {
        //Set up frame:
        initComponents();
        
        //Format JFrame window:
        this.setSize(570, 650);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setTitle("Login");
        
        //Create client and add all listeners to client:
        try {
            client = new Client(constants.Constants.SERVER_NAME, constants.Constants.SERVER_PORT_NUM);
            
            //To get response as to correct or incorrect login:
            client.addServerResponseListener(new ServerResponseListener() {
                    @Override
                    public void onError(String errCommand, String errType, String errMessage) {
                        System.out.println("Error: " + errCommand + ". " + errType + " - " + errMessage + DateTime.getDateTime());
                    }

                    @Override
                    public void onResponse(String initialCommand, String respType, String response) {
                        System.out.println("Server Response: " + initialCommand + " - " + respType + " - " + response  + DateTime.getDateTime());
                    }
                });
        
        } 
        
        catch (IOException | ClassNotFoundException ex) {
            System.out.println("Connection Error: Couldn't connect to " + constants.Constants.SERVER_NAME + " on port " + constants.Constants.SERVER_PORT_NUM + ".");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        heading = new javax.swing.JLabel();
        username_label = new javax.swing.JLabel();
        password_label = new javax.swing.JLabel();
        username_txt = new javax.swing.JTextField();
        password_txt = new javax.swing.JPasswordField();
        login_btn = new javax.swing.JButton();
        lblBackground = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        heading.setFont(new java.awt.Font("Trebuchet MS", 1, 48)); // NOI18N
        heading.setText("Connect to Server");
        getContentPane().add(heading, new org.netbeans.lib.awtextra.AbsoluteConstraints(82, 74, 429, 124));

        username_label.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        username_label.setText("Username:");
        getContentPane().add(username_label, new org.netbeans.lib.awtextra.AbsoluteConstraints(82, 284, -1, 20));

        password_label.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        password_label.setText("Password:");
        getContentPane().add(password_label, new org.netbeans.lib.awtextra.AbsoluteConstraints(82, 414, -1, -1));
        getContentPane().add(username_txt, new org.netbeans.lib.awtextra.AbsoluteConstraints(192, 284, 290, -1));
        getContentPane().add(password_txt, new org.netbeans.lib.awtextra.AbsoluteConstraints(192, 414, 290, -1));

        login_btn.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        login_btn.setText("Login");
        login_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                login_btnActionPerformed(evt);
            }
        });
        getContentPane().add(login_btn, new org.netbeans.lib.awtextra.AbsoluteConstraints(382, 534, 101, -1));

        lblBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/client/ui/back.jpeg"))); // NOI18N
        getContentPane().add(lblBackground, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 570, 650));

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    /**
     * Attempt to login user using provided user name and password.
     * @param evt 
     */
    private void login_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_login_btnActionPerformed
        String name= username_txt.getText(); //User's name
        String password= password_txt.getText(); //User's password
        if(name.equals("") || password.equals("")) {
            username_txt.setText("");
            password_txt.setText("");
            JOptionPane.showMessageDialog(null, "Please enter both username and password.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
        else try {
            if(client.login(name, password)) { //username and password exist and match
                this.setVisible(false);
                ChatFrame hf = new ChatFrame(client, name);
                hf.setVisible(true);
                this.dispose();
            }
            else {  //username and password either don't exist or don't match
                username_txt.setText("");
                password_txt.setText("");
                JOptionPane.showMessageDialog(null, "Incorrect username and/or password. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            Logger.getLogger(LoginFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(LoginFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }//GEN-LAST:event_login_btnActionPerformed
    
    /**
     * Run the frame.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new LoginFrame().setVisible(true);
            } 
            catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException ex) {
                Logger.getLogger(LoginFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel heading;
    private javax.swing.JLabel lblBackground;
    private javax.swing.JButton login_btn;
    private javax.swing.JLabel password_label;
    private javax.swing.JPasswordField password_txt;
    private javax.swing.JLabel username_label;
    private javax.swing.JTextField username_txt;
    // End of variables declaration//GEN-END:variables
}
