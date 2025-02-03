import java.io.*;

public class GameState {
    private boolean gameOver;

    public void initialize() {
        gameOver = false;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * 解析玩家输入，并转换为 SMCDEL 查询（包含完整的模型）
     */
    public String translateToSMCDEL(String playerAction) {
        if (playerAction == null || playerAction.trim().isEmpty()) {
            return null;
        }

        // SMCDEL 变量和规则
        String baseSMCDEL = "VARS 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17\n" +
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

        // 生成 SMCDEL 查询
        return baseSMCDEL + "\nTRUE?\n  {}\n  " + playerAction.trim();
    }

    /**
     * 运行 SMCDEL 工具，并返回推理结果
     */
    public String callSMCDEL(String smcdelQuery) {
        if (smcdelQuery == null || smcdelQuery.trim().isEmpty()) {
            return "当前逻辑公式无法被验证，请重新输入。";
        }

        try {
            // 将查询写入文件
            File queryFile = new File("GameQuery.smcdel.txt");
            FileWriter writer = new FileWriter(queryFile);
            writer.write(smcdelQuery);
            writer.close();

            // 调用 SMCDEL 运行工具
            ProcessBuilder pb = new ProcessBuilder("./smcdel", "GameQuery.smcdel.txt");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 读取输出
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

    /**
     * 更新游戏状态
     */
    public void updateState(String smcdelOutput) {
        if (smcdelOutput.contains("True")) {
            smcdelOutput = smcdelOutput.replace("True", "该命题为真！你发现了一条重要线索。");
        } else if (smcdelOutput.contains("False")) {
            smcdelOutput = smcdelOutput.replace("False", "该命题为假！需要更多证据支持。");
        }

        // 判断是否进入游戏结束状态
        if (smcdelOutput.contains("Stone 被揭露")) {
            System.out.println("你成功揭露了 Stone 的罪行！正义结局达成。");
            gameOver = true;
        } else if (smcdelOutput.contains("Stone 逃脱")) {
            System.out.println("Stone 逃脱了审判，案件仍未解决。");
            gameOver = true;
        }
    }
}
