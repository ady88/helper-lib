package com.helperlib.command.tunneltoggle;

import com.helperlib.api.command.CommandMetadata;
import com.helperlib.api.command.CommandType;

public class TunnelToggleCommandMetadata extends CommandMetadata {

    // SSH connection
    private String host;
    private int port; // default 22
    private String username;

    // Auth
    private TunnelAuthType authType; // PASSWORD or PRIVATE_KEY
    private String password;         // optional (PASSWORD)
    private String privateKeyPath;   // optional (PRIVATE_KEY)
    private String passphrase;       // optional (PRIVATE_KEY)

    // Host key verification
    private boolean strictHostKeyChecking; // default false
    private String knownHostsPath;         // optional, used if provided

    // Local port forward
    private String localBindHost; // default "127.0.0.1"
    private int localPort;
    private String remoteHost;
    private int remotePort;

    // Timeouts/keepalive
    private int connectTimeoutMs;     // default 10000
    private int keepAliveIntervalSec; // default 30

    public TunnelToggleCommandMetadata(
            String name,
            String description,
            String host,
            int port,
            String username,
            TunnelAuthType authType,
            String password,
            String privateKeyPath,
            String passphrase,
            boolean strictHostKeyChecking,
            String knownHostsPath,
            String localBindHost,
            int localPort,
            String remoteHost,
            int remotePort,
            int connectTimeoutMs,
            int keepAliveIntervalSec
    ) {
        super(name, description, CommandType.TUNNEL_TOGGLE);
        this.host = host;
        this.port = port;
        this.username = username;
        this.authType = authType;
        this.password = password;
        this.privateKeyPath = privateKeyPath;
        this.passphrase = passphrase;
        this.strictHostKeyChecking = strictHostKeyChecking;
        this.knownHostsPath = knownHostsPath;
        this.localBindHost = localBindHost;
        this.localPort = localPort;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.connectTimeoutMs = connectTimeoutMs;
        this.keepAliveIntervalSec = keepAliveIntervalSec;
    }

    // Getters and setters

    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public TunnelAuthType getAuthType() {
        return authType;
    }
    public void setAuthType(TunnelAuthType authType) {
        this.authType = authType;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getPrivateKeyPath() {
        return privateKeyPath;
    }
    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }
    public String getPassphrase() {
        return passphrase;
    }
    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }
    public boolean isStrictHostKeyChecking() {
        return strictHostKeyChecking;
    }
    public void setStrictHostKeyChecking(boolean strictHostKeyChecking) {
        this.strictHostKeyChecking = strictHostKeyChecking;
    }
    public String getKnownHostsPath() {
        return knownHostsPath;
    }
    public void setKnownHostsPath(String knownHostsPath) {
        this.knownHostsPath = knownHostsPath;
    }
    public String getLocalBindHost() {
        return localBindHost;
    }
    public void setLocalBindHost(String localBindHost) {
        this.localBindHost = localBindHost;
    }
    public int getLocalPort() {
        return localPort;
    }
    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }
    public String getRemoteHost() {
        return remoteHost;
    }
    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }
    public int getRemotePort() {
        return remotePort;
    }
    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }
    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }
    public int getKeepAliveIntervalSec() {
        return keepAliveIntervalSec;
    }
    public void setKeepAliveIntervalSec(int keepAliveIntervalSec) {
        this.keepAliveIntervalSec = keepAliveIntervalSec;
    }
}
