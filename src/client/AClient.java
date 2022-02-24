package client;

/**
 * @author miemiehoho
 * @date 2022/2/24 21:06
 */
public class AClient {
    public static void main(String[] args) {
        try {
            new ChatClient().startClient("lucy");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
