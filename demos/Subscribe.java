package com.example.subscribefx;

import com.twilio.Twilio;
import com.twilio.base.ResourceSet;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import javafx.scene.control.TextArea;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.joda.time.DateTime;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

/*
Sound Effects Credits:

Sound Effect by <a href="https://pixabay
.com/users/alice_soundz-44907632/?utm_source=link-attribution&utm_medium
=referral&utm_campaign=music&utm_content=224091">Alice_soundz</a>
from <a href="https://pixabay.com//?utm_source=link-attribution&utm_medium
=referral&utm_campaign=music&utm_content=224091">Pixabay</a>
Sound Effect by <a href="https://pixabay
.com/users/ribhavagrawal-39286533/?utm_source=link-attribution&utm_medium
=referral&utm_campaign=music&utm_content=293316">Ribhav Agrawal</a> from <a
 href="https://pixabay.com/sound-effects//?utm_source=link-attribution
 &utm_medium=referral&utm_campaign=music&utm_content=293316">Pixabay</a
 >http://static.fullstackpython.com/phone-calls-python.xml";
Horn sound effect:
Sound Effect by <a href="https://pixabay
.com/users/waitwhatimsignedin-47519502/?utm_source=link-attribution
&utm_medium=referral&utm_campaign=music&utm_content=273892
">Waitwhatimsignedin</a> from <a href="https://pixabay
.com//?utm_source=link-attribution&utm_medium=referral&utm_campaign=music
&utm_content=273892">Pixabay</a>
Cow sound effect:
Sound Effect by <a href="https://pixabay
.com/users/ribhavagrawal-39286533/?utm_source=link-attribution&utm_medium
=referral&utm_campaign=music&utm_content=293301">Ribhav Agrawal</a> from <a
 href="https://pixabay.com/sound-effects//?utm_source=link-attribution
 &utm_medium=referral&utm_campaign=music&utm_content=293301">Pixabay</a>
Rooster:
Sound Effect by <a href="https://pixabay
.com/users/ribhavagrawal-39286533/?utm_source=link-attribution&utm_medium
=referral&utm_campaign=music&utm_content=293308">Ribhav Agrawal</a> from <a
 href="https://pixabay.com//?utm_source=link-attribution&utm_medium
 =referral&utm_campaign=music&utm_content=293308">Pixabay</a>
BoingWav:
Sound Effect by <a href="https://pixabay
.com/users/freesound_community-46691455/?utm_source=link-attribution
&utm_medium=referral&utm_campaign=music&utm_content=89698
">freesound_community</a> from <a href="https://pixabay
.com//?utm_source=link-attribution&utm_medium=referral&utm_campaign=music
&utm_content=89698">Pixabay</a>
Sick player jump:
Sound Effect by <a href="https://pixabay
.com/users/freesound_community-46691455/?utm_source=link-attribution
&utm_medium=referral&utm_campaign=music&utm_content=81114
">freesound_community</a> from <a href="https://pixabay
.com/sound-effects//?utm_source=link-attribution&utm_medium=referral
&utm_campaign=music&utm_content=81114">Pixabay</a>
Applause:
Sound Effect by <a href="https://pixabay
.com/users/roesisch-46625437/?utm_source=link-attribution&utm_medium
=referral&utm_campaign=music&utm_content=253125">Dario Krobath</a> from <a
href="https://pixabay.com/sound-effects//?utm_source=link-attribution
&utm_medium=referral&utm_campaign=music&utm_content=253125">Pixabay</a>
 */

public class Subscribe {

    TextArea textArea;
    static Set<String> dialNumbers = new HashSet<>();

    public Subscribe(TextArea textArea) {
        this.textArea = textArea;
        dialNumbers.add("+12155152873");

        String topic = "MusicTones";
        int qos = 2;
        String broker = "tcp://localhost:1883";
        String clientId = "theSubscriber";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient subscribingClient = new MqttClient(broker, clientId,
                    persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            textArea.appendText("Start button pressed\n");
            textArea.appendText("Connecting to broker:" + broker + "\n");
            subscribingClient.connect(connOpts);
            textArea.appendText("Connected\n");

            subscribingClient.setCallback(new MqttCallback() {

                @Override
                public void connectionLost(Throwable cause) {
                    textArea.appendText("Connection lost\n");
                }

                @Override
                public void messageArrived(String topic,
                                           MqttMessage message) {
                    var payload = new String(message.getPayload());
                    switch (payload) {
                        case "fly" -> fly();
                        case "call" -> call();
                        case "break" -> breakGlass();
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) { //
                }
            });

            subscribingClient.subscribe(topic, 2);
        } catch (MqttException me) {
            me.printStackTrace();
        }
    }

    static void loadPhones(TextArea textArea) {
        //LocalDateTime dateFilter = LocalDateTime.now().minusDays(1);
        //Date filterDate = Date.from(dateFilter.atZone(ZoneId.systemDefault()).toInstant());
        //DateTime filterDate = new DateTime();
        Twilio.init("xxx",
                "xxx");
        ZonedDateTime filterDate = ZonedDateTime.now().minus(2, ChronoUnit.HOURS);

        // Get messages and filter them
        ResourceSet<Message> messages = Message.reader()
                .setDateSentAfter(filterDate)
                .setTo(new PhoneNumber("+12346010100"))
                .read();

        // Use a Set to avoid duplicates
        //Set<String> matchingNumbers = new HashSet<>(); //////////////

        int phonesCount = 0;
        for (Message message : messages) {
            String messageBody = message.getBody().toLowerCase();

            if (messageBody.contains("java")) {
                dialNumbers.add(message.getFrom().toString());
            }
            phonesCount++;
        }
        HelloApplication.startButton.setDisable(false);
        textArea.appendText(phonesCount + " phone(s) loaded\n");
    }

    void fly() {
        try {
            textArea.appendText("Flying\n");
            Runtime.getRuntime().exec("/Users/bburd/PycharmProjects" +
                    "/PythonProject/.venv/bin/python " +
                    "/Users/bburd/PycharmProjects/PythonProject/flight4.py");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void call() {
        final String TWILIO_PHONE_NUMBER = "+12346010100";
        final String TWIML_INSTRUCTIONS_URL = "https://burdbooks" +
                ".com/Response.xml";


        Twilio.init("xxx",
                "xxx");
        Call call = null;
        for (String number : dialNumbers) {
            textArea.appendText("Dialing ###-###-" + number.substring(8) + "\n");
            try {
                call = Call.creator(
                        new PhoneNumber(number), // To phone number
                        new PhoneNumber(TWILIO_PHONE_NUMBER), // From Twilio
                        // phone number
                        new URI(TWIML_INSTRUCTIONS_URL) // URL for TwiML
                        // instructions
                ).create();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }


    }

    void playSound() {
        try {
            AudioInputStream audioInputStream =
                    AudioSystem.getAudioInputStream(new File("/Users/bburd" +
                            "/Documents/IoT_Java/SubscribeFX/src/main" +
                            "/resources/com/example/subscribefx/glass" +
                            "-breaking-224091.aif").getAbsoluteFile());

            Clip clip = AudioSystem.getClip();

            clip.open(audioInputStream);

            clip.loop(0);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    void breakGlass() {
        SwingUtilities.invokeLater(() -> {
            playSound();
            try {
                BufferedImage image = ImageIO.read(new File("/Users" +
                        "/bburd/Pictures" +
                        "/BrokenMirrorTransparent2200.png"));
                if (image == null) {
                    throw new IllegalArgumentException("Image file " +
                            "not " +
                            "found or invalid format.");
                }

                JWindow window = new JWindow();
                window.setAlwaysOnTop(true);
                window.setBackground(new Color(0, 0, 0, 0));
                window.setBounds(100, 100, image.getWidth(),
                        image.getHeight());

                JPanel panel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setComposite(AlphaComposite.SrcOver);
                        g2d.drawImage(image, 0, 0, null);
                    }
                };
                panel.setOpaque(false);
                var button = new JButton("Close");
                button.setOpaque(false);
                button.setContentAreaFilled(false);
                button.setBorderPainted(false);
                button.addActionListener(e -> {
                    window.dispose();
                });
                panel.add(button);
                window.add(panel);


                window.addKeyListener(new java.awt.event.KeyAdapter() {
                    @Override
                    public void keyPressed(java.awt.event.KeyEvent e) {
                        System.out.println("Key pressed");
                        if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                            window.dispose();
                        }
                    }
                });

                textArea.appendText("You broke the screen!\n");
                window.setVisible(true);

                window.requestFocus();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}

