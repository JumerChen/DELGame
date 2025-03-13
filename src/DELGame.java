import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DELGame {
    private GameState gameState;
    private JTextArea outputArea;
    private JTextArea inputArea;
    private JButton submitButton;
    private JComboBox<String> assertionTypeBox;
    private String language = "zh";

    public DELGame() {
        selectLanguage();

        gameState = new GameState(language);
        gameState.initialize();

        JFrame frame = new JFrame(language.equals("zh") ? "古堡追凶 - 暴风雨之夜" : "Shadows Over the Castle - Stormy Night");
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
                "Language Preference",
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
                "【暴风雨夜的凶案】\n\n" +
                        "阿尔曼德古堡，一座今夜被笼罩在风暴阴影之下的庄园。\n" +
                        "副管家，这座古堡中掌管财务记录与古堡主人遗产信息的关键人物，被发现死于书房。\n" +
                        "案发现场房门从内反锁，死因疑似窒息或中毒——一个典型的密室杀人案件。\n" +
                        "六位嫌疑人各怀秘密，而真相隐藏在他们的言行和证据之中。\n" +
                        "游戏基于动态认知逻辑（DEL）推动，接下来将介绍其基本的语法及游戏规则。\n\n" +
                        "【断言类型】\n" +
                        "▶ `TRUE? {}` 确认事实\n" +
                        "▶ `VALID?` 公开信息\n" +
                        "▶ `WHERE?` 查询符合条件的情况\n" +
                        "▶ 使用公告 `!` 让角色获知新信息\n\n" +

                        "【初始变量】\n" +
                        "在游戏中，以下 17 个变量决定了案件的真相，你可以通过编写逻辑断言查询它们的真伪：\n\n" +

                        "  1- Elsa 是否知道 Bolton 的账册异常\n" +
                        "  2- Bolton 是否知道 Elsa 手中有旧遗嘱\n" +
                        "  3- 房门是否被锁\n" +
                        "  4- Claire 是否持有副管家的亲笔信\n" +
                        "  5- Victor 是否在停电时看见某可疑身影\n" +
                        "  6- Hamilton 是否确定副管家的死亡时间\n" +
                        "  7- Stone 是否拥有有效的旧主遗嘱副本\n" +
                        "  8- Elsa 的财务危机是否暴露\n" +
                        "  9- Victor 是否协助 Stone\n" +
                        "  10- 是否发现毒药痕迹\n" +
                        "  11- 副管家是否公开提到 Stone 的犯罪行为\n" +
                        "  12- Claire 是否知道 Stone 的财务欺诈\n" +
                        "  13- Stone 是否伪造密室证据\n" +
                        "  14- Bolton 是否持有暗道钥匙\n" +
                        "  15- 是否发现副管家身上的旧收据\n" +
                        "  16- Stone 是否试图嫁祸 Claire\n" +
                        "  17- 房门是否被外部反锁\n\n" +

                        "【公告知识示例】\n" +
                        "`VALID? [! claire knows whether 4] claire knows that 4`\n" +
                        "`TRUE? [! claire knows whether 4] stone knows whether 7`\n" +
                        "`WHERE? [?! claire knows whether (4 & 12)] claire knows that (4 & 12)`\n\n" +
                        "输入你的第一个推理公式，开始调查吧！"
                :
                "Murder on the Stormy Night\n\n" +
                        "Armand Castle, a mansion shrouded in the shadows of a raging storm.\n" +
                        "The deputy butler, the key figure managing financial records and the estate of the castle’s owner, has been found dead in the study.\n" +
                        "The crime scene is a classic locked-room case— the door was locked from the inside, and the cause of death is suspected to be suffocation or poisoning.\n\n" +
                        "Six suspects, each harboring secrets, hold the key to the truth hidden within their words and actions.\n" +
                        "The game progresses using Dynamic Epistemic Logic (DEL), and below is an introduction to its syntax and gameplay mechanics.\n\n" +
                        "Assertion Types\n" +
                        "▶ `TRUE? {}` to confirm facts\n" +
                        "▶ `VALID?` to publicly reveal information\n" +
                        "▶ `WHERE?` to query conditions\n" +
                        "▶ Use announcement `!` to make characters aware of new information\n\n" +


                        "Initial Variables\n" +
                        "The following 17 variables determine the truth of the case. You can query them:\n\n" +

                        "  1- Does Elsa know about Bolton’s account anomalies?\n" +
                        "  2- Does Bolton know Elsa has an old will?\n" +
                        "  3- Was the room locked?\n" +
                        "  4- Does Claire possess the deputy butler’s handwritten letter?\n" +
                        "  5- Did Victor see a suspicious figure during the blackout?\n" +
                        "  6- Can Hamilton confirm the deputy butler’s time of death?\n" +
                        "  7- Does Stone have a valid copy of the old master’s will?\n" +
                        "  8- Has Elsa’s financial crisis been exposed?\n" +
                        "  9- Did Victor assist Stone?\n" +
                        "  10- Was poison residue discovered?\n" +
                        "  11- Did the deputy butler publicly accuse Stone?\n" +
                        "  12- Does Claire know about Stone’s financial fraud?\n" +
                        "  13- Did Stone forge evidence for a locked room?\n" +
                        "  14- Does Bolton have the secret passage key?\n" +
                        "  15- Was an old receipt found on the deputy butler?\n" +
                        "  16- Did Stone try to frame Claire?\n" +
                        "  17- Was the room locked from the outside?\n\n" +

                        "Announcement Examples\n" +
                        "`VALID? [! claire knows whether 4] claire knows that 4`\n" +
                        "`TRUE? [! claire knows whether 4] stone knows whether 7`\n" +
                        "`WHERE? [?! claire knows whether (4 & 12)] claire knows that (4 & 12)`\n\n" +
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
        SwingUtilities.invokeLater(DELGame::new);
    }
}
