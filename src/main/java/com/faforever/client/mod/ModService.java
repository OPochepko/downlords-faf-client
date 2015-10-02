package com.faforever.client.mod;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface ModService {

  ObservableSet<ModInfoBean> getAvailableMods();

  void loadInstalledMods();

  ObservableList<ModInfoBean> getInstalledMods();

  CompletableFuture<Void> downloadAndInstallMod(URL url);

  CompletableFuture<Void> downloadAndInstallMod(URL url, DoubleProperty progressProperty, StringProperty titleProperty);

  CompletableFuture<Void> downloadAndInstallMod(ModInfoBean modInfoBean, DoubleProperty progressProperty, StringProperty titleProperty);

  Set<String> getInstalledModUids();

  Set<String> getInstalledUiModsUids();

  void enableSimMods(Set<String> simMods) throws IOException;

  /**
   * Requests the server to send mod information. Since the server sends them one by one and it's unknown how many, this
   * method does not return a future. Instead, callers should listen on the set returned by {@link
   * #getAvailableMods()}.
   */
  void requestMods();

  boolean isModInstalled(String uid);

  CompletableFuture<Void> uninstallMod(ModInfoBean mod);

  Path getPathForMod(ModInfoBean mod);
}
