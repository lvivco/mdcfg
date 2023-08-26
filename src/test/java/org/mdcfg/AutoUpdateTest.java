package org.mdcfg;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mdcfg.builder.MdcBuilder;
import org.mdcfg.builder.MdcCallback;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.provider.MdcProvider;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.mdcfg.Resources.YAML_PATH;

public class AutoUpdateTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testSingleFile() throws MdcException, ExecutionException, InterruptedException, IOException {
        final File tempFile = tempFolder.newFile("tempFile.yaml");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> modifyFile(Path.of(YAML_PATH), tempFile.toPath()), 200, TimeUnit.MILLISECONDS);

        CompletableFuture<String> future = new CompletableFuture<>();
        MdcProvider provider = MdcBuilder.withYaml(tempFile.getAbsolutePath())
                .autoReload(500, MdcCallback.<Integer, MdcException>builder()
                        .onSuccess(c->future.complete(c.toString()))
                        .build())
                .build();

        String count = future.get();
        assertEquals(8, Integer.parseInt(count));
    }

    @Test
    public void testFolder() throws MdcException, ExecutionException, InterruptedException, IOException {
        final File tempFile = tempFolder.newFile("tempFile.yaml");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> modifyFile(Path.of(YAML_PATH), tempFile.toPath()), 200, TimeUnit.MILLISECONDS);

        CompletableFuture<String> future = new CompletableFuture<>();
        MdcProvider provider = MdcBuilder.withYaml(tempFile.getParentFile().getAbsolutePath())
                .autoReload(500, MdcCallback.<Integer, MdcException>builder()
                        .onSuccess(c->future.complete(c.toString()))
                        .build())
                .build();

        String count = future.get();
        assertEquals(8, Integer.parseInt(count));
    }

    private void modifyFile(Path from, Path to) {
        try {
            String read = Files.readString(from);
            Files.writeString(to, read);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
