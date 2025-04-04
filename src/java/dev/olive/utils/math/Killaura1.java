package dev.olive.utils.math;




import dev.olive.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Killaura1 {

    public static String getHWID() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        StringBuilder lilililiiliilililil1ililili1ilil1lililili1li1lilil1i1li1li1l1i1li1l1il1il1i1l = new StringBuilder();
        String lililililiililililililili1ilil1lilili1li1li1lilil1i1li1li1l1i1li1l1il1il1i1l = System.getenv("PROCESS_IDENTIFIER") + System.getenv("COMPUTERNAME");
        byte[] lililililiilililil1ililili1ilil1lilili1li1li1lilil1i1li1li1l1i1li1l1il1il1i1l = lililililiililililililili1ilil1lilili1li1li1lilil1i1li1li1l1i1li1l1il1il1i1l.getBytes("UTF-8");
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] lililililiilililililililililil1lilili1li1li1lilil1i1li1li1l1i1li1l1il1il1i1l = messageDigest.digest(lililililiilililil1ililili1ilil1lilili1li1li1lilil1i1li1li1l1i1li1l1il1il1i1l);
        int i = 0;
        for(byte b : lililililiilililililililililil1lilili1li1li1lilil1i1li1li1l1i1li1l1il1il1i1l) {
            lilililiiliilililil1ililili1ilil1lililili1li1lilil1i1li1li1l1i1li1l1il1il1i1l.append(Integer.toHexString((b & 0xFF) | 0x300),0,3);
            if(i != lililililiilililililililililil1lilili1li1li1lilil1i1li1li1l1i1li1l1il1il1i1l.length -1) {
                lilililiiliilililil1ililili1ilil1lililili1li1lilil1i1li1li1l1i1li1l1il1il1i1l.append("-");
            }
            i++;
        }
        return lilililiiliilililil1ililili1ilil1lililili1li1lilil1i1li1li1l1i1li1l1il1il1i1l.toString();
    }
    public static String check(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
            response.append("\n");
        }

        in.close();

        return response.toString();
    }
    public static void windowsnoti(String Title, String Text, TrayIcon.MessageType type) {// :)逆天名字
        try {
            SystemTray systemTray = SystemTray.getSystemTray();
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Image image = toolkit.createImage("icon.png");
            TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("System tray icon demo");
            systemTray.add(trayIcon);
            trayIcon.displayMessage(Title, Text, type);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
    public static boolean verify2() {
        try {
            if (check(Client.Verify_http).contains("破解")){
                return true;
            } else {
                JOptionPane.showMessageDialog(null,"你破解你妈呢?傻逼");
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return false;
    }
    public static void main(String[] args) {
        showMessageDialog();
    }

    public static void showMessageDialog() {

        JButton button2 = new JButton("进行HWID验证");
        button2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String hwid = getHWID();
                    if (check(Client.Verify_http).contains("破解")){

                        JOptionPane.showMessageDialog(null,"操你妈破解你妈");
                        System.exit(0);
                    }
                } catch (NoSuchAlgorithmException ex) {
                    throw new RuntimeException(ex);
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException(ex);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

    }
}

