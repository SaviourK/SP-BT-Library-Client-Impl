package bt.gui.utils;

import bt.metainfo.TorrentFile;
import bt.torrent.fileselector.SelectionResult;
import bt.torrent.fileselector.TorrentFileSelector;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class TorrentFileSelectorImpl extends TorrentFileSelector {

    private final AtomicReference<Thread> currentThread;
    private final AtomicBoolean shutdown;

    public TorrentFileSelectorImpl() {
        this.currentThread = new AtomicReference<>(null);
        this.shutdown = new AtomicBoolean(false);
    }

    @Override
    protected SelectionResult select(TorrentFile file) {
        return null;
    }

    public void shutdown() {
        this.shutdown.set(true);
        Thread currentThread = this.currentThread.get();
        if (currentThread != null) {
            currentThread.interrupt();
        }
    }
}
