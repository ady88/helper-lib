package com.helperlib.command.terminaltoggle;

import com.helperlib.api.command.CommandType;
import com.helperlib.command.terminal.TerminalCommandMetadata;

import java.util.Map;

/**
 * Extends TerminalCommandMetadata with a dedicated toggle command text.
 * Use commandText for the main/start command and toggleCommandText for the
 * toggle/stop action.
 */
public class TerminalToggleCommandMetadata extends TerminalCommandMetadata {
    private String toggleCommandText;

    public TerminalToggleCommandMetadata(
            String name,
            String description,
            String commandText,
            String toggleCommandText,
            Map<String, String> arguments,
            String path,
            String environmentPathVariable
    ) {
        super(name, description, CommandType.TERMINAL_TOGGLE, commandText, arguments, path, environmentPathVariable);
        this.toggleCommandText = toggleCommandText;
    }

    public TerminalToggleCommandMetadata(
            String name,
            String description,
            CommandType commandType,
            String commandText,
            String toggleCommandText,
            Map<String, String> arguments,
            String path,
            String environmentPathVariable
    ) {
        super(name, description, commandType, commandText, arguments, path, environmentPathVariable);
        this.toggleCommandText = toggleCommandText;
    }

    public String getToggleCommandText() {
        return toggleCommandText;
    }

    public void setToggleCommandText(String toggleCommandText) {
        this.toggleCommandText = toggleCommandText;
    }
}
