import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class GameState {
    private boolean gameOver;

    public void initialize() {
        gameOver = false;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public String translateToSMCDEL(String playerAction, String assertionType) {
        if (playerAction == null || playerAction.trim().isEmpty()) {
            return null;
        }

        String assertionKeyword;
        switch (assertionType) {
            case "TRUE?":
                assertionKeyword = "TRUE?";
                break;
            case "VALID?":
                assertionKeyword = "VALID?";
                break;
            case "WHERE?":
                assertionKeyword = "WHERE?";
                break;
            default:
                assertionKeyword = "TRUE?";
        }

        String baseSMCDEL =
                "VARS 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17\n" +
                        "LAW\n" +
                        "  ( (1 -> ~2) & (4 -> (13 & 16)) & (7 -> (6 -> 3)) & (8 -> (1 & ~7)) & (9 -> ~3) &\n" +
                        "    (10 -> (5 & ~6)) & (11 -> ~8) & (12 -> 11) & (13 -> 3) & (14 -> 9) & (15 -> ~4) )\n" +
                        "OBS\n" +
                        "  elsa: 1,8,13\n" +
                        "  bolton: 2,9,14\n" +
                        "  claire: 4,12,16\n" +
                        "  victor: 5,9,10\n" +
                        "  hamilton: 6,11\n" +
                        "  stone: 3,7,15\n";

        return baseSMCDEL + "\n" + assertionKeyword + "\n  {}\n  " + playerAction.trim();
    }

    public String callSMCDEL(String smcdelQuery) {
        if (smcdelQuery == null || smcdelQuery.trim().isEmpty()) {
            return "当前逻辑公式无法被验证，请重新输入。";
        }

        try {
            // 将 SMCDEL 查询写入文件
            File queryFile = new File("GameQuery.smcdel.txt");
            FileWriter writer = new FileWriter(queryFile);
            writer.write(smcdelQuery);
            writer.close();

            // 运行 SMCDEL 工具（假设 `smcdel` 是可执行文件）
            ProcessBuilder pb = new ProcessBuilder("./smcdel", "GameQuery.smcdel.txt");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 读取 SMCDEL 输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            boolean validResponse = false;

            while ((line = reader.readLine()) != null) {
                if (!line.contains("SMCDEL") && !line.contains("Doei!") && !line.trim().isEmpty()) {
                    line = line.replaceAll("\\u001B\\[[;\\d]*m", ""); // 过滤 ANSI 颜色代码
                    output.append(line).append("\n");
                    validResponse = true;
                }
            }
            process.waitFor();

            if (!validResponse) {
                return "当前逻辑公式无法被验证，请重新输入。";
            }

            String smcdelOutput = output.toString().trim();

            if (smcdelOutput.contains("Parse error")) {
                return "逻辑错误，请检查格式！";
            }
            return smcdelOutput;
        } catch (Exception e) {
            e.printStackTrace();
            return "发生错误：SMCDEL 运行失败，请检查你的输入。";
        }
    }

    public String updateState(String smcdelOutput) {
        Map<String, String> variableDescriptions = new HashMap<>();
        variableDescriptions.put("1", "Elsa 是否知道 Bolton 的账册异常");
        variableDescriptions.put("2", "Bolton 是否知道 Elsa 手中有旧遗嘱");
        variableDescriptions.put("3", "房门是否被锁");
        variableDescriptions.put("4", "Claire 是否持有副管家的亲笔信");
        variableDescriptions.put("5", "Victor 是否在停电时看见某可疑身影");
        variableDescriptions.put("6", "Hamilton 是否确定副管家的死亡时间");
        variableDescriptions.put("7", "Stone 是否拥有有效的旧主遗嘱副本");
        variableDescriptions.put("8", "Elsa 的财务危机是否暴露");
        variableDescriptions.put("9", "Victor 是否协助 Stone");
        variableDescriptions.put("10", "是否发现毒药痕迹");
        variableDescriptions.put("11", "副管家是否公开提到 Stone 的犯罪行为");
        variableDescriptions.put("12", "Claire 是否知道 Stone 的财务欺诈");
        variableDescriptions.put("13", "Stone 是否伪造密室证据");
        variableDescriptions.put("14", "Bolton 是否持有暗道钥匙");
        variableDescriptions.put("15", "是否发现副管家身上的旧收据");
        variableDescriptions.put("16", "Stone 是否试图嫁祸 Claire");
        variableDescriptions.put("17", "房门是否被外部反锁");

        String parsedOutput = smcdelOutput;  // 默认使用原始输出

        if (smcdelOutput.contains("Is K ")) {
            String[] parts = smcdelOutput.split("\\?");
            if (parts.length > 1) {
                String fact = parts[0].replace("Is K ", "").trim();
                String[] factParts = fact.split(" ");
                if (factParts.length >= 2) {
                    String agent = factParts[0]; // 角色
                    String variable = factParts[1]; // 变量编号
                    String description = variableDescriptions.getOrDefault(variable, "未知变量");

                    if (parts[1].trim().equals("True")) {
                        parsedOutput = agent + " **确信** " + description + " 为真！";
                    } else {
                        parsedOutput = agent + " **无法确定** " + description + " 为真。";
                    }
                }
            }
        } else if (smcdelOutput.contains("Is Kw ")) {
            String[] parts = smcdelOutput.split("\\?");
            if (parts.length > 1) {
                String fact = parts[0].replace("Is Kw ", "").trim();
                String[] factParts = fact.split(" ");
                if (factParts.length >= 2) {
                    String agent = factParts[0]; // 角色
                    String variable = factParts[1]; // 变量编号
                    String description = variableDescriptions.getOrDefault(variable, "未知变量");

                    if (parts[1].trim().equals("True")) {
                        parsedOutput = agent + " **能够判断** " + description + " 的真假。";
                    } else {
                        parsedOutput = agent + " **无法判断** " + description + " 的真假。";
                    }
                }
            }
        }

        if (smcdelOutput.contains("Stone 被揭露")) {
            parsedOutput = "你成功揭露了 Stone 的罪行！正义结局达成。";
            gameOver = true;
        } else if (smcdelOutput.contains("Stone 逃脱")) {
            parsedOutput = "Stone 逃脱了审判，案件仍未解决。";
            gameOver = true;
        }

        return parsedOutput;  // 返回解析后的文本，让UI使用它
    }
}
