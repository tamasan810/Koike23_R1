package koike23;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class ServerT0 extends JFrame implements Runnable{
	private final int NUMBER = 2;
	private JTextField enter;
	private JTextArea display;
	ObjectOutputStream output[] = new ObjectOutputStream[NUMBER];
	ObjectInputStream input[] = new ObjectInputStream[NUMBER];
//	ObjectOutputStream output;
//	ObjectInputStream input;
	private final int PORT_NUMBER = 5030;
	int count_Client = 0;
	private Socket sockets[] = new Socket[NUMBER];
	private Thread threads[]= new Thread[NUMBER];
	static int testN;

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

		});

		s.runServer();

	}

	public void runServer() {
		ServerSocket serverSocket = null;

		try{
			// サーバソケットの作成
			// サーバソケットは1つでよい
			// ポート番号, 最大受付可能接続数
			serverSocket = new ServerSocket(PORT_NUMBER, 100);

			display.setText(PORT_NUMBER +"が起動しました(port="
					+ serverSocket.getLocalPort() + ")\n");

			while (true ){
				display.append( "Waiting for connection\n" );
				// ServerSocketはソケット接続待ちの状態になる
				// クライアントが接続してくるまでプログラムはこれ以上先に進まない
				// ソケットをスレッドに渡す必要がある。
				sockets[count_Client] = serverSocket.accept();
				++count_Client;				
				threads[count_Client - 1] = new Thread(this);
				threads[count_Client - 1].start();
			}

		}
		// 入力の途中で、予想外のストリームの終了があったことを表すシグナル
		catch( EOFException eof ){
			System.out.println( "Client terminated connection" );
		}

		catch( IOException io ){
			io.printStackTrace();
		}

	}

	private void sendData( String s ){
		try{
			output[count_Client].writeObject( "ServerT0>>> " + s );  // オブジェクトにストリームを書き込む
			output[count_Client].flush(); // ストリームをフラッシュする

		}

		catch( IOException cnfex ){
			display.append( "\nError writing object" );

		}

	}

	@Override
	public void run() {
		int id =0;
		Thread currentThread = Thread.currentThread();
		for ( int i = 0; i< threads.length; i++){
			if( threads[i] == currentThread ) id = i;
		}
		
		Socket socket = sockets[id];

		try{
			// ソケットの情報を表示
			// ソケットの各種情報はgetInetAddress().getHostName()
			// ホストの情報、アドレス情報獲得
			display.append( "Connection " + (count_Client - 1) + ":  " +
					" received from: " +
					socket.getInetAddress().getHostName() );

			output[count_Client - 1] = new ObjectOutputStream( socket.getOutputStream() );
			output[count_Client - 1].flush(); // ストリームをフラッシュする
			input[count_Client - 1] = new ObjectInputStream( socket.getInputStream() );
			display.append( "\nGot I/O Streams\n" );
			String message = "ServerT0>>> Connection successful" ;
			output[count_Client - 1].writeObject( message ); // オブジェクトにストリームを書き込む

			output[count_Client - 1].flush(); // ストリームをフラッシュする
			enter.setEditable( true );
			//count_Client++; // このタイミングダメ

			do{
				try{
					// message = ...で止まっている
					System.out.println(testN); // 接続した段階で表示される
					message = (String) input[testN].readObject(); // エラー
					display.append("id = " +String.valueOf(id)+ message + "\n");
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
			output[testN].close();
			input[testN].close();
			socket.close();
		}
		catch( EOFException eof ){
			System.out.println( "Client terminated connection" );
		}

		catch( IOException io ){
			io.printStackTrace();
		}

	}

}