package web;

import service.FileStorageService;
import service.GameDataManager;
import util.DataInitializer;

import java.awt.Desktop;
import java.io.IOException;
import java.net.BindException;
import java.net.URI;
import java.nio.file.Path;

public class WebMain {
    private static final Path DATA_DIR = Path.of("data");
    private static final Path WEB_DIR = Path.of("web");

    public static void main(String[] args) throws Exception {
        boolean openBrowser = true;
        for (String arg : args) {
            if ("--no-open".equals(arg)) {
                openBrowser = false;
            }
        }

        GameDataManager dataManager = loadOrCreateData();
        WebServer server = null;
        int port = 8080;
        for (; port <= 8090; port++) {
            try {
                server = new WebServer(dataManager, DATA_DIR, WEB_DIR);
                server.start(port);
                break;
            } catch (BindException ex) {
                server = null;
            }
        }
        if (server == null) {
            throw new IllegalStateException("No free local port found in range 8080-8090.");
        }

        String url = "http://127.0.0.1:" + port + "/";
        System.out.println("JavaCW web frontend running at " + url);
        System.out.println("Close this terminal window or press Ctrl+C to stop the server.");
        if (openBrowser) {
            openBrowser(url);
        }

        Thread.currentThread().join();
    }

    private static GameDataManager loadOrCreateData() {
        FileStorageService storageService = new FileStorageService();
        try {
            if (storageService.hasDataFiles(DATA_DIR)) {
                System.out.println("Loaded data from " + DATA_DIR);
                return storageService.loadAll(DATA_DIR);
            }
            GameDataManager sample = DataInitializer.createSampleData();
            storageService.saveAll(sample, DATA_DIR);
            System.out.println("Created sample data in " + DATA_DIR);
            return sample;
        } catch (IOException | RuntimeException ex) {
            System.out.println("Could not load CSV data: " + ex.getMessage());
            System.out.println("Using built-in sample data for this web session.");
            return DataInitializer.createSampleData();
        }
    }

    private static void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            }
        } catch (IOException | RuntimeException ex) {
            System.out.println("Open this URL manually if the browser did not open: " + url);
        }
    }
}
