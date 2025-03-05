///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS org.openjfx:javafx-controls:23.0.2
//DEPS org.openjfx:javafx-graphics:23.0.2:${os.detected.jfxname}
//DEPS org.apache.commons:commons-math3:3.6.1
//DEPS org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5
//DEPS org.controlsfx:controlsfx:11.2.1

/*
Frequency detection written by Jose Hidalgo, https://coderanch
.com/t/618633/java/Basic-FFT-identity-frequency
*/

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.*;
import org.controlsfx.control.ToggleSwitch;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class Publish extends Application {

    private boolean brokerButtonClicked = false;
    public static boolean isActive = false;

    @Override
    public void start(Stage stage) {
        VBox holder = new VBox();
        TextArea textArea = new TextArea();
        ImageView imageView = new ImageView();
        FlowPane flowPane = new FlowPane();
        TextField textField = new TextField();
        Button brokerButton = new Button("Broker");
        Region brokerRegion = new Region();
        ToggleGroup radioGroup = new ToggleGroup();
        RadioButton sopranoButton = new RadioButton("Soprano");
        Region radioRgion = new Region();
        RadioButton tenorButton = new RadioButton("Tenor");
        Region vocalRangeRegion = new Region();
        Button startButton = new Button("Start");
        Region region = new Region();
        ToggleSwitch canPublish = new ToggleSwitch("Pause");
        Button stopButton = new Button("Quit");
        Scene scene = new Scene(holder);

        textArea.setFont(Font.font("Times New Roman", 32));
        textArea.appendText("IP address NOT set yet\nSelect vocal range\nStart button NOT pressed yet\nPublishing paused\n");
        holder.getChildren().add(textArea);

        Image image = new Image("file:///home/bburd/Documents/IoT_Java" +
                "/final/RaspberryPi.png");
        imageView.setImage(image);
        holder.getChildren().add(imageView);

        textField.setText("192.168.8.139");
        flowPane.getChildren().add(textField);

        brokerButton.setOnAction(e -> {
            Publisher.broker = "tcp://" + textField.getText() + ":1883";
            textArea.appendText("Broker set to " + textField.getText() + "\n");
            setBrokerButtonClicked(true);
            //if (radioGroup.getSelectedToggle() != null) {
            startButton.setDisable(false);
            //}
        });
        flowPane.getChildren().add(brokerButton);

        brokerRegion.setMaxHeight(10.0);
        brokerRegion.setMinWidth(100.0);
        flowPane.getChildren().add(brokerRegion);

        sopranoButton.setToggleGroup(radioGroup);
        flowPane.getChildren().add(sopranoButton);

        radioRgion.setMaxHeight(10.0);
        radioRgion.setMinWidth(30.0);
        flowPane.getChildren().add(radioRgion);

        tenorButton.setToggleGroup(radioGroup);
        flowPane.getChildren().add(tenorButton);

        radioGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> event,
                                Toggle old_toggle, Toggle new_toggle) {
                Publisher.isSoprano = new_toggle == sopranoButton;
                //if (isBrokerButtonClicked()) {
                //    startButton.setDisable(false);
                //}
                textArea.appendText("isSoprano: " +  Publisher.isSoprano + "\n");
            }
        });
        radioGroup.selectToggle(sopranoButton);

        vocalRangeRegion.setMaxHeight(10.0);
        vocalRangeRegion.setMinWidth(100.0);
        flowPane.getChildren().add(vocalRangeRegion);

        startButton.setOnAction(e -> {
            Thread thread = new Thread(() -> AudioInput.listen(textArea));
            thread.start();
            textArea.appendText("Start button pressed\n");
            textArea.appendText("Listening thread started\n");
        });
        startButton.setDisable(true);                    
        flowPane.getChildren().add(startButton); 


        canPublish.selectedProperty().addListener((observable, oldValue, newValue) -> {

                isActive = canPublish.isSelected();
                canPublish.setText(isActive ? "Active" : "Pause");            
                textArea.appendText("isActive: " + isActive + "\n");

        });
        canPublish.setSelected(false);
        flowPane.getChildren().add(canPublish);
        
        region.setMaxHeight(10.0);
        region.setMinWidth(30.0);
        flowPane.getChildren().add(region);

        stopButton.setOnAction(e -> {
            System.exit(0);
        });
        flowPane.getChildren().add(stopButton);

        holder.getChildren().add(flowPane);
        stage.setTitle("Publish");
        stage.setScene(scene);
        stage.show();
    }

    boolean isBrokerButtonClicked() {
        return brokerButtonClicked;
    }

    void setBrokerButtonClicked(boolean value) {
        brokerButtonClicked = value;
    }

    public static void main(String[] args) {
        launch();
    }
}

class Publisher {

    String topic = "MusicTones";
    int qos = 2;
    static String broker = "tcp://192.168.50.139:1883";
    static boolean isSoprano;
    String clientId = "thePublisher";
    MemoryPersistence persistence = new MemoryPersistence();
    MqttClient publishingClient;

    TextArea textArea;

    public Publisher(TextArea textArea) {

        this.textArea = textArea;

        try {
            publishingClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            textArea.appendText("Connecting to broker: " + broker + "\n");
            publishingClient.connect(connOpts);
            textArea.appendText("Connected\n");
        } catch (MqttException me) {
            me.printStackTrace();
        }
    }

    public void send(String content) {
        textArea.appendText("Publishing message: " + content + "\n");
        try {
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            publishingClient.publish(topic, message);
        } catch (MqttException me) {
            me.printStackTrace();
        }
    }
}

class AudioInput {

    TargetDataLine microphone;
    final int audioFrames = 8192;
    final float sampleRate = 8000.0f;
    final int bitsPerRecord = 16;
    final int channels = 1;
    final boolean bigEndian = true;
    final boolean signed = true;

    byte byteData[];     // length=audioFrames * 2
    double doubleData[];   // length=audioFrames only reals needed for
    // apache lib.
    AudioFormat format;
    FastFourierTransformer transformer;

    boolean sendToPublish = true;

    Publisher publisher;
    TextArea textArea;

    public AudioInput(TextArea textArea) {

        this.textArea = textArea;
        publisher = new Publisher(textArea);

        byteData = new byte[audioFrames * 2];  //two bytes per audio frame,
        // 16 bits

        doubleData = new double[audioFrames];  // only real for apache

        transformer = new FastFourierTransformer(DftNormalization.STANDARD);

        textArea.appendText("Microphone initialization\n");
        format = new AudioFormat(sampleRate, bitsPerRecord, channels,
                signed, bigEndian);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class,
                format); // format is an AudioFormat object

        if (!AudioSystem.isLineSupported(info)) {
            System.err.print("isLineSupported failed");
            System.exit(1);
        }

        try {
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            textArea.appendText("Microphone opened with format: " + format.toString() + "\n");
            microphone.start();
        } catch (Exception ex) {
            textArea.appendText("Microphone failed: " + ex.getMessage() +
                    "\n");
            System.exit(1);
        }
    }

    public int readPcm() {
        int numBytesRead =
                microphone.read(byteData, 0, byteData.length);
        if (numBytesRead != byteData.length) {
            System.out.println("Warning: read less bytes than buffer size");
            System.exit(1);
        }
        return numBytesRead;
    }


    public void byteToDouble() {
        ByteBuffer buf = ByteBuffer.wrap(byteData);
        buf.order(ByteOrder.BIG_ENDIAN);
        int i = 0;

        while (buf.remaining() > 2) {
            short s = buf.getShort();
            doubleData[i] = (double) s; //(new Short(s)).doubleValue();
            ++i;
        }
    }


    public void findFrequency() {
        double frequency;

        for (int i = 0; i < doubleData.length; i++) {
            doubleData[i] *= 0.5 * (1 - Math.cos(2 * Math.PI * i / (doubleData.length - 1))); // Hann window
        }

        Complex[] cmplx = transformer.transform(doubleData,
                TransformType.FORWARD);
        double real;
        double im;
        double mag[] = new double[cmplx.length];

        for (int i = 0; i < cmplx.length; i++) {
            real = cmplx[i].getReal();
            im = cmplx[i].getImaginary();
            mag[i] = Math.sqrt((real * real) + (im * im));
        }

        double peak = -1.0;
        int index = -1;
        for (int i = 0; i < cmplx.length / 2; i++) {
            if (peak < mag[i]) {
                index = i;
                peak = mag[i];
            }
        }
        frequency = (sampleRate * index) / audioFrames;
        System.err.println(frequency);
        textArea.appendText("" + frequency + " Hz " + 
            (sendToPublish ? "" : " Waiting for 13 seconds ") + (Publish.isActive ? "" : " Paused ") + "\n");
        int intFrequency = (int) frequency;

        String word;

     if (sendToPublish && Publish.isActive) {
        if (intFrequency >= 436 && intFrequency <= 444) {
            textArea.appendText("break\n");
            publisher.send("break");
            delay();
        } else if (intFrequency >= 344 && intFrequency <= 353) {
            word = (Publisher.isSoprano) ? "call" : "break";           
            textArea.appendText(word + "\n");
            publisher.send(word);
            delay();
        } else if (intFrequency >= 257 && intFrequency <= 265) {
            word = (Publisher.isSoprano) ? "fly" : "call";
            textArea.appendText(word + "\n");
            publisher.send(word);
            delay();
        } else if ((intFrequency >= 192 && intFrequency <= 200) || (intFrequency >= 436 && intFrequency <= 444)) {
            textArea.appendText("fly\n");
            publisher.send("fly");
            delay();
        }
     }
    }


    public void delay() {
        new Thread(() -> {
            sendToPublish = false;
            try {
                Thread.sleep(13000);
            } catch (InterruptedException e) {
            }
            sendToPublish = true;
        }).start();
    }


    public static void listen(TextArea textArea) {

        AudioInput ai = new AudioInput(textArea);
        while (true) {
            ai.readPcm();
            ai.byteToDouble();
            ai.findFrequency();
        }
    }
}