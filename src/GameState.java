import java.util.Scanner;

public class GameState {
    private boolean gameOver;

    public void initialize() {
        // 初始化游戏变量和状态
        gameOver = false;
        System.out.println("欢迎来到古堡谋杀之夜！");
        // 显示初始背景
        displayBackground();
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public String getPlayerInput() {
        // 获取玩家的行动输入
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入你的行动: ");
        return scanner.nextLine();
    }

    public String translateToSMCDEL(String playerAction) {
        // 定义 SMCDEL 基础部分
        String baseSMCDEL = "VARS 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15\n" +
                "LAW\n" +
                "  ( (1 -> ~2) & (4 -> 3) & (7 -> (6 -> ~3)) & (8 -> 1) & (9 -> ~3) & (10 -> 5) )\n" +
                "OBS\n" +
                "  elsa: 1,8,15\n" +
                "  bolton: 2,9,12,14\n" +
                "  claire: 4,5,13\n" +
                "  victor: 5,10\n" +
                "  hamilton: 6,11\n" +
                "  stone: 3,7,12\n";

        // 根据玩家输入生成查询部分
        String query;
        if (playerAction.equalsIgnoreCase("查询房间")) {
            query = "TRUE?\n  {}\n  elsa knows that (1 & 8)";
        } else if (playerAction.equalsIgnoreCase("调查死者")) {
            query = "WHERE?\n  (15 & 3)";
        } else {
            query = "TRUE?\n  {}\n  stone knows whether 12";
        }

        // 拼接完整 SMCDEL 文件内容
        return baseSMCDEL + "\n" + query;
    }

    public void updateState(String smcdelOutput) {
        // 解析SMCDEL输出并更新状态
        System.out.println("SMCDEL输出: " + smcdelOutput);
        if (smcdelOutput.contains("Game Over Condition")) {
            gameOver = true;
        }
    }

    public void displayState() {
        // 显示当前游戏状态
        System.out.println("当前游戏状态更新中...");
    }

    private void displayBackground() {
        // 输出游戏背景故事
        System.out.println("你身处阿尔曼德古堡，雨夜，停电，副管家离奇死亡...");
    }
}
