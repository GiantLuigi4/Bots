package idek;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class TicTacToe {

    static ArrayList<Integer> playerPositions = new ArrayList<>();
    static ArrayList<Integer> systemPositions = new ArrayList<>();
    static boolean twoP = false;

    public static void main(String[] args) {
        char[][] board = {
            { ' ', '|', ' ', '|', ' '},
            { '-', '+', '-', '+', '-'},
            { ' ', '|', ' ', '|', ' '},
            { '-', '+', '-', '+', '-'},
            { ' ', '|', ' ', '|', ' '}
        };
        printBoard(board);

        System.out.println("Wanna play with a real friend? Write true or false.");
        Scanner sc = new Scanner(System.in);
        String stringBool = sc.next();
        while (!isBoolean(stringBool)) {
            System.err.println("I've said TRUE or FALSE, not " + stringBool + "!!");
            stringBool = sc.next();
        }
        twoP = Boolean.parseBoolean(stringBool);
        if (!twoP) {
            System.out.println("It's YOU (X) against COMPUTER (0)");

            while (true) {
                System.out.println("Write a number from 1 (left-upper corner) to 9 (right-down corner)");
                int playerPos = rightPosition(sc);

                while (playerPositions.contains(playerPos) || systemPositions.contains(playerPos)) {
                    System.out.println("Position Taken! Choose another one you dumb.");
                    playerPos = rightPosition(sc);
                }

                placePiece(board, playerPos, "player");

                printBoard(board);

                String winner = checkWinner();
                if (!winner.equals("")) {
                    System.out.println(winner);
                    System.exit(0);
                }

                System.out.println("System turn!");
                int systemPos = systemAI();

                while (playerPositions.contains(systemPos) || systemPositions.contains(systemPos)) {
                    System.out.println("Position Taken! System is dumb.");
                    systemPos = systemAI();
                }
                placePiece(board, systemPos, "system");

                printBoard(board);

                String winner2 = checkWinner();
                if (!winner2.equals("")) {
                    System.out.println(winner2);
                    System.exit(0);
                }
            }
        } else {
            System.out.println("It's YOU (X) against A FRIEND (0)");

            while (true) {
                System.out.println("P1: Write a number from 1 (left-upper corner) to 9 (right-down corner)");
                int firstPlayerPos = rightPosition(sc);

                while (playerPositions.contains(firstPlayerPos) || systemPositions.contains(firstPlayerPos)) {
                    System.out.println("Position Taken! Choose another one you dumb.");
                    firstPlayerPos = rightPosition(sc);
                }

                placePiece(board, firstPlayerPos, "player");

                printBoard(board);

                String winner = checkWinner();
                if (!winner.equals("")) {
                    System.out.println(winner);
                    System.exit(0);
                }

                System.out.println("P2: Write a number from 1 (left-upper corner) to 9 (right-down corner)");
                int secondPlayerPos = rightPosition(sc);

                while (playerPositions.contains(secondPlayerPos) || systemPositions.contains(secondPlayerPos)) {
                    System.out.println("Position Taken! Your friend is a bit dumb huh.");
                    secondPlayerPos = rightPosition(sc);
                }
                placePiece(board, secondPlayerPos, "system");

                printBoard(board);

                String winner2 = checkWinner();
                if (!winner2.equals("")) {
                    System.out.println(winner2);
                    System.exit(0);
                }
            }
        }
    }


    public static void printBoard(char[][] gameBoard) {
        for (char[] row : gameBoard) {
            for (char c : row) {
                System.out.print(c);
            }
            System.out.println();
        }
    }

    public static void placePiece(char[][] gameBoard, int pos, String user) {
        char symbol = ' ';

        if (user.toLowerCase().equals("player")) {
            symbol = 'X';
            playerPositions.add(pos);
        } else if (user.toLowerCase().equals("system")) {
            symbol = '0';
            systemPositions.add(pos);
        }

        switch (pos) {
            case 1:
                gameBoard[0][0] = symbol;
                break;
            case 2:
                gameBoard[0][2] = symbol;
                break;
            case 3:
                gameBoard[0][4] = symbol;
                break;
            case 4:
                gameBoard[2][0] = symbol;
                break;
            case 5:
                gameBoard[2][2] = symbol;
                break;
            case 6:
                gameBoard[2][4] = symbol;
                break;
            case 7:
                gameBoard[4][0] = symbol;
                break;
            case 8:
                gameBoard[4][2] = symbol;
                break;
            case 9:
                gameBoard[4][4] = symbol;
                break;
            default:
                System.out.println("Invalid position! Choose from 1 to 9!");
                Scanner sc = new Scanner(System.in);
                int newPos = rightPosition(sc);
                placePiece(gameBoard, newPos, user);
                /*if (newPos < 10 && newPos > 0)
                    printBoard(gameBoard);*/
                break;
        }
    }

    public static int systemAI() {
        Random rand = new Random();
        AtomicInteger pos = new AtomicInteger(rand.nextInt(9) + 1);
        List<Integer> top = Arrays.asList(1, 2);
        List<Integer> top2 = Arrays.asList(2, 3);
        List<Integer> middle = Arrays.asList(4, 5);
        List<Integer> middle2 = Arrays.asList(5, 6);
        List<Integer> bottom = Arrays.asList(7, 8);
        List<Integer> bottom2 = Arrays.asList(8, 9);
        List<Integer> col11 = Arrays.asList(1, 4);
        List<Integer> col12 = Arrays.asList(4, 7);
        List<Integer> col21 = Arrays.asList(2, 5);
        List<Integer> col22 = Arrays.asList(5, 8);
        List<Integer> col31 = Arrays.asList(3, 6);
        List<Integer> col32 = Arrays.asList(6, 9);
        List<Integer> cross11 = Arrays.asList(1, 5);
        List<Integer> cross12 = Arrays.asList(5, 9);
        List<Integer> cross21 = Arrays.asList(3, 5);
        List<Integer> cross22 = Arrays.asList(5, 7);

        List<List<Integer>> almostWins = new ArrayList<>();

        almostWins.add(top);
        almostWins.add(top2);
        almostWins.add(middle);
        almostWins.add(middle2);
        almostWins.add(bottom);
        almostWins.add(bottom2);
        almostWins.add(col11);
        almostWins.add(col12);
        almostWins.add(col21);
        almostWins.add(col22);
        almostWins.add(col31);
        almostWins.add(col32);
        almostWins.add(cross11);
        almostWins.add(cross12);
        almostWins.add(cross21);
        almostWins.add(cross22);

        for (List<Integer> l : almostWins) {
            checkLists(l, pos);
        }
        return pos.get();
    }

    public static String checkWinner() {
        for (List<Integer> l : winningList()) {
            if (playerPositions.containsAll(l)) {
                return twoP ? "P1 (X) wins!" : "You won! System got beaten";
            } else if (systemPositions.containsAll(l)) {
                return twoP ? "P2 (0) wins! Your friend beat you lol" : "System won!";
            } else if (playerPositions.size() + systemPositions.size() == 9) {
                return "Tie!";
            }
        }
        return "";
    }

    public static List<List<Integer>> winningList() {
        List<Integer> topRow = Arrays.asList(1, 2, 3);
        List<Integer> middleRow = Arrays.asList(4, 5, 6);
        List<Integer> bottomRow = Arrays.asList(7, 8, 9);
        List<Integer> col1 = Arrays.asList(1, 4, 7);
        List<Integer> col2 = Arrays.asList(2, 5, 8);
        List<Integer> col3 = Arrays.asList(3, 6, 9);
        List<Integer> cross1 = Arrays.asList(1, 5, 9);
        List<Integer> cross2 = Arrays.asList(3, 5, 7);

        List<List<Integer>> winnings = new ArrayList<>();

        winnings.add(topRow);
        winnings.add(middleRow);
        winnings.add(bottomRow);
        winnings.add(col1);
        winnings.add(col2);
        winnings.add(col3);
        winnings.add(cross1);
        winnings.add(cross2);

        return winnings;
    }

    public static void checkLists(List<Integer> l, AtomicInteger pos) {
        if (playerPositions.containsAll(l) || systemPositions.containsAll(l)) {
            System.out.println(l);
            AtomicReference<List<Integer>> l1 = new AtomicReference<>();
            winningList().forEach(list -> {
                if (list.containsAll(l))
                    l1.set(list);
            });
            System.out.println(l1);
            List<Integer> realList = l1.get();
            for (Integer o : realList) {
                if (!l.contains(o) && !playerPositions.contains(o) && !systemPositions.contains(o))
                    pos.set(o);
            }
        }
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            int i = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static int rightPosition(Scanner sc) {
        String stringPos = sc.next();
        while (!isNumeric(stringPos)) {
            System.err.println("I've said a NUMBER, not " + stringPos + "!!");
            stringPos = sc.next();
        }
        return Integer.parseInt(stringPos);
    }

    public static boolean isBoolean(String strBool) {
        if (strBool == null) {
            return false;
        }
        return strBool.equalsIgnoreCase("true") || (strBool.equalsIgnoreCase("false"));
    }
}
