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

public abstract class FileSource implements Source {

    private final File root;

    private List<File> includes;

    private Watcher watcher;

    protected FileSource(String path) {
        this.root = new File(path);
    }

    @Override
    public Map<String, Map<String, String>> read(Function<Map<String, Map<String, String>>, Map<String, String>> includesExtractor) throws MdcException {
        if(root.isDirectory()){
            File[] files = extractFiles(root);
            if(files == null || files.length == 0){
                throw new MdcException("Folder doesn't contain any config file.");
            }
            return readAndMerge(Arrays.asList(files), new HashMap<>());
        } else {

            Map<String, Map<String, String>> main = readFile(root);
            includes = includesExtractor.apply(main).entrySet().stream()
                    .map(e -> root.getParentFile().toPath().resolve(Paths.get(e.getValue())).toFile())
                    .filter(File::isFile)
                    .filter(File::exists)
                    .collect(Collectors.toList());

            return readAndMerge(includes, main);
        }
    }

    @Override
    public void observeChange(Runnable onChange, long reloadInterval) throws MdcException {
        if(!root.exists()){
            throw new MdcException(String.format("File or folder %s doesn't exist.", root.getAbsolutePath()));
        }

        watcher = root.isDirectory()
                ? new FolderWatcher(root.getAbsolutePath(), onChange, reloadInterval)
                : new FileWatcher(getAllSourceFiles(), onChange, reloadInterval);
        watcher.start();
    }

    abstract Map<String, Map<String, String>> readFile(File source) throws MdcException;

    abstract File[] extractFiles(File folder);

    private Map<String, Map<String, String>> readAndMerge(List<File> files, Map<String, Map<String, String>> merged) throws MdcException {
        for (File file : files) {
            Map<String, Map<String, String>> map = readFile(file);
            Set<String> interfileKeys = getInterfileKeys(map, merged);
            if(!interfileKeys.isEmpty()){
                throw new MdcException(String.format("There is interfile configuration for keys %s", interfileKeys));
            }
            merged.putAll(map);
        }
        return merged;
    }

    private List<File> getAllSourceFiles(){
        return Stream.concat(Stream.of(root), includes.stream()).collect(Collectors.toList());
    }

    private Set<String> getInterfileKeys(Map<String, Map<String, String>> map, Map<String, Map<String, String>> merged) {
        Set<String> intersection = new HashSet<>(merged.keySet());
        intersection.retainAll(map.keySet());
        return intersection;
    }
}
