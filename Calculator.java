import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Calculator extends JFrame implements ActionListener {

    // Display field (screen)
    private JTextField display;

    // Stores user input and full expression
    private String currentInput = "";
    private String expression = "";

    // ===== CONSTRUCTOR =====
    public Calculator() {

        // Window settings
        setTitle("CS Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setResizable(false);

        // ===== DISPLAY =====
        display = new JTextField();
        display.setEditable(false); // user cannot type manually
        display.setHorizontalAlignment(JTextField.RIGHT); // align text right
        display.setFont(new Font("Arial", Font.PLAIN, 18));
        display.setPreferredSize(new Dimension(360, 35));
        display.setBackground(new Color(240, 240, 240));

        // Panel for display
        JPanel displayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        displayPanel.add(display);
        add(displayPanel, BorderLayout.NORTH);

        // ===== MAIN PANEL (LEFT + RIGHT) =====
        JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        // ===== LEFT PANEL: NUMBERS =====
        JPanel leftPanel = new JPanel(new GridLayout(4, 3, 5, 5));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String[] numberButtons = {
                "7", "8", "9",
                "4", "5", "6",
                "1", "2", "3",
                "",  "0", ""   // empty spaces for layout
        };

        for (String label : numberButtons) {
            if (label.isEmpty()) {
                leftPanel.add(new JLabel()); // empty cell
            } else {
                leftPanel.add(createButton(label));
            }
        }

        // ===== RIGHT PANEL: OPERATORS =====
        JPanel rightPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String[] opButtons = {
                "C",  "CE",
                "(",  ")",
                "*",  "/",
                "+",  "-",
                "",   "="
        };

        for (String label : opButtons) {
            if (label.isEmpty()) {
                rightPanel.add(new JLabel()); // empty space
            } else {
                rightPanel.add(createButton(label));
            }
        }

        // Add both panels to main panel
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);
        add(mainPanel, BorderLayout.CENTER);

        // Adjust window size automatically
        pack();
        setLocationRelativeTo(null); // center window
        setVisible(true);
    }

    // ===== BUTTON CREATOR METHOD =====
    private JButton createButton(String label) {
        JButton btn = new JButton(label);

        // Styling
        btn.setFont(new Font("Arial", Font.PLAIN, 16));
        btn.setPreferredSize(new Dimension(60, 45));
        btn.setBackground(new Color(220, 225, 235));
        btn.setFocusPainted(false);

        // Border styling
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 185, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Add click event
        btn.addActionListener(this);

        return btn;
    }

    // ===== EVENT HANDLING =====
    @Override
    public void actionPerformed(ActionEvent e) {

        String cmd = e.getActionCommand(); // button text

        switch (cmd) {

            case "C":
                // Clear everything
                currentInput = "";
                expression = "";
                display.setText("");
                break;

            case "CE":
                // Remove last character
                if (!currentInput.isEmpty()) {
                    currentInput = currentInput.substring(0, currentInput.length() - 1);
                    display.setText(currentInput);
                }
                break;

            case "=":
                // Calculate result
                try {
                    expression = currentInput;
                    double result = evaluate(expression);

                    // Show integer if possible
                    if (result == Math.floor(result) && !Double.isInfinite(result)) {
                        display.setText(String.valueOf((long) result));
                        currentInput = String.valueOf((long) result);
                    } else {
                        display.setText(String.valueOf(result));
                        currentInput = String.valueOf(result);
                    }

                } catch (Exception ex) {
                    display.setText("Error");
                    currentInput = "";
                }
                break;

            default:
                // Add number/operator to input
                currentInput += cmd;
                display.setText(currentInput);
                break;
        }
    }

    // ===== EVALUATE EXPRESSION =====
    private double evaluate(String expr) {
        expr = expr.trim();
        return new ExprParser(expr).parse();
    }

    // ===== EXPRESSION PARSER CLASS =====
    static class ExprParser {
        private final String input;
        private int pos;

        ExprParser(String input) {
            this.input = input;
            this.pos = 0;
        }

        // Start parsing
        double parse() {
            double result = parseAddSub();
            if (pos < input.length())
                throw new RuntimeException("Unexpected: " + input.charAt(pos));
            return result;
        }

        // Handle + and -
        private double parseAddSub() {
            double left = parseMulDiv();
            while (pos < input.length() &&
                    (input.charAt(pos) == '+' || input.charAt(pos) == '-')) {

                char op = input.charAt(pos++);
                double right = parseMulDiv();

                left = (op == '+') ? left + right : left - right;
            }
            return left;
        }

        // Handle * and /
        private double parseMulDiv() {
            double left = parseUnary();

            while (pos < input.length() &&
                    (input.charAt(pos) == '*' || input.charAt(pos) == '/')) {

                char op = input.charAt(pos++);
                double right = parseUnary();

                if (op == '/') {
                    if (right == 0) throw new ArithmeticException("Division by zero");
                    left = left / right;
                } else {
                    left = left * right;
                }
            }
            return left;
        }

        // Handle negative numbers
        private double parseUnary() {
            if (pos < input.length() && input.charAt(pos) == '-') {
                pos++;
                return -parsePrimary();
            }
            if (pos < input.length() && input.charAt(pos) == '+') {
                pos++;
            }
            return parsePrimary();
        }

        // Handle parentheses
        private double parsePrimary() {
            if (pos < input.length() && input.charAt(pos) == '(') {
                pos++; // skip '('
                double val = parseAddSub();

                if (pos >= input.length() || input.charAt(pos) != ')')
                    throw new RuntimeException("Missing closing parenthesis");

                pos++; // skip ')'
                return val;
            }
            return parseNumber();
        }

        // Read number (integer or decimal)
        private double parseNumber() {
            int start = pos;

            while (pos < input.length() &&
                    (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
                pos++;
            }

            if (start == pos)
                throw new RuntimeException("Expected number at position " + pos);

            return Double.parseDouble(input.substring(start, pos));
        }
    }

    // ===== MAIN METHOD =====
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Calculator::new);
    }
}