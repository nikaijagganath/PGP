package client.ui;

//Project Imports:
import protocol.*;
import client.*;

//Java Imports:
import java.awt.event.*;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.logging.*;
import javax.swing.*;
        
public class ChatFrame extends javax.swing.JFrame implements MessageListener, ServerResponseListener { //UserStatusListener, 
    
    //Instance Variables:
    
    public static Client client;
    
    public static String name;
    
    ChatFrame me = this;
    
    public ChatFrame(Client client, String name) { //String name, String ref, HomeFrame hf)
        //Set up frame:
        initComponents();
        
        this.client = client;
        this.name = name;
        
        //Add listeners and start reader thread:
        try {
            this.client.startReaderThread();
            this.client.addMessageListener(this);
            this.client.addServerResponseListener(this);
        } 
        catch (IOException ex) {
            Logger.getLogger(ChatFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Format JFrame window:
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(exitListener);
        this.setSize(570, 650);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setTitle("Server Chat");
        
        message_txt.setToolTipText("Type message here.");
        
    }
    
    //Logoff and safely exit the program when close button is clicked.
    WindowListener exitListener = new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            try {
                handleLogoff();
                System.exit(0);
            } 
            catch (IOException ex) {
                Logger.getLogger(ChatFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        chat_area = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        message_txt = new javax.swing.JTextArea();
        sendtext_btn = new javax.swing.JButton();
        text_lbl = new javax.swing.JLabel();
        logout_btn = new javax.swing.JButton();
        lblBackground = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(570, 650));

        chat_area.setEditable(false);
        chat_area.setColumns(20);
        chat_area.setRows(5);
        jScrollPane1.setViewportView(chat_area);

        message_txt.setColumns(20);
        message_txt.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        message_txt.setRows(5);
        jScrollPane2.setViewportView(message_txt);

        sendtext_btn.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        sendtext_btn.setText("Send");
        sendtext_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendtext_btnActionPerformed(evt);
            }
        });

        text_lbl.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        text_lbl.setText("Enter message:");

        logout_btn.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        logout_btn.setText("Log Out");
        logout_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logout_btnActionPerformed(evt);
            }
        });

        lblBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/client/ui/back.jpeg"))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(430, 430, 430)
                .addComponent(logout_btn))
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 510, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addGap(410, 410, 410)
                .addComponent(sendtext_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 510, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(text_lbl, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(lblBackground, javax.swing.GroupLayout.PREFERRED_SIZE, 570, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(logout_btn))
            .addGroup(layout.createSequentialGroup()
                .addGap(463, 463, 463)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addGap(563, 563, 563)
                .addComponent(sendtext_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addGap(443, 443, 443)
                .addComponent(text_lbl, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(lblBackground, javax.swing.GroupLayout.PREFERRED_SIZE, 640, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    public void closeChatFrame(){
        this.setVisible(false);
    }
    
    private void sendtext_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendtext_btnActionPerformed
        
        //Send a text message.
        String message = message_txt.getText();
        
        if(message.equals("")){ //Ensure no blank messages are sent.
            JOptionPane.showMessageDialog(null, "Please enter a message", "Warning", JOptionPane.WARNING_MESSAGE);
        }
        else{
            try {
                chat_area.append(String.format("%-15s%10s%n", "me > ", message));//Display message in text area.
                try {
                    handleDirectTextMessageGUI(message);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(ChatFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SignatureException ex) {
                    Logger.getLogger(ChatFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                message_txt.setText("");
                //message= message.replaceAll("(.{0,50})\\b", "$1\n").trim();    //split on line length of 68 chars
            } catch (IOException ex) {
                Logger.getLogger(ChatFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }//GEN-LAST:event_sendtext_btnActionPerformed

    private void logout_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logout_btnActionPerformed
    try {
            handleLogoff();
            System.exit(0);
    } catch (IOException ex) {
            Logger.getLogger(ChatFrame.class.getName()).log(Level.SEVERE, null, ex);
    }
    }//GEN-LAST:event_logout_btnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(() -> {
            new ChatFrame(client, name).setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea chat_area;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblBackground;
    private javax.swing.JButton logout_btn;
    private javax.swing.JTextArea message_txt;
    private javax.swing.JButton sendtext_btn;
    private javax.swing.JLabel text_lbl;
    // End of variables declaration//GEN-END:variables

    /**
     * Sends direct text message to server.
     * @param receiver Client that server must send message to.
     * @param message Text body of message to be sent.
     * @throws java.io.IOException
     */
    public void handleDirectTextMessageGUI(String message) throws IOException, UnsupportedEncodingException, SignatureException {
        Message m = ClientProtocol.createDirectTextMessage(name, message);  //MESSAGE
        try {
            client.sendMessage(m);
        } catch (Exception ex) {
            
        }
    }
    
    
    /**
     * Receives and displays a message in the text area from other clients.
     * @param sender The person that has sent the message.
     * @param messageBody The text/body of the message.
     */
    @Override
    public void onDirectMessage(String sender, String messageBody){
            //messageBody= messageBody.replaceAll("(.{0,50})\\b", "$1\n").trim();    //split on line length of 68 chars
            chat_area.append(String.format("%-15s%10s%n", sender + " > ", messageBody));//Display message in text area.
    }

    @Override
    public void onError(String errCommand, String errType, String errMessage) {
        //ignore
    }
    
    @Override
    public void onResponse(String initialCommand, String respType, String response) {
        //ignore
    }
    
     /**
     * Tells server that user is logging off.
     */
    private void handleLogoff() throws IOException {
        try{
            client.logoff();
        }
        catch(Exception e){
            
        }
    }
}

