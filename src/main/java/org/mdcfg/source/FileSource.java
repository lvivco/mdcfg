/**
 *   Copyright (C) 2023 LvivCoffeeCoders team.
 */
package org.mdcfg.source;

import org.mdcfg.exceptions.MdcException;
import org.mdcfg.watchers.FileWatcher;
import org.mdcfg.watchers.FolderWatcher;
import org.mdcfg.watchers.Watcher;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Base class for all File based source implementations */
public abstract class FileSource implements Source {
    private final File root;
    private List<File> includes;
    private Watcher watcher;

    protected FileSource(String path) {
        this.root = new File(path);
    }

    @Override
    public Map<String, Map<String, String>> read(Function<Map<String, Map<String, String>>, Map<String, String>> includesExtractor, boolean isCaseSensitive) throws MdcException {
        if(root.isDirectory()){
            File[] files = extractFiles(root);
            if(files == null || files.length == 0){
                throw new MdcException("Folder doesn't contain any config file.");
            }
            return readAndMerge(Arrays.asList(files), new HashMap<>(), isCaseSensitive);
        } else {
            Map<String, Map<String, String>> main = readFile(root, isCaseSensitive);
            includes = includesExtractor.apply(main).values().stream()
                    .map(v -> root.getParentFile().toPath().resolve(Paths.get(v)).toFile())
                    .filter(File::isFile)
                    .filter(File::exists)
                    .collect(Collectors.toList());
            return readAndMerge(includes, main, isCaseSensitive);
        }
    }

    @Override
    public void observeChange(Runnable onChange, long reloadInterval) throws MdcException {
        if(!root.exists()) {
            throw new MdcException(String.format("File or folder %s doesn't exist.", root.getAbsolutePath()));
        }

        watcher = root.isDirectory()
                ? new FolderWatcher(root.getAbsolutePath(), onChange, reloadInterval)
                : new FileWatcher(getAllSourceFiles(), onChange, reloadInterval);
        watcher.start();
    }

    /** Read one file */
    abstract Map<String, Map<String, String>> readFile(File source, boolean isCaseSensitive) throws MdcException;

    /** Get array of appropriate files in folder */
    abstract File[] extractFiles(File folder);

    /** Read properties from files and merge them into one Map */
    private Map<String, Map<String, String>> readAndMerge(List<File> files, Map<String, Map<String, String>> merged, boolean isCaseSensitive) throws MdcException {
        for (File file : files) {
            Map<String, Map<String, String>> map = readFile(file, isCaseSensitive);
            Set<String> interfileKeys = getInterfileKeys(map, merged);
            if(!interfileKeys.isEmpty()){
                throw new MdcException(String.format("There is interfile configuration for keys %s", interfileKeys));
            }
            merged.putAll(map);
        }
        return merged;
    }

    /** Combine root file with includes into single list */
    private List<File> getAllSourceFiles(){
        return Stream.concat(Stream.of(root), includes.stream()).collect(Collectors.toList());
    }

    /** Get list of properties that exists in different source files */
    private Set<String> getInterfileKeys(Map<String, Map<String, String>> map, Map<String, Map<String, String>> merged) {
        Set<String> intersection = new HashSet<>(merged.keySet());
        intersection.retainAll(map.keySet());
        return intersection;
    }
}
