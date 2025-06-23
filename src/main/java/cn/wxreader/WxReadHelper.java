package cn.wxreader;

import cn.wxreader.constant.Constant;
import cn.wxreader.domain.User;
import cn.wxreader.enums.WxTaskEnum;
import cn.wxreader.worker.Push;
import cn.wxreader.worker.Read;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WxReadHelper {
    private static final Logger log = LoggerFactory.getLogger(WxReadHelper.class);
    public static void main(String[] args) {
        String userJsonPath = args.length < 1 ? Constant.DEFAULT_USER_JSON_PATH : args[0];
        List<User> users = User.init(userJsonPath);
        ExecutorService executorService = Executors.newFixedThreadPool(users.size());
        for (User user : users) {
            executorService.submit(() -> {
                try {
                    new Read(user).startRead();
                } catch (Exception e) {
                    new Push(user, e.getMessage()).push(WxTaskEnum.READ);
                    log.error("Exception in thread for user: {}", user, e);
                }
            });
        }
        executorService.shutdown();
    }
}