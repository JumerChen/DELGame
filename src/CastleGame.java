import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CastleGame {
    private GameState gameState;
    private JTextArea outputArea;
    private JTextArea inputArea;
    private JButton submitButton;
    private JComboBox<String> assertionTypeBox;
    private String language = "zh";

    public CastleGame() {
        selectLanguage();

        gameState = new GameState(language);
        gameState.initialize();

        JFrame frame = new JFrame(language.equals("zh") ? "Castle Game - 暴风雨之夜" : "Castle Game - Stormy Night");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
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
        inputArea = new JTextArea(5, 30);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        JScrollPane inputScrollPane = new JScrollPane(inputArea);
        submitButton = new JButton(language.equals("zh") ? "提交" : "Submit");

        inputPanel.add(assertionTypeBox, BorderLayout.NORTH);
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.SOUTH);
        frame.add(inputPanel, BorderLayout.SOUTH);

        submitButton.addActionListener(e -> processInput());

        frame.setVisible(true);
        showGameIntroduction();
    }

    private void selectLanguage() {
        String[] options = {"中文", "English"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "请选择语言 / Select Language",
                "Language",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (choice == 1) {
            language = "en";
        }
    }

    private void showGameIntroduction() {
        String introduction = language.equals("zh") ?
                "【暴风雨之夜的真相】\n\n" +
                        "阿尔曼德古堡，一座笼罩在风暴阴影之下的庄园。\n" +
                        "副管家，这座古堡中掌管财务记录与遗产信息的关键人物，在一个电闪雷鸣的夜晚被发现死于书房。\n" +
                        "房门从内反锁，死因疑似窒息或中毒——一个典型的密室杀人案件。\n" +
                        "六位嫌疑人各怀秘密，而真相隐藏在他们的言行和证据之中。\n\n" +
                        "【游戏规则】\n" +
                        "- `TRUE? {}` 查询事实\n" +
                        "- `VALID?` 公开信息\n" +
                        "- `WHERE?` 查询符合条件的情况\n" +
                        "- 使用公告 `!` 让角色获知新信息\n\n" +
                        "公告示例\n" +
                        "`VALID? [! claire knows whether 4] claire knows that 4`\n" +
                        "`VALID? [! claire knows whether 4] [! stone knows whether 7]`\n\n" +
                        "输入你的第一个推理公式，开始调查吧！"
                :
                "Truth of the Stormy Night\n\n" +
                        "Armand Castle, a mansion shrouded in the shadow of the storm.\n" +
                        "The deputy butler, the key person in charge of financial records and inheritance information, was found dead in the study on a stormy night.\n" +
                        "The room was locked from the inside, and the cause of death was suspected to be suffocation or poisoning - a typical locked-room murder.\n\n" +
                        "Game Rules\n" +
                        "- `TRUE? {}` to check facts\n" +
                        "- `VALID?` to announce information\n" +
                        "- `WHERE?` to query conditions\n" +
                        "- Use announcement `!` to make characters aware of new information\n\n" +
                        "Example Announcements\n" +
                        "`VALID? [! claire knows whether 4] claire knows that 4`\n" +
                        "`VALID? [! claire knows whether 4] [! stone knows whether 7]`\n\n" +
                        "Enter your first reasoning formula to start the investigation!";
        outputArea.append(introduction + "\n");
    }

    private void processInput() {
        String playerInput = inputArea.getText().trim();
        if (!playerInput.isEmpty()) {
            String[] lines = playerInput.split("\\n");
            if (lines.length > 10) {
                outputArea.append(language.equals("zh") ? "输入行数超过10行，请重新输入！\n" : "Too many lines of input! Please try again.\n");
                return;
            }

            outputArea.append((language.equals("zh") ? "你输入:\n" : "You entered:\n") + playerInput + "\n");

            String smcdelQuery = gameState.translateToSMCDEL(lines, (String) assertionTypeBox.getSelectedItem());
            String smcdelOutput = gameState.callSMCDEL(smcdelQuery);
            String parsedOutput = gameState.updateState(smcdelOutput);

            outputArea.append((language.equals("zh") ? "系统回复:\n" : "System Response:\n") + parsedOutput + "\n");

            inputArea.setText("");
            inputArea.requestFocus();

            if (gameState.isGameOver()) {
                if (!parsedOutput.contains(language.equals("zh") ? "游戏结束！感谢参与。" : "Game over! Thanks for playing.")) {
                    outputArea.append(language.equals("zh") ? "游戏结束！感谢参与。\n" : "Game over! Thanks for playing.\n");
                }
                inputArea.setEnabled(false);
                submitButton.setEnabled(false);
                assertionTypeBox.setEnabled(false);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CastleGame::new);
    }
}
