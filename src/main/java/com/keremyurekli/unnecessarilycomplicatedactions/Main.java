package com.keremyurekli.unnecessarilycomplicatedactions;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.fxmisc.richtext.CodeArea;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Main extends Application implements NativeKeyListener {

    private Stage mainStage;

    private Robot robot = new Robot();

    public static String byteToString(byte b) {
        byte[] masks = {-128, 64, 32, 16, 8, 4, 2, 1};
        StringBuilder builder = new StringBuilder();
        for (byte m : masks) {
            if ((b & m) == m) {
                builder.append('1');
            } else {
                builder.append('0');
            }
        }
        return builder.toString();
    }

    public static String readClipboard() {
        try {
            return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        launch();
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        var modifiers = e.getModifiers();
        var isModifierPressed = modifiers == NativeInputEvent.CTRL_L_MASK;
        if (e.getKeyCode() == NativeKeyEvent.VC_C) {
            if (isModifierPressed) {
                if (mainStage.isShowing()) {
                    return;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                Platform.runLater(this::openCopyScene);
            }
        } else if (e.getKeyCode() == NativeKeyEvent.VC_V) {
            if (isModifierPressed) {
                Platform.runLater(this::openPasteScene);
            }
        }


    }

    public void nativeKeyReleased(NativeKeyEvent e) {

    }

    public void nativeKeyTyped(NativeKeyEvent e) {

    }

    @Override
    public void start(Stage stage) throws IOException {
        this.mainStage = stage;
        mainStage.initStyle(StageStyle.TRANSPARENT);
        mainStage.setAlwaysOnTop(true);

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());

            System.exit(1);
        }

        GlobalScreen.addNativeKeyListener(this);
        Platform.setImplicitExit(false);
    }


    public void openCopyScene() {
        String copiedString = readClipboard();

        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(
                        new StringSelection(""),
                        null);

        StringBuilder binary = new StringBuilder();
        byte[] encoded = copiedString.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < encoded.length; i++) {
            byte b = encoded[i];
            binary.append(byteToString(b) + " ");

        }


        StackPane stackPane = new StackPane();
        stackPane.setStyle("-fx-background-color: rgba(43, 45, 48, 0.8); " +
                "-fx-background-radius: 15;");


        Label firstLabel = new Label("Start typing the binary to copy");
        firstLabel.setStyle("-fx-text-fill: white;");

        CodeArea inputField = new CodeArea();
        inputField.setMaxSize(380, 130);
        inputField.setWrapText(true);
        inputField.setStyle("-fx-background-color: green");
        inputField.appendText(binary.toString());
        //inputField.setDisable(true);
        inputField.setEditable(false);
        inputField.setOnMouseClicked(mouseEvent -> {
            inputField.requestFocus();
        });


        AtomicInteger caretPosition = new AtomicInteger();
        inputField.setOnKeyPressed(keyEvent -> {
            String keyCode = keyEvent.getText();
            if (Objects.equals(keyCode, "0") || Objects.equals(keyCode, "1")) {
//                System.out.println(keyCode);
                if (caretPosition.get() < binary.length()) {

                    StringBuilder stringBuilder = new StringBuilder(inputField.getText());
                    stringBuilder.setCharAt(caretPosition.get(), keyCode.charAt(0));
                    boolean truePlacement = false;
                    if (keyCode.charAt(0) == binary.charAt(caretPosition.get())) {
                        truePlacement = true;
                    }

                    inputField.clear();
                    inputField.appendText(stringBuilder.toString());


                    if (truePlacement) {
//                    System.out.println("TRUE PLACEMENT");
                        //inputField.setStyle(caretPosition.get(),caretPosition.get()+1, Collections.singleton("green"));
                        if (caretPosition.get() == binary.length() - 2) {
//                        System.out.println("ENDING");
                            boolean perfect = true;
                            for (int i = 0; i < caretPosition.get(); i++) {
                                char trueBinary = binary.charAt(i);

                                if (stringBuilder.charAt(i) != trueBinary) {
                                    perfect = false;
                                    break;
                                }
                            }
                            if (perfect) {
                                Toolkit.getDefaultToolkit()
                                        .getSystemClipboard()
                                        .setContents(
                                                new StringSelection(copiedString),
                                                null);
                                mainStage.hide();
                                mainStage.setScene(null);
                                //
                            }

                        }
                    } else {
                        //inputField.setStyle(caretPosition.get(),caretPosition.get()+1, Collections.singleton("red"));
                    }

                    if (stringBuilder.charAt(caretPosition.get() + 1) == ' ') {
                        caretPosition.getAndAdd(2);
                    } else {
                        caretPosition.getAndIncrement();
                    }
                    //colorizing
                    for (int i = 0; i < caretPosition.get(); i++) {
                        char trueBinary = binary.charAt(i);

                        if (stringBuilder.charAt(i) == trueBinary) {
                            inputField.setStyle(i, i + 1, Collections.singleton("green"));
                        } else {
                            inputField.setStyle(i, i + 1, Collections.singleton("red"));
                        }

                    }
                }
                //colorizing

            } else if (keyEvent.getCode().equals(KeyCode.BACK_SPACE)) {
//                System.out.println(keyCode);
                StringBuilder stringBuilder = new StringBuilder(inputField.getText());
                if (caretPosition.get() != 0) {
                    if (stringBuilder.charAt(caretPosition.get() - 1) != ' ') {
                        stringBuilder.setCharAt(caretPosition.get() - 1, binary.charAt(caretPosition.get() - 1));
                    } else {
                        stringBuilder.setCharAt(caretPosition.get() - 2, binary.charAt(caretPosition.get() - 2));
                    }
                } else {
                    stringBuilder.setCharAt(0, binary.charAt(0));
                }


                inputField.clear();
                inputField.appendText(stringBuilder.toString());

                if (caretPosition.get() != 0) {
                    if (caretPosition.get() > 1 && stringBuilder.charAt(caretPosition.get() - 1) == ' ') {
                        caretPosition.getAndAdd(-2);
                    } else {
                        caretPosition.getAndDecrement();
                    }
                }
                //colorizing
                for (int i = 0; i < caretPosition.get(); i++) {
                    char trueBinary = binary.charAt(i);

                    if (stringBuilder.charAt(i) == trueBinary) {
                        inputField.setStyle(i, i + 1, Collections.singleton("green"));
                    } else {
                        inputField.setStyle(i, i + 1, Collections.singleton("red"));
                    }
                }
                //colorizing
            }
//            System.out.println(caretPosition.get());

        });


        inputField.setStyle("-fx-background-color: rgb(255,255,255, 1); ");

        Label secondLabel = new Label("OR");
        secondLabel.setStyle("-fx-text-fill: white;");

        Button firstButton = new Button("ABANDON YOUR KNOWLEDGE");
        firstButton.setMaxWidth(190);
        firstButton.setOnAction(actionEvent -> {
            mainStage.hide();
            mainStage.setScene(null);
        });


        stackPane.getChildren().addAll(firstLabel, inputField, secondLabel, firstButton);
        stackPane.getChildren().forEach(node -> {
            StackPane.setAlignment(node, Pos.TOP_CENTER);
        });
        firstLabel.setTranslateY(5);
        inputField.setTranslateY(30);
        secondLabel.setTranslateY(165);
        firstButton.setTranslateY(190);


        Scene scene = new Scene(stackPane, 420, 225);

        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());
        this.mainStage.setScene(scene);
        this.mainStage.show();
    }

    public void openPasteScene() {
//        StackPane stackPane = new StackPane();
//        stackPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); " +
//                "-fx-background-radius: 15;");
//
//
//        Scene scene = new Scene(stackPane, 420, 220);
//
//        scene.setFill(Color.TRANSPARENT);
//        scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());
    }


}