/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
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
        AutoUpdateTest.class
})
public class TestSuite {
}
