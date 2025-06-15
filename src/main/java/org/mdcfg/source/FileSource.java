/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg.source;

import org.mdcfg.exceptions.MdcException;
import org.mdcfg.watchers.FileWatcher;
import org.mdcfg.watchers.FolderWatcher;
import org.mdcfg.watchers.Watcher;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Base class for all File based source implementations */
public abstract class FileSource extends StreamSource {
    private File root;
    private List<File> includes;
    private Watcher watcher;

    protected FileSource(InputStream stream) {
        super(stream);
    }

    protected FileSource(File file) {
        this.root = file;
    }

    protected FileSource(String path) {
        this.root = new File(path);
    }

    @Override
    public Map<String, Map<String, String>> read(Function<Map<String, Map<String, String>>, Map<String, String>> includesExtractor, boolean isCaseSensitive) throws MdcException {
        if(root == null){
            return super.read(includesExtractor, isCaseSensitive);
        }

        if(!root.exists()){
            throw new MdcException(String.format("File or folder %s doesn't exist.", root.getAbsolutePath()));
        }

        if (root.isDirectory()) {
            File[] files = listFiles(root);
            if (files == null || files.length == 0) {
                throw new MdcException("Folder doesn't contain any config file.");
            }
            return readAndMerge(toStreamList(Arrays.asList(files)), new HashMap<>(), isCaseSensitive);
        } else {
            Map<String, Map<String, String>> main = read(toStream(root), isCaseSensitive);
            includes = includesExtractor.apply(main).values().stream()
                    .map(v -> root.getParentFile().toPath().resolve(Paths.get(v)).toFile())
                    .collect(Collectors.toList());
            return readAndMerge(toStreamList(includes), main, isCaseSensitive);
        }
    }

    @Override
    public void observeChange(Runnable onChange, long reloadInterval) throws MdcException {
        if(root == null){
            throw new MdcException("Auto reload not supported for stream-based sources.");
        }
        watcher = root.isDirectory()
                ? new FolderWatcher(root.getAbsolutePath(), onChange, reloadInterval)
                : new FileWatcher(getAllSourceFiles(), onChange, reloadInterval);
        watcher.start();
    }

    /** Get array of appropriate files in folder */
    abstract File[] listFiles(File folder);

    private List<InputStream> toStreamList(List<File> files) throws MdcException {
        List<InputStream> streams = new ArrayList<>();
        for (File file : files) {
            streams.add(toStream(file));
        }
        return streams;
    }

    private InputStream toStream(File file) throws MdcException {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new MdcException(String.format("Couldn't open stream for file %s", file.getAbsolutePath()), e);
        }
    }

    /** Combine root file with includes into single list */
    private List<File> getAllSourceFiles(){
        return Stream.concat(Stream.of(root), includes.stream()).collect(Collectors.toList());
    }
}
