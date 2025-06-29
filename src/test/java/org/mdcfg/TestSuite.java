/**
 *   Copyright (C) 2025 LvivCoffeeCoders team.
 */
package org.mdcfg;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
        BuilderTest.class,
        ProviderTest.class,
        ConversionTest.class,
        HookTest.class,
        AliasTest.class,
        EnablingTest.class,
        AutoUpdateTest.class,
        CaseSensitivityTest.class
})
public class TestSuite {
}
