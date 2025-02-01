import java.io.*;

public class CastleGame {
    public static void main(String[] args) {
        // 1. 初始化游戏状态
        GameState state = new GameState();
        state.initialize();

        // 2. 主游戏循环
        while (!state.isGameOver()) {
            // 获取玩家输入
            String playerAction = state.getPlayerInput();

            // 将玩家行为翻译为SMCDEL查询
            String smcdelQuery = state.translateToSMCDEL(playerAction);

            // 调用SMCDEL工具并获取结果
            String smcdelOutput = callSMCDEL(smcdelQuery);

            // 解析SMCDEL输出并更新游戏状态
            state.updateState(smcdelOutput);

            // 显示当前游戏状态
            state.displayState();
        }

        // 游戏结束
        System.out.println("游戏结束！感谢参与。");
    }

    private static String callSMCDEL(String smcdelQuery) {
        try {
            // 将查询写入文件
            File queryFile = new File("GameQuery.smcdel.txt");
            FileWriter writer = new FileWriter(queryFile);
            writer.write(smcdelQuery);
            writer.close();

            // 调用SMCDEL命令行工具
            ProcessBuilder pb = new ProcessBuilder("./smcdel", "GameQuery.smcdel.txt");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 读取SMCDEL输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
            return output.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error in SMCDEL execution.";
        }
    }
}
