module helper.lib {
    // Required modules
    requires helper.lib.core;           // Our core dependency
    requires com.helperlib.api;         // API module (transitive from core, but explicit for clarity)
    requires java.base;                 // Base Java module (implicit, but explicit for clarity)
    requires java.desktop;              // For AWT classes used in ClipboardCommand
    requires java.net.http;             // For HTTP client functionality
    requires jakarta.json;              // For JSON processing
    requires com.jcraft.jsch;
    requires static jdk.httpserver; // for com.sun.net.httpserver.HttpServer used in tests



    uses jakarta.json.spi.JsonProvider;

    // Export our command implementations so they can be used by other modules
    exports com.helperlib.command.terminal;
    exports com.helperlib.command.clipboard;
    exports com.helperlib.command.terminaltoggle;
    exports com.helperlib.command.rest;
    exports com.helperlib.command.tunneltoggle;
    exports com.helperlib.command.template;


    // If you have any service providers, declare them here
    // For example, if you're providing CommandFactory implementations:
    provides com.helperlib.api.command.CommandFactory
            with com.helperlib.command.terminal.TerminalCommandFactory,
                    com.helperlib.command.terminaltoggle.TerminalToggleCommandFactory,
                    com.helperlib.command.clipboard.ClipboardCommandFactory,
                    com.helperlib.command.rest.RestCommandFactory,
                    com.helperlib.command.tunneltoggle.TunnelToggleCommandFactory;
}