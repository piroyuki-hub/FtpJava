import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MainTest {

    @InjectMocks
    Main target;

    FakeFtpServer server;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // NOTE: 検証用として file を ftp server に配置 (最終更新日時を set)
        var file = new FileEntry("/success.jpg");
        file.setLastModified(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()));
        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(file);

        // NOTE: real ftp server に接続する場合、FakeFTPServer の設定不要
        server = new FakeFtpServer();
        server.setServerControlPort(0);
        server.addUserAccount(new UserAccount("username", "mypass", "/"));
        server.setFileSystem(fileSystem);
        server.start();

        var conf = new FtpConf();
        conf.setHost("localhost");
        conf.setPort(server.getServerControlPort());
        conf.setUser("username");
        conf.setPass("mypass");
        conf.setConnectTimeout(3);
        conf.setDefaultTimeout(60);
        conf.setSoTimeout(60);
        conf.setDataTimeout(180);
        target.configure(conf);
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void getLastModifiedTimeByMdtmTest() throws IOException {
//        var actual = target.getLastModifiedTimeByMdtm("/coffee-g4880017cd_640.jpg");
        // FIXME: FakeFtpServer だと FTPClient.getModificationTime が使えない？ (返却 null)
        // TODO: 検証するなら real ftp server に接続して検証 (ftp server が MDTM に対応していること)
        var actual = target.getLastModifiedTimeByMdtm("/success.jpg");
        assertThat(actual).isNotNull();
        System.out.println(actual);

        assertThatThrownBy(() -> target.getLastModifiedTimeByMdtm("/fail.jpg"))
                .isInstanceOfSatisfying(RuntimeException.class
                        , (e) -> assertThat(e.getMessage()).isEqualTo("file not exist"));
    }

    @Test
    void getLastModifiedTimeByListTest() throws IOException {
//        var actual = target.getLastModifiedTimeByList("/coffee-g4880017cd_640.jpg");
        var actual = target.getLastModifiedTimeByList("/success.jpg");
        assertThat(actual).isNotNull();
        System.out.println(actual);

        assertThatThrownBy(() -> target.getLastModifiedTimeByList("/fail.jpg"))
                .isInstanceOfSatisfying(RuntimeException.class
                        , (e) -> assertThat(e.getMessage()).isEqualTo("file not exist"));
    }
}
