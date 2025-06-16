/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mdcfg.builder.MdcBuilder;
import org.mdcfg.builder.MdcCallback;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.provider.MdcProvider;
import org.mdcfg.helpers.TestContextBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.*;
import java.nio.file.attribute.FileTime;

import static org.junit.Assert.assertEquals;
import static org.mdcfg.helpers.Resources.YAML_SINGLE_PATH;

public class AutoUpdateTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testSingleFile() throws MdcException, ExecutionException, InterruptedException, IOException {
        final File tempFile = tempFolder.newFile("tempFile.yaml");

        CompletableFuture<String> future = new CompletableFuture<>();
        MdcProvider provider = MdcBuilder.withYaml(tempFile.getAbsolutePath())
                .autoReload(500, MdcCallback.<Integer, MdcException>builder()
                        .onSuccess(c->future.complete(c.toString()))
                        .build())
                .build();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> modifyFile(Path.of(YAML_SINGLE_PATH), tempFile.toPath()), 200, TimeUnit.MILLISECONDS);

        String count = future.get();
        assertEquals(1, Integer.parseInt(count));
        assertEquals("45000", provider.getString(TestContextBuilder.init().model("bmw").build(), "price"));
        provider.stopAutoReload();
    }

    @Test
    public void testFolder() throws MdcException, ExecutionException, InterruptedException, IOException {
        final File tempFile = tempFolder.newFile("tempFile.yaml");

        CompletableFuture<String> future = new CompletableFuture<>();
        MdcProvider provider = MdcBuilder.withYaml(tempFile.getParentFile().getAbsolutePath())
                .autoReload(500, MdcCallback.<Integer, MdcException>builder()
                        .onSuccess(c->future.complete(c.toString()))
                        .build())
                .build();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> modifyFile(Path.of(YAML_SINGLE_PATH), tempFile.toPath()), 200, TimeUnit.MILLISECONDS);

        String count = future.get();
        assertEquals(1, Integer.parseInt(count));
        assertEquals("45000", provider.getString(TestContextBuilder.init().model("bmw").build(), "price"));
        provider.stopAutoReload();
    }

    @Test(expected = MdcException.class)
    public void testAutoReloadNotSupportedForStream() throws MdcException, FileNotFoundException {
        MdcBuilder.withYaml(new FileInputStream(YAML_SINGLE_PATH))
                .autoReload()
                .build();
    }

    @Test
    public void testFailureCallback() throws MdcException, ExecutionException, InterruptedException, IOException {
        final File tempFile = tempFolder.newFile("tempFile.yaml");
        Files.writeString(tempFile.toPath(), Files.readString(Path.of(YAML_SINGLE_PATH)));

        CompletableFuture<String> future = new CompletableFuture<>();
        MdcProvider provider = MdcBuilder.withYaml(tempFile.getAbsolutePath())
                .autoReload(200, MdcCallback.<Integer, MdcException>builder()
                        .onFailure(e -> future.complete(e.getMessage()))
                        .build())
                .build();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> writeInvalidYaml(tempFile.toPath()), 300, TimeUnit.MILLISECONDS);

        String message = future.get();
        assertEquals("Invalid nesting for any", message);
        assertEquals(0, provider.getSize());
        provider.stopAutoReload();
    }

    private void modifyFile(Path from, Path to) {
        try {
            String read = Files.readString(from);
            Files.writeString(to, read);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeInvalidYaml(Path to) {
        try {
            String data = "invalid:\n  any: 42\n";
            Files.writeString(to, data);
            Files.setLastModifiedTime(to, FileTime.fromMillis(System.currentTimeMillis()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
