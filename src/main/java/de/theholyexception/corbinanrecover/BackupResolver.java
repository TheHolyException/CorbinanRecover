package de.theholyexception.corbinanrecover;

import de.theholyexception.holyapi.util.backuprecover.BackupItem;
import de.theholyexception.holyapi.util.backuprecover.BackupItemInfo;

import java.util.function.Function;

public class BackupResolver {

    private Function<String, BackupItemInfo> resolver;
    private String friendlyName;

    public BackupResolver(Function<String, BackupItemInfo> resolver, String friendlyName) {
        this.resolver = resolver;
        this.friendlyName = friendlyName;
    }

    public Function<String, BackupItemInfo> getResolver() {
        return resolver;
    }



    @Override
    public String toString() {
        return friendlyName;
    }
}
