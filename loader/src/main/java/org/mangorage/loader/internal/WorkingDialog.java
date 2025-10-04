package org.mangorage.loader.internal;

import javax.swing.*;

public final class WorkingDialog {
    private JDialog dialog;
    private JLabel label;

    public void init(String initialText) {
        if (dialog != null && dialog.isVisible()) return;

        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setType(JFrame.Type.UTILITY);

        dialog = new JDialog(frame, "Working...", false); // not modal
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setAlwaysOnTop(true);

        label = new JLabel(initialText, SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        dialog.getContentPane().add(label);

        dialog.pack();
        dialog.setLocationRelativeTo(null);

        // Launch this stupid thing in a thread so it doesn’t freeze your program
        new Thread(() -> dialog.setVisible(true)).start();
    }

    public void setText(String newText) {
        if (label != null) {
            SwingUtilities.invokeLater(() -> label.setText(newText));
            System.out.println(newText);
        }
    }

    public void close() {
        if (dialog != null) {
            SwingUtilities.invokeLater(() -> {
                try {
                    Thread.sleep(8000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                dialog.setVisible(false);
                dialog.dispose();
            });
        }
    }
}


