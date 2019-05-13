package client.ui;

//Project Imports:
import time.*;
import client.*;

//Java Imports:
import java.io.*;
import java.util.logging.*;
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
    public LoginFrame() throws IOException {
        //Set up frame:
        initComponents();
        
        //Format JFrame window:
        this.setSize(570, 650);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setTitle("Login");
        
        signup_btn.setToolTipText("Enter a unique username and password into spaces provided before clicking this button");
        
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
        
        catch (IOException ex) {
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
        signup_btn = new javax.swing.JButton();
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

        signup_btn.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        signup_btn.setText("Sign Up");
        signup_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                signup_btnActionPerformed(evt);
            }
        });
        getContentPane().add(signup_btn, new org.netbeans.lib.awtextra.AbsoluteConstraints(82, 534, -1, -1));

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
        
        try {
            if(name.equals("") || password.equals("")) {    //incorrect format(no spaces/@/#)
                username_txt.setText("");
                password_txt.setText("");
                JOptionPane.showMessageDialog(null, "Please enter both username and password.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
            else if(client.login(name, password)) { //username and password exist and match
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
        } 
        
        catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(LoginFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }//GEN-LAST:event_login_btnActionPerformed
    
    /**
     * Attempt to sign up a new user using provided user name and password.
     * @param evt 
     */
    private void signup_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_signup_btnActionPerformed
        String name = username_txt.getText(); //new user's name
        String password = password_txt.getText(); //new user's password
        
        if(name.equals("") || password.equals("")){    //incorrect format - need to give some form of name and password
            username_txt.setText("");
            password_txt.setText("");
            JOptionPane.showMessageDialog(null, "Please enter both username and password.", "Warning", JOptionPane.WARNING_MESSAGE);
        }        
        else {
           try {
                switch (client.addUser(name, password)) {   //attempt to add user to client
                    case constants.Constants.SUCCES_NEW_USER: //successful sign up
                        JOptionPane.showMessageDialog(null, "Sign up succesful. Please log in.");
                        break;
                    case constants.Constants.SYMBOL_NEW_USER_ERR: //incorrect format
                        username_txt.setText("");
                        password_txt.setText("");
                        JOptionPane.showMessageDialog(null, "The following symbols are not allowed: @, # and white space", "Error", JOptionPane.ERROR_MESSAGE);
                        break;
                    case constants.Constants.EXISTS_NEW_USER_ERR: //username exist already
                        username_txt.setText("");
                        password_txt.setText("");
                        JOptionPane.showMessageDialog(null, "Sorry this username exists, try again.","Warning", JOptionPane.WARNING_MESSAGE );
                        break;
                    default:
                        break;
                }
            } 
            
            catch (IOException | ClassNotFoundException ex){
               Logger.getLogger(LoginFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }//GEN-LAST:event_signup_btnActionPerformed

    /**
     * Run the frame.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new LoginFrame().setVisible(true);
            } 
            catch (IOException ex) {
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
    private javax.swing.JButton signup_btn;
    private javax.swing.JLabel username_label;
    private javax.swing.JTextField username_txt;
    // End of variables declaration//GEN-END:variables
}