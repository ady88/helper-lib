module helper.lib {
    // Required modules
    requires helper.lib.core;           // Our core dependency
    requires com.helperlib.api;         // API module (transitive from core, but explicit for clarity)
    requires jakarta.json;              // For JSON processing
    requires java.base;                 // Base Java module (implicit, but explicit for clarity)
    requires java.desktop;              // For AWT classes used in ClipboardCommand

    // Export our command implementations so they can be used by other modules
    exports com.helperlib.command.terminal;
    exports com.helperlib.command.clipboard;
    exports com.helperlib.command.terminaltoggle;

    // If you have any service providers, declare them here
    // For example, if you're providing CommandFactory implementations:
    provides com.helperlib.api.command.CommandFactory
            with com.helperlib.command.terminal.TerminalCommandFactory,
                    com.helperlib.command.terminaltoggle.TerminalToggleCommandFactory,
                    com.helperlib.command.clipboard.ClipboardCommandFactory;
}