import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

public class Ftp implements AutoCloseable {

    private final FTPClient ftp;
    private final FtpConf conf;

    public Ftp(FtpConf conf) {
        this.conf = conf;
        this.ftp = new FTPClient();
    }

    @Override
    public void close() throws IOException {
        if (!ftp.isConnected()) {
            return;
        }
        ftp.disconnect();
    }

    /**
     * ftp server から file の最終更新日時を取得.
     * <p>* ftp server が MDTM に対応している必要がある</p>
     *
     * @param path file path
     * @return {@link LocalDateTime} (timezone: GMT / format: yyyyMMddHHmmss)
     */
    public Optional<LocalDateTime> getLastModificationTimeByMdtm(String path) throws IOException {
        login();
        var gmt = ftp.getModificationTime(path);
        logout();
        return Objects.isNull(gmt)
                ? Optional.empty()
                : Optional.of(LocalDateTime.parse(gmt, DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
    }

    /**
     * ftp server から file の最終更新日時を取得.
     * <p>* 秒欠損</p>
     *
     * @param path file path
     * @return {@link LocalDateTime} (timezone: UTC / format: yyyyMMddHHmm00)
     */
    public Optional<LocalDateTime> getLastModificationTimeByList(String path) throws IOException {
        login();
        var files = ftp.listFiles(path);
        logout();
        return files.length == 0
                ? Optional.empty()
                : Optional.of(LocalDateTime.ofInstant(files[0].getTimestamp().toInstant(), ZoneId.systemDefault()));
    }

    private void login() throws IOException {
        connect();

        // NOTE: 接続後の timeout
        ftp.setSoTimeout(conf.getSoTimeout() * 1000);
        if (!ftp.login(conf.getUser(), conf.getPass())) {
            throw new RuntimeException("Could not login...");
        }

        ftp.enterLocalPassiveMode();
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
    }

    private void connect() throws IOException {
        if (ftp.isConnected()) return;

        // NOTE: 接続前 timeout
        ftp.setConnectTimeout(conf.getConnectTimeout() * 1000);
        ftp.setDefaultTimeout(conf.getDefaultTimeout() * 1000);
        ftp.setDataTimeout(conf.getDataTimeout() * 1000);

        ftp.connect(conf.getHost(), conf.getPort());
        if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
            throw new RuntimeException("Could not connect to server...");
        }
    }

    private void logout() throws IOException {
        ftp.logout();
    }
}
