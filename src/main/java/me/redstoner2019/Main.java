package me.redstoner2019;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return false;
            }

            @Override
            public String getDescription() {
                return "";
            }
        });
    }
}