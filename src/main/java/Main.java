import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Main {

    private FtpConf conf;

    public void configure(FtpConf conf) {
        this.conf = conf;
    }

    /**
     * ftp server から file の最終更新日時を取得.
     * <p>* ftp server が MDTM に対応している必要がある</p>
     *
     * @param path file path
     * @return {@link LocalDateTime} (timezone: JST)
     */
    public LocalDateTime getLastModifiedTimeByMdtm(String path) throws IOException {
        try (var ftp = new Ftp(conf)) {
            return ftp.getLastModificationTimeByMdtm(path)
                    .map(this::convertGmt2Jst)
                    .orElseThrow(RuntimeException::new);
        }
    }

    /**
     * ftp server から file の最終更新日時を取得.
     * <p>* 秒欠損</p>
     *
     * @param path file path
     * @return {@link LocalDateTime} (timezone: JST)
     */
    public LocalDateTime getLastModifiedTimeByList(String path) throws IOException {
        try (var ftp = new Ftp(conf)) {
            return ftp.getLastModificationTimeByList(path)
                    .map(this::convertGmt2Jst)
                    .orElseThrow(() -> new RuntimeException("file not exist"));
        }
    }

    private LocalDateTime convertGmt2Jst(LocalDateTime gmt) {
        return ZonedDateTime.of(gmt, ZoneId.of("GMT"))
                .withZoneSameInstant(ZoneId.of("Asia/Tokyo"))
                .toLocalDateTime();
    }
}
