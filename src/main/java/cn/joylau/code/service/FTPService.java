package cn.joylau.code.service;

import lombok.Data;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Service
@Data
@ConfigurationProperties(prefix = "ftp")
public class FTPService {
    private String username;
    private String password;
    private String ip;
    private int port;
    private static final Logger logger = LoggerFactory.getLogger(FTPService.class);

    private Charset serverCharset = Charset.forName("UTF-8");

    private FTPClient ftpClient;

    @PostConstruct
    public void initService() {
        try {
            FTPClient ftpClient = new FTPClient();
            ftpClient.setDefaultPort(port);
            ftpClient.connect(ip);
            if (!ftpClient.login(username, password)) {
                throw new Exception("ftp登录出错");
            }
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                logger.error("FTP server refused connection  " + ip + ":" + port);
            }
            ftpClient.setDataTimeout(30000);
            //开启UTF-8支持
            if (FTPReply.isPositiveCompletion(ftpClient.sendCommand("OPTS UTF8", "ON"))) {
                serverCharset = StandardCharsets.UTF_8;
            }
            //用于传输时，IO的字符集,所有读取服务器上的文件名，用于放回显示时；默认ISO-8859-1不支持中文,所以使用GBK或UTF-8，解决中文乱码
            ftpClient.setControlEncoding(serverCharset.name());
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileTransferMode(FTPClient.STREAM_TRANSFER_MODE);
            if (!ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE)) {
                throw new Exception("错误的FileType");
            }
            this.ftpClient = ftpClient;
        } catch (Exception e) {
            logger.error("FTP 连接失败");
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void destroyService() {
        try {
            FTPClient ftpClient = this.ftpClient;
            if (ftpClient != null) {
                ftpClient.logout();
                ftpClient.disconnect();
                logger.info("退出FTP连接");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载文件到输出流
     * @param path 路径
     * @param fileName 文件名
     * @return InputStream
     */
    public InputStream downStreamFile(String path, String fileName) {
        FTPClient ftpClient = this.ftpClient;
        FTPFile[] fs;
        try {
            String remotePath = new String(path.getBytes(serverCharset.toString()), "ISO-8859-1");
            if (!ftpClient.changeWorkingDirectory(remotePath)) {                //转移到FTP服务器目录
                logger.error(path + "目录不存在");
                return null;
            }
            fs = ftpClient.listFiles();
            for (FTPFile ff : fs) {
                if (ff.getName().equals(fileName)) {
                    return ftpClient.retrieveFileStream(ff.getName());
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 下载文件到输出流
     * @param remoteRelativePath 全路径
     * @return InputStream
     */
    public InputStream downStreamFile(String remoteRelativePath) {
        String remoteRelativeDir = "/";
        String remoteFileName;
        if (remoteRelativePath.contains("/")) {
            int index = remoteRelativePath.lastIndexOf("/");
            remoteRelativeDir = remoteRelativePath.substring(0, index);
            remoteFileName = remoteRelativePath.substring(index + 1);
        } else {
            remoteFileName = remoteRelativePath;
        }
        return downStreamFile(remoteRelativeDir,remoteFileName);
    }
}
