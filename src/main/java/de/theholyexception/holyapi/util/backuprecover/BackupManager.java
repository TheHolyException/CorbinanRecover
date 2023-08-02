package de.theholyexception.holyapi.util.backuprecover;

import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.ZipParameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BackupManager {

    private final Map<String, List<BackupItem>> items;
    private final File sourceFolder;
    private final File outputFolder;
    private final Function<String, BackupItemInfo> backupInfoResolver;


    public BackupManager(File sourceFolder, File outputFolder, Function<String, BackupItemInfo> backupInfoResolver) {
        Objects.requireNonNull(sourceFolder, "sourceFolder");
        Objects.requireNonNull(outputFolder, "outputFolder");
        Objects.requireNonNull(backupInfoResolver, "backupInfoResolver");

        this.items = new HashMap<>();
        this.sourceFolder = sourceFolder;
        this.outputFolder = outputFolder;
        this.backupInfoResolver = backupInfoResolver;

        if (!sourceFolder.exists()) throw new IllegalArgumentException("SourceFolder not exists");
        if (!outputFolder.exists()) outputFolder.mkdirs();
        scanSourceDirectory();
    }

    public static Map<String, List<BackupItemInfo>> scanSourceDirectory(File sourceFolder, Function<String, BackupItemInfo> resolver) {
        List<BackupItemInfo> tempItems = new ArrayList<>();
        for (File file : Objects.requireNonNull(sourceFolder.listFiles())) {
            BackupItemInfo item = resolver.apply(file.getName());
            if (item == null || !item.isValid()) continue;
            tempItems.add(item);
        }

        return tempItems.stream()
            .sorted(Comparator.comparing(BackupItemInfo::getTimeStamp))
            .collect(Collectors.groupingBy(BackupItemInfo::getPath));
    }

    private void scanSourceDirectory() {
        // Converting all Files to BackupItem objects
        List<BackupItem> tempItems = new ArrayList<>();
        for (File file : Objects.requireNonNull(sourceFolder.listFiles())) {
            BackupItem item = BackupItem.create(file, this);
            if (item.getItemInfo() == null) continue;
            tempItems.add(item);
        }

        items.clear();
        // Group all BackupItems by their Path
        items.putAll(tempItems.stream()
                .sorted(Comparator.comparing(BackupItem::getTimeStamp))
                .collect(Collectors.groupingBy(BackupItem::getPath)));

        // Iterate over all Groups
        items.forEach((k, v) -> {

            BackupItem lastDiffOrFull = null;
            BackupItem lastItem = null;
            // Iterate over all BackupItems
            for (BackupItem bi : v) {
                if (bi.getBackupType().equals(BackupType.FULL)) {
                    lastDiffOrFull = bi;
                    lastItem = null;
                }

                if (lastDiffOrFull == null) continue;

                if (bi.getBackupType().equals(BackupType.DIFFERENTIAL)) {
                    bi.setLastBackup(lastDiffOrFull);
                    lastDiffOrFull = bi;
                    lastItem = null;
                }

                if (bi.getBackupType().equals(BackupType.INCREMENTAL)) {
                    bi.setLastBackup(lastItem == null ? lastDiffOrFull : lastItem);
                    lastItem = bi;
                }
            }
        });
    }

    public Function<String, BackupItemInfo> getBackupInfoResolver() {
        return backupInfoResolver;
    }

    public File restore(BackupItem item) {
        return restore(item.getPath(), item.getTimeStamp(), null);
    }

    public File restore(BackupItemInfo itemInfo) {
        return restore(itemInfo.getPath(), itemInfo.getTimeStamp(), null);
    }

    /**
     *
     * @param path the file prefix inside the folder you want to recover ex: TestItem123 2022-07-31 05;30;06 (Full) => you only need "TestItem123"
     * @param timestamp the timestamp you want to recover from, it tries to pick the nearest backup
     * @param password for archives that needed password, leaf it empty if not needed
     * @return the file object of the restored backup
     */
    public File restore(String path, long timestamp, char[] password, String startpoint) {
        if (startpoint != null && startpoint.length() == 0) startpoint = null;

        List<BackupItem> localItems = items.get(path);
        if (localItems == null) throw new IllegalArgumentException("Invalid path");

        Optional<BackupItem> item  = localItems.stream()
                .filter(backupItem -> backupItem.getTimeStamp() <= timestamp)
                .max(Comparator.comparing(BackupItem::getTimeStamp));

        if (!item.isPresent()) throw new IllegalStateException("BackupItem not found!");

        BackupItemInfo itemInfo = item.get().getItemInfo();
        File outputFile = new File(outputFolder, itemInfo.getPath() + " " + itemInfo.getTimeStamp() + ".zip");



        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile))) {
            BackupItem i = item.get();
            List<String> blackList = new ArrayList<>();

            do {
                writeZipFile(zos, i, password, blackList, startpoint);
                i = i.getLastBackup();
            } while (i != null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.gc();
        System.runFinalization();
        return outputFile;
    }

    /**
     *
     * @param path the file prefix inside the folder you want to recover ex: TestItem123 2022-07-31 05;30;06 (Full) => you only need "TestItem123"
     * @param timestamp the timestamp you want to recover from, it tries to pick the nearest backup
     * @param password for archives that needed password, leaf it empty if not needed
     * @return the file object of the restored backup
     */
    public File restore(String path, long timestamp, char[] password) {
        return this.restore(path, timestamp, password, null);
    }

    private void writeZipFile(ZipOutputStream zos, BackupItem item, char[] password, List<String> blacklist, String startpoint) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(item.getFile()), password)) {
            LocalFileHeader localFileHeader;
            byte[] buffer = new byte[8192];
            int l;

            while((localFileHeader = zis.getNextEntry()) != null) {
                String fileName = localFileHeader.getFileName().replaceAll("HarddiskVolumeShadowCopy[0-9]+", "HarddiskVolumeShadowCopy");
                if (blacklist.contains(fileName))
                    continue;


                if (startpoint != null) {
                    startpoint = startpoint.replaceAll("HarddiskVolumeShadowCopy[0-9]+", "HarddiskVolumeShadowCopy");
                    startpoint = startpoint.replace("/","\\");
                    fileName = fileName.replace("/","\\");
                    if (!fileName.startsWith(startpoint))
                        continue;
                    fileName = fileName.replace(startpoint, "");
                }
                System.out.println(fileName);

                ZipParameters parameters = new ZipParameters();
                parameters.setFileNameInZip(fileName);
                parameters.setLastModifiedFileTime(localFileHeader.getLastModifiedTime());
                parameters.setFileComment("from " + item.getFile());
                zos.putNextEntry(parameters);
                while((l = zis.read(buffer)) != -1) {
                    zos.write(buffer, 0, l);
                }
                zos.closeEntry();
                blacklist.add(localFileHeader.getFileName());
            }



        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Resolvers are there for converting the filename to usable data
     * for example we get the Backup simple name, Timestamp and Type (Full, Differential, Incremental) from it
     *
     * Here are some default resolvers and anyone can create their own resolver
     */
    public static class Resolvers {

        public static final SimpleDateFormat CORBINAN_DE_SDF = new SimpleDateFormat("yyyy-MM-dd HH;mm;ss");
        public static final Function<String, BackupItemInfo> CORBINAN_DE = fileName -> {
            String[] segments = fileName.split(" ");
            if (segments.length < 4) return null;

            int segSize = segments.length;
            BackupType type;
            switch (segments[segSize-1].replace(".zip","").replace("(","").replace(")", "")) {
                case "Inkrementell":
                    type = BackupType.INCREMENTAL;
                    break;
                case "Vollständig":
                    type = BackupType.FULL;
                    break;
                case "Differentiell":
                    type = BackupType.DIFFERENTIAL;
                    break;
                default: type = null;
            }

            long timeStamp = -1;
            try {
                timeStamp = CORBINAN_DE_SDF.parse(segments[segSize-3] + " " + segments[segSize-2]).getTime();
            } catch (ParseException ex) {
                ex.printStackTrace();
                return null;
            }

            return new BackupItemInfo(segments[0], type, timeStamp);
        };

        public static final Function<String, BackupItemInfo> CORBINAN_EN = fileName -> {
            String[] segments = fileName.split(" ");
            if (segments.length < 4) return null;

            int segSize = segments.length;
            BackupType type;
            switch (segments[segSize-1].replace(".zip","").replace("(","").replace(")", "")) {
                case "Incremental":
                    type = BackupType.INCREMENTAL;
                    break;
                case "Full":
                    type = BackupType.FULL;
                    break;
                case "Differential":
                    type = BackupType.DIFFERENTIAL;
                    break;
                default: type = null;
            }

            long timeStamp = -1;
            try {
                timeStamp = CORBINAN_DE_SDF.parse(segments[segSize-3] + " " + segments[segSize-2]).getTime();
            } catch (ParseException ex) {
                ex.printStackTrace();
                return null;
            }

            return new BackupItemInfo(segments[0], type, timeStamp);
        };
    }

}
