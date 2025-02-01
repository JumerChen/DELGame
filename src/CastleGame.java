import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CastleGame {
    private GameState gameState;
    private JTextArea outputArea;
    private JTextField inputField;
    private JButton submitButton;

    public CastleGame() {
        gameState = new GameState();
        gameState.initialize();

        // 创建 Swing 界面
        JFrame frame = new JFrame("Castle Game - 暴风雨之夜");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // 输出区域
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // 输入区域
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputField = new JTextField();
        submitButton = new JButton("提交");

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // 绑定提交按钮点击事件
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processInput();
            }
        });

        // 绑定 Enter 键事件
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
        if (!playerInput.isEmpty()) {
            outputArea.append("你输入: " + playerInput + "\n");

            // 调用 GameState 处理逻辑
            String smcdelQuery = gameState.translateToSMCDEL(playerInput);
            String smcdelOutput = gameState.callSMCDEL(smcdelQuery);
            gameState.updateState(smcdelOutput);

            outputArea.append("系统回复: " + smcdelOutput + "\n");

            // 清空输入框 & 重新获取焦点
            inputField.setText("");
            inputField.requestFocus();

            // 如果游戏结束，禁用输入
            if (gameState.isGameOver()) {
                outputArea.append("游戏结束！感谢参与。\n");
                inputField.setEnabled(false);
                submitButton.setEnabled(false);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CastleGame());
    }
}
