package client;

/**
 * @author miemiehoho
 * @date 2022/2/24 21:06
 */
public class BClient {
    public static void main(String[] args) {
        try {
            new ChatClient().startClient("mary");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
