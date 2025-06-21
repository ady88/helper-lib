package com.helperlib.command.clipboard;

import com.helperlib.api.command.Command;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class ClipboardCommand extends Command {

    public ClipboardCommand(ClipboardCommandMetadata metadata) {
        super(metadata);
    }

    @Override
    public void execute() {
        ClipboardCommandMetadata clipboardMetadata = (ClipboardCommandMetadata) metadata;
        try {
            StringSelection stringSelection = new StringSelection(clipboardMetadata.getTextToCopy());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            System.out.println("Text copied to clipboard successfully.");
        } catch (Exception e) {
            System.err.println("Error copying text to clipboard: " + e.getMessage());
        }
    }

}

