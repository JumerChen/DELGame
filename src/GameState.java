import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class GameState {
    private boolean gameOver;
    private int attemptCount = 0;
    private final int MAX_ATTEMPTS = 5;

    public void initialize() {
        gameOver = false;
        attemptCount = 0;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public String translateToSMCDEL(String[] playerActions, String assertionType) {
        if (playerActions == null || playerActions.length == 0) {
            return null;
        }

        String assertionKeyword;
        switch (assertionType.trim()) {
            case "TRUE?": assertionKeyword = "TRUE?"; break;
            case "VALID?": assertionKeyword = "VALID?"; break;
            case "WHERE?": assertionKeyword = "WHERE?"; break;
            default: assertionKeyword = "TRUE?";
        }

        StringBuilder baseSMCDEL = new StringBuilder(
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
                        "  stone: 3,7,15\n"
        );

        baseSMCDEL.append("\n").append(assertionKeyword).append("\n");

        if (assertionKeyword.equals("VALID?")) {
            String combinedAction = String.join(" ", playerActions).replace("\n", " ").trim();
            baseSMCDEL.append("  ").append(combinedAction).append("\n");
        } else {
            baseSMCDEL.append("  {}\n");
            for (String action : playerActions) {
                baseSMCDEL.append("  ").append(action.trim()).append("\n");
            }
        }

        return baseSMCDEL.toString();
    }

    public String callSMCDEL(String smcdelQuery) {
        if (smcdelQuery == null || smcdelQuery.trim().isEmpty()) {
            return "当前逻辑公式无法被验证，请重新输入。";
        }

        try {
            File queryFile = new File("GameQuery.smcdel.txt");
            FileWriter writer = new FileWriter(queryFile);
            writer.write(smcdelQuery);
            writer.close();

            ProcessBuilder pb = new ProcessBuilder("./smcdel", "GameQuery.smcdel.txt");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            boolean validResponse = false;

            while ((line = reader.readLine()) != null) {
                if (!line.contains("SMCDEL") && !line.contains("Doei!") && !line.trim().isEmpty()) {
                    line = line.replaceAll("\\u001B\\[[;\\d]*m", "");
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
        String outputNormalized = smcdelOutput.replaceAll("\\s+", "");

        boolean crimeExposed =
                outputNormalized.contains("IsKstone13trueat[]?True") ||
                        outputNormalized.contains("IsKstone16trueat[]?True") ||
                        outputNormalized.contains("IsKstone7trueat[]?True");

        String feedback;

        if (smcdelOutput.contains("Parse error")) {
            feedback = "逻辑错误，请检查格式！你还有 " + (MAX_ATTEMPTS - attemptCount) + " 次机会。";
        } else if (smcdelOutput.contains("False")) {
            feedback = "推理失败，出现矛盾。你还有 " + (MAX_ATTEMPTS - attemptCount) + " 次机会。";
        } else if (smcdelOutput.contains("True")) {
            attemptCount++;

            if (crimeExposed) {
                gameOver = true;
                feedback = "推理成功！你成功揭露了 Stone 的罪行！正义结局达成。\n游戏结束！感谢参与。";
            } else if (attemptCount >= MAX_ATTEMPTS) {
                gameOver = true;
                feedback = "推理成功，但尚未发现关键证据。\n你已用尽所有调查机会。\nStone 逃脱了审判，案件仍未解决。\n游戏结束！感谢参与。";
            } else {
                feedback = "推理成功，但尚未发现关键证据。你还有 " + (MAX_ATTEMPTS - attemptCount) + " 次机会，请继续调查。";
            }
        } else {
            feedback = "输入格式错误，请检查你的推理公式！你还有 " + (MAX_ATTEMPTS - attemptCount) + " 次机会。";
        }

        return feedback;
    }

    private boolean outputContains(String output, String query, String result) {
        String normalizedQuery = query.replaceAll("\\s+", "") + result;
        return output.contains(normalizedQuery);
    }
}
