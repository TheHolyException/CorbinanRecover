package de.theholyexception.holyapi.util.drophandler;


import java.io.File;
import java.util.Collection;

public class DropInformation {

    private final Collection<File> files;
    private final int posX;
    private final int posY;

    public DropInformation(Collection<File> f, int posX, int posY) {
        this.files = f;
        this.posX = posX;
        this.posY = posY;
    }

    public Collection<File> getFiles() {
        return files;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }
}
