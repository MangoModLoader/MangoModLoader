package org.mangorage.mml.installer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;


public final class InstallerGui {
    public static void createAndShowGUI() {
        JFrame frame = new JFrame("Installer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(420, 200);
        frame.setLocationRelativeTo(null);

        // Main layout with consistent alignment
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Folder label
        JLabel label = new JLabel("Select Folder:");
        label.setHorizontalAlignment(SwingConstants.LEFT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(label, gbc);

        // Folder text field
        JTextField folderField = new JTextField();
        gbc.gridy = 1;
        mainPanel.add(folderField, gbc);

        // Browse button
        JButton browseButton = new JButton("Browse...");
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(browseButton, gbc);

        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFolder = chooser.getSelectedFile();
                folderField.setText(selectedFolder.getAbsolutePath());
            }
        });

        // Install buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton installClientButton = new JButton("Install Client");
        JButton installServerButton = new JButton("Install Server");

        // Shared disable logic
        Runnable disableButtons = () -> {
            installClientButton.setEnabled(false);
            installServerButton.setEnabled(false);
            browseButton.setEnabled(false);
        };

        installClientButton.addActionListener(e -> {
            String path = folderField.getText().trim();
            if (path.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please select a folder first!");
                return;
            }

            // Disable buttons immediately
            disableButtons.run();

            // Create a small "Installing" dialog
            JDialog progressDialog = new JDialog(frame, "Installing Client...", true);
            JLabel progressLabel = new JLabel("Installing Client, please wait...");
            progressLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            progressDialog.add(progressLabel);
            progressDialog.pack();
            progressDialog.setLocationRelativeTo(frame);

            // Run installation in a new thread
            new Thread(() -> {
                try {
                    Installer.installClient(path); // Long-running task

                    // Close the dialog and show success message on EDT
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        JOptionPane.showMessageDialog(frame, "Client installed successfully!");
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Client installation failed: " + ex.getMessage());
                    });
                }
            }).start();

            // Show the dialog on EDT (non-blocking because the task is on another thread)
            progressDialog.setVisible(true);
        });

        buttonPanel.add(installClientButton);
        buttonPanel.add(installServerButton);

        gbc.gridy = 3;
        mainPanel.add(buttonPanel, gbc);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

}
