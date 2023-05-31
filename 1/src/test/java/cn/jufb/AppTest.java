package cn.jufb;

import cn.jufb.webSocket.WebSocketServer;
import cn.jufb.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AppTest {

    @Test
    public void test01() {
        System.out.println(1);
    }

    @Test
    public void sendTest() {
//        User user = new User(666666L, "王依桐");
//        WebSocketServer.sendInfo("奥利给", user);
    }
}
