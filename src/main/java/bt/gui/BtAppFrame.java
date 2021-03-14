package bt.gui;

import bt.Bt;
import bt.BtClientBuilder;
import bt.gui.options.Options;
import bt.gui.utils.SessionStatePrinter;
import bt.gui.utils.TorrentFileSelectorImpl;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import bt.protocol.crypto.EncryptionPolicy;
import bt.runtime.BtClient;
import bt.runtime.BtRuntime;
import bt.runtime.Config;
import bt.service.IRuntimeLifecycleBinder;
import bt.torrent.selector.PieceSelector;
import bt.torrent.selector.RarestFirstSelector;
import bt.torrent.selector.SequentialSelector;
import com.google.inject.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Optional;

public class BtAppFrame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(BtAppFrame.class);

    private JButton addTorrentBtn;
    private JLabel selectedFileLbl;

    private JButton selectDownloadLocationBtn;
    private JLabel downloadLocationLbl;

    private JButton createTorrentBtn;

    private JPanel mainPanel;
    private JLabel downloadStats;

    private JButton setSpeedBtn;

    private JLabel downloadSpeedLbl;
    private JTextField downloadSpeedField;

    private JTextField uploadSpeedField;
    private JLabel uploadSpeedLbl;

    private File file;

    private JFileChooser fc;

    private Options options;

    private SessionStatePrinter printer;
    private BtClient client;

    public BtAppFrame(String title) {
        super(title);
        setVisible(true);
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(mainPanel);

        createDefaultOptions();

        fc = createFileChooser();

        configAddTorrentButton();
        configSelectDownloadLocationButton();
        configCreateTorrentBtn();
        configSetSpeedButton();
    }

    private void createDefaultOptions() {
        options = new Options();
        downloadLocationLbl.setText("Download location: " + options.getTargetDirectory().getAbsolutePath());
    }

    private void startDownload(Options options) {
        this.printer = new SessionStatePrinter(downloadStats);

        Config config = buildConfig(options);

        BtRuntime runtime = BtRuntime.builder(config)
                .module(buildDHTModule(options))
                .autoLoadModules()
                .build();

        Storage storage = new FileSystemStorage(options.getTargetDirectory().toPath());
        PieceSelector selector = options.getSequential() ?
                SequentialSelector.sequential() : RarestFirstSelector.randomizedRarest();

        BtClientBuilder clientBuilder = Bt.client(runtime)
                .storage(storage)
                .selector(selector);

        if (!options.getDownloadAllFiles()) {
            TorrentFileSelectorImpl fileSelector = new TorrentFileSelectorImpl();
            clientBuilder.fileSelector(fileSelector);
            runtime.service(IRuntimeLifecycleBinder.class).onShutdown(fileSelector::shutdown);
        }

        clientBuilder.afterTorrentFetched(printer::onTorrentFetched);
        clientBuilder.afterFilesChosen(printer::onFilesChosen);

        if (options.getMetaInfoFile() != null) {
            clientBuilder = clientBuilder.torrent(toUrl(options.getMetaInfoFile()));
        } else if (options.getMagnetUri() != null) {
            clientBuilder = clientBuilder.magnet(options.getMagnetUri());
        } else {
            throw new IllegalStateException("Torrent file or magnet URI is required");
        }

        this.client = clientBuilder.build();
        start();
    }

    private void start() {
        printer.start();
        client.startAsync(state -> {
            boolean complete = (state.getPiecesRemaining() == 0);
            if (complete) {
                if (options.getSeedAfterDownloaded()) {
                    printer.onDownloadComplete();
                } else {
                    printer.stop();
                    client.stop();
                }
            }
            printer.updateState(state);
        }, 1000).join();
    }


    private static URL toUrl(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Unexpected error", e);
        }
    }

    private static Module buildDHTModule(Options options) {
        Optional<Integer> dhtPortOverride = tryGetPort(options.getDhtPort());

        return new DHTModule(new DHTConfig() {
            @Override
            public int getListeningPort() {
                return dhtPortOverride.orElseGet(super::getListeningPort);
            }

            @Override
            public boolean shouldUseRouterBootstrap() {
                return true;
            }
        });
    }

    private static Optional<InetAddress> getAcceptorAddressOverride(Options options) {
        String inetAddress = options.getInetAddress();
        if (inetAddress == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(InetAddress.getByName(inetAddress));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Failed to parse the acceptor's internet address", e);
        }
    }

    private static Optional<Integer> tryGetPort(Integer port) {
        if (port == null) {
            return Optional.empty();
        } else if (port < 1024 || port > 65535) {
            throw new IllegalArgumentException("Invalid port: " + port + "; expected 1024..65535");
        }
        return Optional.of(port);
    }

    private static Config buildConfig(Options options) {
        Optional<InetAddress> acceptorAddressOverride = getAcceptorAddressOverride(options);
        Optional<Integer> portOverride = tryGetPort(options.getPort());

        return new Config() {
            @Override
            public InetAddress getAcceptorAddress() {
                return acceptorAddressOverride.orElseGet(super::getAcceptorAddress);
            }

            @Override
            public int getAcceptorPort() {
                return portOverride.orElseGet(super::getAcceptorPort);
            }

            @Override
            public int getNumOfHashingThreads() {
                return Runtime.getRuntime().availableProcessors();
            }

            @Override
            public EncryptionPolicy getEncryptionPolicy() {
                return options.getEnforceEncryption() ? EncryptionPolicy.REQUIRE_ENCRYPTED : EncryptionPolicy.PREFER_PLAINTEXT;
            }
        };
    }

    private void configAddTorrentButton() {
        addTorrentBtn.addActionListener(e -> {
            if (e.getSource() == addTorrentBtn) {
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                if (fc.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
                    file = fc.getSelectedFile();
                    options.setMetaInfoFile(file);
                    logger.warn("Opening: " + file.getName() + ".");
                    selectedFileLbl.setVisible(true);
                    selectedFileLbl.setText("Selected file: " + file.getName());
                    startDownload(options);
                } else {
                    logger.warn("Open command cancelled by user.");
                }
            }
        });
    }

    private void configSelectDownloadLocationButton() {
        selectDownloadLocationBtn.addActionListener(e -> {
            if (e.getSource() == selectDownloadLocationBtn) {
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = fc.showOpenDialog(mainPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File currDir = fc.getSelectedFile();
                    options.setTargetDirectory(currDir);
                    downloadLocationLbl.setText("Download location: " + currDir.getAbsolutePath());
                    logger.warn("Download location set: " + currDir.getAbsolutePath());
                    selectedFileLbl.setVisible(true);
                } else {
                    logger.warn("Open command cancelled by user.");
                }
            }
        });
    }

    private void configCreateTorrentBtn() {
        downloadSpeedField.setSize(100, 20);
        uploadSpeedField.setSize(100, 20);
        createTorrentBtn.addActionListener(e -> {
            //TODO Create torrent action

        });
    }

    private void configSetSpeedButton() {
        setSpeedBtn.addActionListener(e -> {
            //TODO add speed to options
        });
    }

    private JFileChooser createFileChooser() {
        fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        fc.setFileFilter(new FileNameExtensionFilter("Torrent files", "torrent"));
        return fc;
    }
}
