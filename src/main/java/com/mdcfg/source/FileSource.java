package com.mdcfg.source;

import com.mdcfg.exceptions.MdcException;
import com.mdcfg.watchers.FileWatcher;
import com.mdcfg.watchers.FolderWatcher;
import com.mdcfg.watchers.Watcher;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class FileSource implements Source {

    private final String path;

    private Watcher watcher;

    protected FileSource(String path) {
        this.path = path;
    }

    @Override
    public Map<String, Map<String, String>> read() throws MdcException {
        File source = new File(path);
        if(source.isDirectory()){
            File[] files = extractFiles(source);
            if(files == null || files.length == 0){
                throw new MdcException("Folder doesn't contain any config file.");
            }

            Map<String, Map<String, String>> combined = new HashMap<>();
            for (File file : files) {
                Map<String, Map<String, String>> map = readFile(file);

                // check whether there are interfile keys
                Set<String> intersection = combined.keySet();
                intersection.retainAll(map.keySet());
                if(!intersection.isEmpty()){
                    throw new MdcException(String.format("There is interfile configuration for keys %s", intersection));
                }
                combined.putAll(map);
            }
            return combined;
        } else {
            return readFile(source);
        }
    }

    @Override
    public void observeChange(Runnable onChange, long reloadInterval) throws MdcException {
        File file = new File(path);
        if(!file.exists()){
            throw new MdcException(String.format("File or folder %s doesn't exist.", path));
        }

        watcher = file.isDirectory()
                ? new FolderWatcher(path, onChange, reloadInterval)
                : new FileWatcher(path, onChange, reloadInterval);
        watcher.start();
    }

    abstract Map<String, Map<String, String>> readFile(File source) throws MdcException;

    abstract File[] extractFiles(File folder);

}
