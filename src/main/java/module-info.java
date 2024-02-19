module com.keremyurekli.unnecessarilycomplicatedactions {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.fxmisc.richtext;
    requires com.github.kwhat.jnativehook;


    opens com.keremyurekli.unnecessarilycomplicatedactions to javafx.fxml;
    exports com.keremyurekli.unnecessarilycomplicatedactions;
}