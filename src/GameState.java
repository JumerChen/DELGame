import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class GameState {
    private boolean gameOver;
    private int attemptCount = 0;
    private final int MAX_ATTEMPTS = 5;
    private String language = "zh";

    public GameState(String language) {
        this.language = language;
        gameOver = false;
        attemptCount = 0;
    }

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

        if (assertionKeyword.equals("TRUE?") || assertionKeyword.equals("WHERE?")) {
            baseSMCDEL.append("  {}\n");
        }

        for (String action : playerActions) {
            baseSMCDEL.append("  ").append(action.trim()).append("\n");
        }

        return baseSMCDEL.toString();
    }

    public String callSMCDEL(String smcdelQuery) {
        if (smcdelQuery == null || smcdelQuery.trim().isEmpty()) {
            return language.equals("zh") ? "当前逻辑公式无法被验证，请重新输入。" : "Invalid query! Please try again.";
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
                return language.equals("zh") ? "当前逻辑断言无法被验证，请重新输入。" : "Invalid assertions! Please try again.";
            }

            String smcdelOutput = output.toString().trim();
            if (smcdelOutput.contains("Parse error")) {
                return language.equals("zh") ? "逻辑错误，请检查格式！" : "Syntax error! Check the format.";
            }

            return smcdelOutput;
        } catch (Exception e) {
            e.printStackTrace();
            return language.equals("zh") ? "发生错误：SMCDEL 运行失败，请检查你的输入。" : "Error: SMCDEL execution failed. Check your input.";
        }
    }


    public String updateState(String smcdelOutput) {
        String outputNormalized = smcdelOutput.replaceAll("\\s+", "").toLowerCase();

        boolean crimeExposed =
                outputNormalized.contains("kstone13true") ||
                        outputNormalized.contains("kstone16true") ||
                        outputNormalized.contains("kstone(13&16)true");

        String feedback;

        if (smcdelOutput.contains("Parse error")) {
            feedback = language.equals("zh") ?
                    "断言语法错误，请检查格式！你还有 " + (MAX_ATTEMPTS - attemptCount) + " 次机会。" :
                    "Assertion parse error! Please check the format. You have " + (MAX_ATTEMPTS - attemptCount) + " attempts left.";
        } else if (smcdelOutput.contains("False")) {
            feedback = language.equals("zh") ?
                    "断言不正确，推理失败。你还有 " + (MAX_ATTEMPTS - attemptCount) + " 次机会。" :
                    "False, reasoning failed. You have " + (MAX_ATTEMPTS - attemptCount) + " attempts left.";
        } else if (smcdelOutput.contains("True")) {
            attemptCount++;

            if (crimeExposed) {
                gameOver = true;
                feedback = language.equals("zh") ?
                        "推理成功！你成功揭露了 Stone 的罪行！正义结局达成。" :
                        "Reasoning successful! You have exposed Stone’s crime! Justice has been served.";
            } else if (attemptCount >= MAX_ATTEMPTS) {
                gameOver = true;
                feedback = language.equals("zh") ?
                        "推理成功，但尚未发现关键证据。\n你已用尽所有调查机会。\nStone 逃脱了审判，案件仍未解决。" :
                        "True. reasoning successful, but no key evidence found.\nYou have used all attempts.\nStone has escaped justice.";
            } else {
                feedback = language.equals("zh") ?
                        "推理成功，但尚未发现关键证据。你还有 " + (MAX_ATTEMPTS - attemptCount) + " 次机会，请继续调查。" :
                        "True. reasoning successful, but no key evidence found. You have " + (MAX_ATTEMPTS - attemptCount) + " attempts left.";
            }
        } else {
            feedback = language.equals("zh") ?
                    "输入格式错误，请检查你的推理公式！你还有 " + (MAX_ATTEMPTS - attemptCount) + " 次机会。" :
                    "Format error! Please check your logical assertions. You have " + (MAX_ATTEMPTS - attemptCount) + " attempts left.";
        }

        return feedback;
    }
}
