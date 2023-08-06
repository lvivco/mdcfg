package org.mdcfg;

import org.mdcfg.builder.MdcBuilder;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.provider.MdcProvider;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

import static org.mdcfg.Resources.YAML_PATH;

@State(Scope.Benchmark)
public class PerformanceTest {

	private MdcProvider provider;

	@Setup(Level.Invocation)
	public void setUp() throws MdcException {
		provider = MdcBuilder.withYaml(YAML_PATH).build();
	}

	@Benchmark
	@Fork(value = 0, warmups = 1)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	@BenchmarkMode(Mode.AverageTime)
	public void init() {
		provider.getString(
			TestContextBuilder.init()
					.model("bmw")
					.category("crossover")
					.build(), "available-colors");
	}

	public static void main(String[] args) throws Exception {
		org.openjdk.jmh.Main.main(args);
	}
}
