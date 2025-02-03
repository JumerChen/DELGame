import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CastleGame {
    private GameState gameState;
    private JTextArea outputArea;
    private JTextField inputField;
    private JButton submitButton;
    private JComboBox<String> assertionTypeBox;

    public CastleGame() {
        gameState = new GameState();
        gameState.initialize();

        JFrame frame = new JFrame("Castle Game - 暴风雨之夜");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        assertionTypeBox = new JComboBox<>(new String[]{"TRUE?", "VALID?", "WHERE?"});
        inputField = new JTextField();
        submitButton = new JButton("提交");

        inputPanel.add(assertionTypeBox, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.SOUTH);

        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processInput();
            }
        });

        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processInput();
            }
        });

        frame.setVisible(true);
        outputArea.append("欢迎来到古堡谋杀之夜！\n");
    }

    private void processInput() {
        String playerInput = inputField.getText().trim();
        String assertionType = (String) assertionTypeBox.getSelectedItem();
        if (!playerInput.isEmpty()) {
            outputArea.append("你输入: " + playerInput + "\n");

            String smcdelQuery = gameState.translateToSMCDEL(playerInput, assertionType);
            String smcdelOutput = gameState.callSMCDEL(smcdelQuery);
            gameState.updateState(smcdelOutput);

            outputArea.append("系统回复: " + smcdelOutput + "\n");

            inputField.setText("");
            inputField.requestFocus();

            if (gameState.isGameOver()) {
                outputArea.append("游戏结束！感谢参与。\n");
                inputField.setEnabled(false);
                submitButton.setEnabled(false);
                assertionTypeBox.setEnabled(false);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CastleGame());
    }
}
