import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class FtpConf {
    private String host;
    private int port;
    private String user;
    private String pass;
    private int defaultTimeout;
    private int soTimeout;
    private int connectTimeout;
    private int dataTimeout;
}
