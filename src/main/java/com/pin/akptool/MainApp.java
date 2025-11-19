package com.pin.akptool;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MainApp extends Application {

    // 左侧目录树
    private TreeView<String> treeView;

    // 右侧输出区域
    private TextArea outputArea;

    @Override
    public void start(Stage primaryStage) {

        // 1. 按钮
        Button chooseButton = new Button("选择 APK 文件…");

        // 2. 左侧 TreeView
        treeView = new TreeView<>();
        treeView.setPrefWidth(260);

        // 3. 右侧文本区域
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);

        // 4. 中间用 SplitPane 左右分栏
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(treeView, outputArea);
        splitPane.setDividerPositions(0.3); // 左右宽度比例：左 30%，右 70%

        // 5. 整体用 VBox：上面一行按钮，下面是左右分栏
        VBox root = new VBox(10, chooseButton, splitPane);
        root.setPadding(new Insets(10));

        // 6. 场景和窗口
        Scene scene = new Scene(root, 900, 600);
        primaryStage.setTitle("APKAnalyzerX");
        primaryStage.setScene(scene);
        primaryStage.show();

        // 7. 事件绑定
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
        sb.append(String.format(Locale.CHINA, "%.2f", sizeMB)).append(" MB)\n\n");

        outputArea.setText(sb.toString());

        analyzeApk(file);
    }

    private void analyzeApk(File apkFile) {

        // 构建 TreeView 的根节点
        TreeItem<String> rootItem = new TreeItem<>("APK 内容");
        rootItem.setExpanded(true);
        treeView.setRoot(rootItem);

        try (ZipFile zipFile = new ZipFile(apkFile)) {
            List<? extends ZipEntry> entries = Collections.list(zipFile.entries());

            for (ZipEntry entry : entries) {
                String name = entry.getName();

                if (entry.isDirectory()) {
                    // 明确目录条目（大部分 apk 没有，这里兼容一下）
                    getOrCreateNode(rootItem, name);
                } else {
                    int lastSlash = name.lastIndexOf("/");
                    if (lastSlash != -1) {
                        String dir = name.substring(0, lastSlash);
                        String fileName = name.substring(lastSlash + 1);

                        TreeItem<String> dirNode = getOrCreateNode(rootItem, dir);
                        dirNode.getChildren().add(new TreeItem<>(fileName));
                    } else {
                        // 根目录文件
                        rootItem.getChildren().add(new TreeItem<>(name));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            outputArea.appendText("\n解析失败：" + e.getMessage());
        }
    }

    // 根据路径在树上找/建一个节点，例如：res/layout → 创建 "res" → "layout"
    private TreeItem<String> getOrCreateNode(TreeItem<String> root, String path) {
        String[] parts = path.split("/");

        TreeItem<String> current = root;

        for (String part : parts) {
            if (part.isEmpty()) continue;

            TreeItem<String> found = null;
            for (TreeItem<String> child : current.getChildren()) {
                if (child.getValue().equals(part)) {
                    found = child;
                    break;
                }
            }

            if (found == null) {
                found = new TreeItem<>(part);
                current.getChildren().add(found);
            }

            current = found;
        }
        return current;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
