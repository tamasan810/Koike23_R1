package koike23;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class ClientT1 extends JFrame implements Runnable {

	private JTextField enter[] = new JTextField[2];
	private JTextArea display;
	private JPanel enterPanel;
	private ObjectOutputStream output[] = new ObjectOutputStream[2];
	private ObjectInputStream input[] = new ObjectInputStream[2];
	private Socket clientSocket[]= new Socket[2];
	//private Socket client;
	private String message = "";
	private Thread threads[]= new Thread[2];

	// コンストラクタ
	public ClientT1(){
		super ( "Clinent Window" );

		Container c = getContentPane();
		c.setLayout( new BorderLayout() );

		enter[0] = new JTextField("Enter your message to S0",20);
		enter[0].setEditable( false ); // 編集不可
		enter[0].addActionListener( // 文字入力をしてEnterキーを押したとき
				new ActionListener(){
					public void actionPerformed( ActionEvent ae) {
						sendData( ae.getActionCommand(), 0);     
					}
				}    
				);

		enter[1] = new JTextField("Enter your message to S1",20);
		enter[1].setEditable( false );
		enter[1].addActionListener(
				new ActionListener(){
					public void actionPerformed( ActionEvent ae){
						sendData( ae.getActionCommand(), 1);
					}
				}
				);

		enterPanel = new JPanel();
		enterPanel.setLayout(
				new GridLayout( enter.length, 1) );
		for ( int i=0; i< enter.length; i++){
			enterPanel.add(enter[i]);
		}

		c.add( enterPanel, BorderLayout.NORTH );

		display = new JTextArea(20,10); // 行数, 列数

		c.add ( new JScrollPane( display ), BorderLayout.CENTER );

		setSize( 300, 300 );

		// show();
		setVisible( true );

	}

	public static void main( String args[] ){

		ClientT1 c = new ClientT1();
		c.addWindowListener( new WindowAdapter(){
			public void windowClosing( WindowEvent we ){
				System.exit(0); }

		}
				);

		c.threadsRun(2); // 引数はスレッドの数    
	}

	public void threadsRun( int id ){
		display.setText("ClientT1 start\n");
		for ( int i=0; i< id; i++) threads[i] = new Thread(this);
		for ( int i=0; i< id; i++) threads[i].start();

		try{
			// 全てのスレッドが終了するのを待つ
			for ( int i=0; i<id; i++) threads[i].join();
		}
		catch(Exception e){
			System.exit( 1 );		
		}
		System.out.println( "ClientTreead Terminated");
		System.exit( 0 );

	}

	public void run(){
		int id =0;
		Thread currentThread = Thread.currentThread();
		for ( int i = 0; i< threads.length; i++){
			if( threads[i] == currentThread ) id = i;
		}

		try{
			display.append( "Attempting connection to server" + id + "\n" );
			// Socketのインスタンスを作成
			// ローカルホストへのソケット接続(接続先を指定)
			// Socket connection = new Socket(サーバアドレス, ポート番号);
			clientSocket[ id ] = new Socket( InetAddress.getLocalHost(), (5030 + id) );
			// IPアドレス指定のソケット接続
			//client = new Socket(InetAddress.getByName( "133.25.83.140" ), 5030 );

			display.append( "Connected to: " +
					clientSocket[id].getInetAddress().getHostName() +"\n");

			output[id] = new ObjectOutputStream( clientSocket[id].getOutputStream() );
			output[id].flush();

			input[id] = new ObjectInputStream( clientSocket[id].getInputStream() );

			display.append( "\nGot I/O streams\n" );


			try{
				message = (String) input[id].readObject();

				display.append( "\n" + message );
				display.setCaretPosition(
						display.getText().length()  );

			}
			catch( ClassNotFoundException cnfex ){
				display.append(
						"\nUnknown object type received" );

			}

			enter[id].setEditable( true ); // 編集を許可する

			do{
				try{
					message = (String) input[id].readObject();

					display.append( "\n" + message );
					display.setCaretPosition(
							display.getText().length()  );


				}
				catch( ClassNotFoundException cnfex ){
					display.append(
							"\nUnknown object type received" );

				}
				catch( SocketException se ){
					System.out.println( "Server" +id + " terminated connection" );
					break;
				}

			} while ( !message.equals( "SERVER>>> TERMINATE" ) );

			display.append( "Closing connection.\n" );
			enter[id].setEditable( false );
			input[id].close();
			output[id].close();
			clientSocket[id].close();


		}
		catch( EOFException eof ){
			System.out.println( "Server terminated connection" );
		}
		catch( IOException e ) {
			e.printStackTrace();
		}
	}


	private void sendData( String s, int id){
		if(id == 0){
			ServerT0.testN = id;			
		} else{
			ServerT1.testN = id;			
		}
		try{
			output[id].writeObject( "CLIENT>>> " + s );

			output[id].flush();
			display.append( "\nCLIENT>>> " + s );
			if( s.equals( "TERMINATE") ){
				display.append( "\n You terminated connection to Server" + id );
				// TERMINATEしたテキストフィールドは編集不可にする
				enter[id].setEditable( false );
				output[id].close();
				input[id].close();
				clientSocket[id].close();

			}

		}
		catch( SocketException se ){
			System.out.println( "Server" +id + " terminated connection" );
		}
		catch( IOException cnfex ){
			display.append(
					"\nError writing object" );
		}
	}

}