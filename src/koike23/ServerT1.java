package koike23;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class ServerT1 extends JFrame {
   private JTextField enter;
   private JTextArea display;
   ObjectOutputStream output;
   ObjectInputStream input;
   private Thread threads[]= new Thread[2];

   public ServerT1(){
      super( "ServerT1" );

      Container c = getContentPane();

      enter= new JTextField();
      enter.setEditable(false);
      enter.addActionListener(
          new ActionListener(){
                 public void actionPerformed( ActionEvent ae ){
                   sendData( ae.getActionCommand() );
                 }
              }

      );

      c.add( enter, BorderLayout.NORTH );

      display = new JTextArea();
      c.add( new JScrollPane( display ), BorderLayout.CENTER );



      setSize( 300, 150 );
      setVisible(true);
   }
   
   public static void main ( String args[]) {
       ServerT1 s = new ServerT1();
       s.addWindowListener( new WindowAdapter(){
           public void windowClosing( WindowEvent we ){
               System.exit(0); }

       }

       );
       s.runServer();

   }

   public void runServer() {

      ServerSocket server;
      Socket connection;

      int counter = 1;

      try{
         server = new ServerSocket( 5031, 100 );



         while (true ){
           
             display.setText( "Waiting for connection\n" );
             connection = server.accept();

             display.append( "Connection " + counter + ":  " +
                   " received from: " +
                   connection.getInetAddress().getHostName() );

             output = new ObjectOutputStream( connection.getOutputStream() );
             output.flush();
             input = new ObjectInputStream( connection.getInputStream() );
             display.append( "\nGot I/O Streams\n" );
             String message = "ServerT1>>> Connection successful" ;
             output.writeObject( message );


             output.flush();
             enter.setEditable( true );

             do{
               try{
                  message = (String) input.readObject();
                  display.append( "\n" + message );
                  display.setCaretPosition(
                       display.getText().length() );

               }
               catch( ClassNotFoundException cnfex ){
                  display.append( "\nUnknown object type received" );
               }

             } while ( !message.equals( "CLIENT>>> TERMINATE" ) );

             display.append( "\nUser terminated connection" );
             enter.setEditable( false );
             output.close();
             input.close();
             connection.close();

             ++counter;
         }

        }
        catch( EOFException eof ){
            System.out.println( "Client terminated connection" );
        }

        catch( IOException io ){
            io.printStackTrace();
        }


   }

   private void sendData( String s ){
      try{
         output.writeObject( "ServerT1>>> " + s );
         output.flush();

      }

      catch( IOException cnfex ){
          display.append( "\nError writing object" );

      }

   }

}