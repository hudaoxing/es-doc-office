package cn.joylau.code.task;

import cn.joylau.code.service.FTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author liuf@ahtsoft.cn (刘法)
 * Description: cn.joylau.code.task
 * Date: 2018-7-31
 * Company: 合肥安慧软件有限公司
 * Copyright: Copyright (c) 2018
 */
@Component
public class FTPTask {
    @Autowired
    private FTPService ftpService;
    @Scheduled(fixedRate = 5000 )
    public void keepAlive(){
        try {
            ftpService.getFtpClient().sendCommand("pwd");
        } catch (IOException e) {
            // ignore
        }
    }
}
