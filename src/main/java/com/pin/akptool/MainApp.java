package com.pin.akptool;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MainApp extends Application {

    // 输出文件信息的区域
    private TextArea outputArea;

    @Override
    public void start(Stage primaryStage) {

        // 1. 按钮
        Button chooseButton = new Button("选择 APK 文件…");

        // 2. 文本区域
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setPrefRowCount(8);

        // 3. 布局：上面按钮，下面文本框
        VBox root = new VBox(10, chooseButton, outputArea);
        root.setPadding(new Insets(10));

        // 4. 场景和窗口
        Scene scene = new Scene(root, 700, 500);
        primaryStage.setTitle("APKAnalyzerX");
        primaryStage.setScene(scene);
        primaryStage.show();

        // 5. 事件绑定
        chooseButton.setOnAction(event -> chooseApkFile(primaryStage));
    }

    private void chooseApkFile(Stage owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择 APK 文件");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("APK 文件 (*.apk)", "*.apk")
        );

        File file = fileChooser.showOpenDialog(owner);
        if (file == null) {
            return;
        }

        long sizeBytes = file.length();
        double sizeMB = sizeBytes / 1024.0 / 1024.0;

        StringBuilder sb = new StringBuilder();
        sb.append("已选择文件：").append(file.getAbsolutePath()).append("\n");
        sb.append("文件大小：").append(sizeBytes).append(" 字节 (~");
        sb.append(String.format(Locale.CHINA, "%.2f", sizeMB)).append(" MB)\n");

        outputArea.setText(sb.toString());

        analyzeApk(file);
    }

    private void analyzeApk(File apkFile) {
        outputArea.appendText("---- APK 内容（zip 条目）----\n");

        try (ZipFile zipFile = new ZipFile(apkFile)) {
            List<? extends ZipEntry> entries = Collections.list(zipFile.entries());

            int dirCount = 0;
            int fileCount = 0;

            for (ZipEntry entry : entries) {
                String name = entry.getName();

                if (entry.isDirectory()) {
                    dirCount++;
                    outputArea.appendText("[D] " + name + "\n");
                } else {
                    fileCount++;
                    outputArea.appendText("[F] " + name + "\n");
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
