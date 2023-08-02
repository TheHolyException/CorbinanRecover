package de.theholyexception.corbinanrecover;

import de.theholyexception.holyapi.util.backuprecover.BackupItemInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BackupItemInfoCC extends BackupItemInfo {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("EEEE yyyy.MM.dd - HH:mm:ss");

    public BackupItemInfoCC(BackupItemInfo backupItemInfo) {
        super(backupItemInfo.getPath(), backupItemInfo.getType(), backupItemInfo.getTimeStamp());
    }

    @Override
    public String toString() {
        String shortType = switch (getType()) {
            case FULL -> "F";
            case INCREMENTAL -> "I";
            case DIFFERENTIAL -> "D";
            default -> "NA";
        };
        return String.format("%s [%s] %s", getPath(), shortType, SIMPLE_DATE_FORMAT.format(new Date(getTimeStamp())));
    }
}
