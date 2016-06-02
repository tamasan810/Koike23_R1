package koike23;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ServerT0 extends JFrame implements Runnable{
	private JTextField enter;
	private JTextArea display;
	ObjectOutputStream output;
	ObjectInputStream input;
	private Thread threads[]= new Thread[2];

	// コンストラクタ
	public ServerT0(){
		super( "ServerT0" );

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
		//  show();
		setVisible(true);
	}

	public static void main ( String args[]) {
		ServerT0 s = new ServerT0();
		s.addWindowListener( new WindowAdapter(){
			public void windowClosing( WindowEvent we ){
				System.exit(0); }

		}

				);

		s.threadsRun(2); // 引数はスレッドの数

	}

	public void threadsRun( int id ){
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

	@Override
	//   public void runServer(int id) {
	public void run() {
		int id = 0;
		Thread currentThread = Thread.currentThread();
		for(int i = 0; i < threads.length; i++){
			if(threads[i] == currentThread) id = i;
		}
		
		ServerSocket serverSocket;
		Socket socket;

		int counter = 1;

		try{
			// サーバソケットの作成
			// サーバソケットは1つでよい
			// ポート番号, 最大受付可能接続数
			serverSocket = new ServerSocket( 5030, 100 );
			
			display.setText("5030が起動しました(port="
					+ serverSocket.getLocalPort() + ")\n");

			while (true ){

				display.setText( "Waiting for connection\n" );
				// ServerSocketはソケット接続待ちの状態になる
				// クライアントが接続してくるまでプログラムはこれ以上先に進まない
				socket = serverSocket.accept();

				// ソケットの情報を表示
				// ソケットの各種情報はgetInetAddress().getHostName()
				// ホストの情報、アドレス情報獲得
				display.append( "Connection " + counter + ":  " +
						" received from: " +
						socket.getInetAddress().getHostName() );

				output = new ObjectOutputStream( socket.getOutputStream() );
				output.flush(); // ストリームをフラッシュする
				input = new ObjectInputStream( socket.getInputStream() );
				display.append( "\nGot I/O Streams\n" );
				String message = "ServerT0>>> Connection successful" ;
				output.writeObject( message ); // オブジェクトにストリームを書き込む

				output.flush(); // ストリームをフラッシュする
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
				enter.setEditable( false ); // 編集不可にする
				output.close();
				input.close();
				socket.close();

				//++counter;
				counter++;
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
		   output.writeObject( "ServerT0>>> " + s );  // オブジェクトにストリームを書き込む
		   output.flush(); // ストリームをフラッシュする

	   }

	   catch( IOException cnfex ){
		   display.append( "\nError writing object" );

	   }

   }

}