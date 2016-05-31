package koike22;

import java.io.IOException;

public class Main {
    public static void main(String args[]) {
        try {
        	// ポート番号8888
            new MiniServer(8888).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
