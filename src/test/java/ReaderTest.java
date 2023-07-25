import com.mdcfg.builder.MdcBuilder;
import com.mdcfg.exceptions.MdcException;
import com.mdcfg.provider.MdcContext;
import com.mdcfg.provider.MdcOptional;
import com.mdcfg.provider.MdcProvider;
import org.junit.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ReaderTest {

    private static final String YAML_PATH = Objects.requireNonNull(ReaderTest.class.getResource("conf.yaml")).getPath();
    private static final String JSON_PATH = Objects.requireNonNull(ReaderTest.class.getResource("conf.json")).getPath();

    @Test
    public void testYaml() throws MdcException {
        MdcOptional provider = MdcBuilder.withYaml(YAML_PATH)
                .autoReload()
                .build().getOptional();

        assertNotNull(provider);
    }

    @Test
    public void testSubProperty() throws MdcException {
        MdcProvider provider = MdcBuilder.withYaml(YAML_PATH).build();

        List<String> types = provider.getStringList(new MdcContext(), "engine.type");
        assertEquals("[electric, gas, diesel]", types.toString());

        List<String> drives = provider.getStringList(new MdcContext(), "engine.drive");
        assertEquals("[4WD, 2WD]", drives.toString());
    }

    @Test
    public void testJson() throws MdcException {
        MdcProvider provider = MdcBuilder.withJson(JSON_PATH)
                .autoReload()
                .build();

        assertNotNull(provider);
    }
}
