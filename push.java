import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class Calculator extends JFrame {
    private JTextField display;
    private double num1 = 0, num2 = 0, result = 0;
    private char operator;

    // Modern Color Palette
    private static final Color BG_COLOR = new Color(18, 18, 18);
    private static final Color NUMBER_BG = new Color(50, 50, 50);
    private static final Color OPERATOR_BG = new Color(255, 149, 0); // Orange
    private static final Color ACTION_BG = new Color(165, 165, 165); // Light Grey
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color DARK_TEXT = new Color(28, 28, 30);

    public Calculator() {
        setTitle("Modern Calculator");
        setSize(350, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG_COLOR);

        // UI Components
        setLayout(new BorderLayout(10, 10));

        // Display
        display = new JTextField();
        display.setEditable(false);
        display.setFont(new Font("Segoe UI", Font.BOLD, 36));
        display.setBackground(BG_COLOR);
        display.setForeground(TEXT_COLOR);
        display.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        display.setHorizontalAlignment(JTextField.RIGHT);
        add(display, BorderLayout.NORTH);

        // Buttons Panel
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 4, 10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] buttons = {
            "C", "±", "%", "/",
            "7", "8", "9", "*",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "0", ".", "DEL", "="
        };

        for (String text : buttons) {
            panel.add(createButton(text));
        }

        add(panel, BorderLayout.CENTER);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(getBackground().darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(getBackground().brighter());
                } else {
                    g2.setColor(getBackground());
                }

                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                
                if (getBackground().equals(ACTION_BG)) {
                    g2.setColor(DARK_TEXT);
                } else {
                    g2.setColor(TEXT_COLOR);
                }
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };

        button.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);

        // Set colors based on function
        if (text.matches("[0-9.]")) {
            button.setBackground(NUMBER_BG);
        } else if (text.matches("[/*\\-+=]")) {
            button.setBackground(OPERATOR_BG);
        } else {
            button.setBackground(ACTION_BG);
        }

        button.addActionListener(new ButtonClickListener());
        return button;
    }

    private class ButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();

            if (command.matches("[0-9.]")) {
                display.setText(display.getText() + command);
            } else if (command.equals("C")) {
                display.setText("");
                num1 = num2 = result = 0;
            } else if (command.equals("DEL")) {
                String currentText = display.getText();
                if (currentText.length() > 0) {
                    display.setText(currentText.substring(0, currentText.length() - 1));
                }
            } else if (command.equals("=")) {
                num2 = Double.parseDouble(display.getText());
                calculate();
                display.setText(String.valueOf(result));
            } else if (command.equals("±")) {
                if (!display.getText().isEmpty()) {
                    double val = Double.parseDouble(display.getText());
                    display.setText(String.valueOf(val * -1));
                }
            } else {
                if (!display.getText().isEmpty()) {
                    num1 = Double.parseDouble(display.getText());
                    operator = command.charAt(0);
                    display.setText("");
                }
            }
        }
    }

    private void calculate() {
        switch (operator) {
            case '+': result = num1 + num2; break;
            case '-': result = num1 - num2; break;
            case '*': result = num1 * num2; break;
            case '/': 
                if (num2 != 0) result = num1 / num2; 
                else display.setText("Error");
                break;
            case '%': result = num1 % num2; break;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            new Calculator().setVisible(true);
        });
    }
}
