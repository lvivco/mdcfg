import com.mdcfg.builder.MdcBuilder;
import com.mdcfg.exceptions.MdcException;
import com.mdcfg.provider.MdcOptional;
import com.mdcfg.provider.MdcProvider;
import org.junit.Test;

import java.io.File;

public class ReaderTest {

    private static final String YAML_PATH = "conf.yaml";
    private static final String JSON_PATH = "conf.json";

    @Test
    public void testYaml() throws MdcException {
        MdcOptional provider = MdcBuilder.withYaml(new File(getClass().getResource(YAML_PATH).getPath()).getPath())
                .autoReload()
                .build().getOptional();
    }

    @Test
    public void testJson() throws MdcException {
        MdcProvider provider = MdcBuilder.withJson(new File(getClass().getResource(JSON_PATH).getPath()).getPath())
                .autoReload()
                .build();

    }
}
