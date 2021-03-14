package bt.gui.options;

import java.io.File;

public class Options {

    public enum LogLevel {
        NORMAL, VERBOSE, TRACE
    }

    private Boolean sequential;
    private Boolean downloadAllFiles;

    private File targetDirectory;
    private File metaInfoFile;

    private Integer port;
    private Integer dhtPort;

    private Boolean enforceEncryption;

    private String inetAddress;

    private String magnetUri;

    private Boolean seedAfterDownloaded;

    private Boolean traceLogging;
    private Boolean verboseLogging;

    public Options() {
        super();
        setEnforceEncryption(false);
        setTargetDirectory(new File("C:\\Torrent"));
        setSequential(true);
        setDownloadAllFiles(true);
    }

    public Boolean getSequential() {
        return sequential;
    }

    public void setSequential(Boolean sequential) {
        this.sequential = sequential;
    }

    public Boolean getDownloadAllFiles() {
        return downloadAllFiles;
    }

    public void setDownloadAllFiles(Boolean downloadAllFiles) {
        this.downloadAllFiles = downloadAllFiles;
    }

    public File getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public File getMetaInfoFile() {
        return metaInfoFile;
    }

    public void setMetaInfoFile(File metaInfoFile) {
        this.metaInfoFile = metaInfoFile;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getDhtPort() {
        return dhtPort;
    }

    public void setDhtPort(Integer dhtPort) {
        this.dhtPort = dhtPort;
    }

    public Boolean getEnforceEncryption() {
        return enforceEncryption;
    }

    public void setEnforceEncryption(Boolean enforceEncryption) {
        this.enforceEncryption = enforceEncryption;
    }

    public String getInetAddress() {
        return inetAddress;
    }

    public void setInetAddress(String inetAddress) {
        this.inetAddress = inetAddress;
    }

    public String getMagnetUri() {
        return magnetUri;
    }

    public void setMagnetUri(String magnetUri) {
        this.magnetUri = magnetUri;
    }

    public Boolean getSeedAfterDownloaded() {
        return seedAfterDownloaded;
    }

    public void setSeedAfterDownloaded(Boolean seedAfterDownloaded) {
        this.seedAfterDownloaded = seedAfterDownloaded;
    }

    public Boolean getTraceLogging() {
        return traceLogging;
    }

    public void setTraceLogging(Boolean traceLogging) {
        this.traceLogging = traceLogging;
    }

    public Boolean getVerboseLogging() {
        return verboseLogging;
    }

    public void setVerboseLogging(Boolean verboseLogging) {
        this.verboseLogging = verboseLogging;
    }
}
