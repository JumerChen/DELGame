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

    public CastleGame() {
        gameState = new GameState();
        gameState.initialize();

        JFrame frame = new JFrame("Castle Game - 暴风雨之夜");
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
        inputArea = new JTextArea(5, 30);  // 支持多行输入
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        JScrollPane inputScrollPane = new JScrollPane(inputArea);
        submitButton = new JButton("提交");

        inputPanel.add(assertionTypeBox, BorderLayout.NORTH);
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.SOUTH);
        frame.add(inputPanel, BorderLayout.SOUTH);

        submitButton.addActionListener(e -> processInput());

        frame.setVisible(true);
        showGameIntroduction();
    }

    private void showGameIntroduction() {
        String introduction = "【暴风雨之夜的真相】\n\n" +
                "阿尔曼德古堡，一座笼罩在风暴阴影之下的庄园。\n" +
                "副管家，这座古堡中掌管财务记录与遗产信息的关键人物，在一个电闪雷鸣的夜晚被发现死于书房。\n" +
                "房门从内反锁，死因疑似窒息或中毒——一个典型的密室杀人案件。\n" +
                "六位嫌疑人各怀秘密，而真相隐藏在他们的言行和证据之中。\n\n" +
                "你能运用逻辑推理，揭开这场谋杀背后的真相吗？\n\n" +
                "【游戏规则】\n\n" +
                "你将通过输入逻辑断言来调查案件，揭示关键线索，并最终锁定凶手。\n" +
                "我们使用 SMCDEL（动态认知逻辑）进行推理，你可以：\n\n" +
                "- 查询事实是否成立：\n" +
                "  TRUE? {} 10  // 是否发现毒药痕迹？\n\n" +
                "- 查询某人是否知道某个事实：\n" +
                "  TRUE? {} claire knows that 16  // Claire 是否知道自己被嫁祸？\n\n" +
                "- 查询符合特定条件的情况：\n" +
                "  WHERE? (10 & 5 & ~3)  // 毒药存在，Victor 目击可疑身影，房门未锁。\n\n" +
                "【游戏变量参考】\n" +
                "在游戏中，一共有 17 个不同的信息点，你可以使用它们进行推理：\n\n" +
                "  1  - Elsa 是否知道 Bolton 的账册异常\n" +
                "  2  - Bolton 是否知道 Elsa 手中有旧遗嘱\n" +
                "  3  - 房门是否被锁\n" +
                "  4  - Claire 是否持有副管家的亲笔信\n" +
                "  5  - Victor 是否在停电时看见某可疑身影\n" +
                "  6  - Hamilton 是否确定副管家的死亡时间\n" +
                "  7  - Stone 是否拥有有效的旧主遗嘱副本\n" +
                "  8  - Elsa 的财务危机是否暴露\n" +
                "  9  - Victor 是否协助 Stone\n" +
                " 10  - 是否发现毒药痕迹\n" +
                " 11  - 副管家是否公开提到 Stone 的犯罪行为\n" +
                " 12  - Claire 是否知道 Stone 的财务欺诈\n" +
                " 13  - Stone 是否伪造密室证据\n" +
                " 14  - Bolton 是否持有暗道钥匙\n" +
                " 15  - 是否发现副管家身上的旧收据\n" +
                " 16  - Stone 是否试图嫁祸 Claire\n" +
                " 17  - 房门是否被外部反锁\n\n" +
                "使用这些变量进行查询，揭开真相！\n\n" +
                "输入你的第一个推理公式，开始调查吧！";
        outputArea.append(introduction + "\n");
    }

    private void processInput() {
        String playerInput = inputArea.getText().trim();
        if (!playerInput.isEmpty()) {
            String[] lines = playerInput.split("\\n");
            if (lines.length > 10) {
                outputArea.append("输入行数超过10行，请重新输入！\n");
                return;
            }

            outputArea.append("你输入:\n" + playerInput + "\n");

            String smcdelQuery = gameState.translateToSMCDEL(lines, (String) assertionTypeBox.getSelectedItem());
            String smcdelOutput = gameState.callSMCDEL(smcdelQuery);
            String parsedOutput = gameState.updateState(smcdelOutput);

            outputArea.append("系统回复:\n" + parsedOutput + "\n");

            inputArea.setText("");
            inputArea.requestFocus();

            if (gameState.isGameOver()) {
                outputArea.append("游戏结束！感谢参与。\n");
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
