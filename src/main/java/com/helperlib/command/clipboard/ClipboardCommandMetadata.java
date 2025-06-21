package com.helperlib.command.clipboard;

import com.helperlib.api.command.CommandMetadata;
import com.helperlib.api.command.CommandType;

public class ClipboardCommandMetadata extends CommandMetadata {
    private String textToCopy;

    public ClipboardCommandMetadata(String name, String description, String textToCopy) {
        super(name, description, CommandType.CLIPBOARD);
        this.textToCopy = textToCopy;
    }

    public String getTextToCopy() {
        return textToCopy;
    }

    public void setTextToCopy(String textToCopy) {
        this.textToCopy = textToCopy;
    }
}

