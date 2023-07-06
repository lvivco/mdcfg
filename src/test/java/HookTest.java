import com.mdcfg.builder.MdcBuilder;
import com.mdcfg.exceptions.MdcException;
import com.mdcfg.provider.MdcContext;
import com.mdcfg.provider.MdcOptional;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class HookTest {

    private static final String YAML_PATH = "/hook/conf.yaml";

    @Test
    public void testHookToAllProps() throws MdcException {
        AtomicInteger count = new AtomicInteger();
        MdcOptional provider = MdcBuilder.withYaml(new File(getClass().getResource(YAML_PATH).getPath()).getPath())
                .autoReload()
                .loadHook((chain) -> {
                    count.getAndIncrement();
                    chain.setValue(chain.getValue() + "_all");
                })
                .build().getOptional();

        Assert.assertEquals(18, count.get());

        MdcContext context = new MdcContext();
        context.put("model", "bmw");

        Assert.assertTrue(provider.getString(context, "price").orElse("").contains("_all"));
        Assert.assertTrue(provider.getString(context, "available-colors").orElse("").contains("_all"));
        Assert.assertTrue(provider.getString(context, "horsepower").orElse("").contains("_all"));
    }

    @Test
    public void testHookExactMatch() throws MdcException {
        AtomicInteger count = new AtomicInteger();
        MdcOptional provider = MdcBuilder.withYaml(new File(getClass().getResource(YAML_PATH).getPath()).getPath())
                .autoReload()
                .loadHook("price", (chain) -> {
                    count.getAndIncrement();
                    chain.setValue(chain.getValue() + "0");
                })
                .build().getOptional();

        Assert.assertEquals(5, count.get());

        MdcContext context = new MdcContext();
        context.put("model", "bmw");
        Assert.assertEquals(Integer.valueOf(450000), provider.getInteger(context, "price").orElse(0));
    }

    @Test
    public void testHookPattern() throws MdcException {
        AtomicInteger count = new AtomicInteger();
        MdcOptional provider = MdcBuilder.withYaml(new File(getClass().getResource(YAML_PATH).getPath()).getPath())
                .autoReload()
                .loadHook(Pattern.compile("^h.+r$"), (chain) -> {
                    count.getAndIncrement();
                    chain.setValue(chain.getValue() + "0");
                })
                .build().getOptional();

        Assert.assertEquals(4, count.get());

        MdcContext context = new MdcContext();
        context.put("model", "bmw");
        context.put("drive", "4WD");
        Assert.assertEquals(Integer.valueOf(5000), provider.getInteger(context, "horsepower").orElse(0));
    }

    @Test
    public void testTwoHooks() throws MdcException {
        MdcOptional provider = MdcBuilder.withYaml(new File(getClass().getResource(YAML_PATH).getPath()).getPath())
                .autoReload()
                .loadHook(Pattern.compile("^h.+r$"), (chain) -> chain.setValue(chain.getValue() + "0"))
                .loadHook("horsepower", (chain) -> chain.setValue(chain.getValue() + "0"))
                .build().getOptional();

        MdcContext context = new MdcContext();
        context.put("model", "bmw");
        Assert.assertEquals(Integer.valueOf(40000), provider.getInteger(context, "horsepower").orElse(0));
    }
}
