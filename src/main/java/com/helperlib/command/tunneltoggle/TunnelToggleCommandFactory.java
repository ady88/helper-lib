package com.helperlib.command.tunneltoggle;

import com.helperlib.api.command.Command;
import com.helperlib.api.command.CommandFactory;
import com.helperlib.api.command.CommandMetadata;
import com.helperlib.api.command.CommandType;
import com.helperlib.api.command.logging.StreamHandler;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class TunnelToggleCommandFactory implements CommandFactory {

    @Override
    public CommandMetadata parseMetadata(JsonObject json) {
        String name = json.getString("name");
        String description = json.getString("description", "SSH Tunnel Toggle");

        // SSH
        String host = json.getString("host");
        int port = json.getInt("port", 22);
        String username = json.getString("username");

        // Auth
        String authTypeStr = json.getString("authType", "PASSWORD");
        TunnelAuthType authType = TunnelAuthType.valueOf(authTypeStr);
        String password = json.getString("password", "");
        String privateKeyPath = json.getString("privateKeyPath", "");
        String passphrase = json.getString("passphrase", "");

        // Host key verification
        boolean strictHK = json.getBoolean("strictHostKeyChecking", false);
        String knownHostsPath = json.getString("knownHostsPath", "");

        // Forwarding
        String localBindHost = json.getString("localBindHost", "127.0.0.1");
        int localPort = json.getInt("localPort");
        String remoteHost = json.getString("remoteHost");
        int remotePort = json.getInt("remotePort");

        // Timings
        int connectTimeoutMs = json.getInt("connectTimeoutMs", 10_000);
        int keepAliveIntervalSec = json.getInt("keepAliveIntervalSec", 30);

        // Prefer a dedicated type (e.g., TUNNEL_TOGGLE)
        CommandType type = CommandType.valueOf(json.getString("type", "TUNNEL_TOGGLE"));

        return new TunnelToggleCommandMetadata(
                name, description, type,
                host, port, username,
                authType, password, privateKeyPath, passphrase,
                strictHK, knownHostsPath,
                localBindHost, localPort, remoteHost, remotePort,
                connectTimeoutMs, keepAliveIntervalSec
        );
    }

    @Override
    public JsonObject serializeMetadata(CommandMetadata meta) {
        TunnelToggleCommandMetadata m = (TunnelToggleCommandMetadata) meta;
        JsonObjectBuilder b = Json.createObjectBuilder()
                .add("name", m.getName())
                .add("description", m.getDescription())
                .add("type", m.getType().toString())
                .add("host", m.getHost())
                .add("port", m.getPort())
                .add("username", m.getUsername())
                .add("authType", m.getAuthType().name())
                .add("strictHostKeyChecking", m.isStrictHostKeyChecking())
                .add("localBindHost", m.getLocalBindHost())
                .add("localPort", m.getLocalPort())
                .add("remoteHost", m.getRemoteHost())
                .add("remotePort", m.getRemotePort())
                .add("connectTimeoutMs", m.getConnectTimeoutMs())
                .add("keepAliveIntervalSec", m.getKeepAliveIntervalSec());

        // Optional fields
        if (m.getKnownHostsPath() != null) b.add("knownHostsPath", m.getKnownHostsPath());
        if (m.getPassword() != null) b.add("password", m.getPassword());
        if (m.getPrivateKeyPath() != null) b.add("privateKeyPath", m.getPrivateKeyPath());
        if (m.getPassphrase() != null) b.add("passphrase", m.getPassphrase());

        return b.build();
    }

    @Override
    public Command createCommand(CommandMetadata metadata, StreamHandler streamHandler) {
        TunnelToggleCommandMetadata m = (TunnelToggleCommandMetadata) metadata;
        String commandId = generateCommandId(m);
        return (Command) TunnelToggleCommandRegistry.getOrCreateCommand(
                commandId,
                () -> new TunnelToggleCommand(m) // StreamHandler unused here
        );
    }

    private String generateCommandId(TunnelToggleCommandMetadata m) {
        return String.format(
                "%s@%s:%d|%s:%d<- %s:%d",
                m.getUsername(),
                m.getHost(),
                m.getPort(),
                m.getRemoteHost(),
                m.getRemotePort(),
                m.getLocalBindHost(),
                m.getLocalPort()
        );
    }
}
