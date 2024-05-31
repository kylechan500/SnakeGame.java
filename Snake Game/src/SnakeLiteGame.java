import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Random;

public class SnakeLiteGame extends JFrame {

    private final int WIDTH = 600, HEIGHT = 600;
    private final int DOT_SIZE = 20;
    private final int ALL_DOTS = 900;
    private final int RAND_POS = 29;
    private int[] x = new int[ALL_DOTS];
    private int[] y = new int[ALL_DOTS];
    private int bodyParts;
    private int foodX;
    private int foodY;
    private int fruitsEaten;
    private int highScore;
    private char direction;
    private boolean running;
    private boolean paused;
    private Timer timer;
    private JPanel gamePanel;
    private final int DEFAULT_SPEED = 120;
    private int gameSpeed;
    private Random random;

    public SnakeLiteGame() {
        setTitle("Snake Lite");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };
        gamePanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();
        gamePanel.setBackground(Color.BLACK);

        add(gamePanel);
        pack();
        setLocationRelativeTo(null);

        random = new Random();
        showInstructions();
    }

    private void showInstructions() {
        JOptionPane.showMessageDialog(this,
                "Welcome to Snake Lite!\n" +
                        "Use the arrow keys to move the snake.\n" +
                        "Press 'P' to pause/resume the game.\n" +
                        "Press 'R' to restart the game.\n" +
                        "Avoid the walls and your own tail!\n" +
                        "Press 'W' to increase speed.\n" +
                        "Press 'S' to decrease speed.",
                "Game Instructions",
                JOptionPane.INFORMATION_MESSAGE);
        initGame();
        initKeyBindings();
        setVisible(true);
    }

    private void initGame() {
        bodyParts = 3;
        fruitsEaten = 0;
        direction = 'R';
        running = true;
        paused = false;
        gameSpeed = DEFAULT_SPEED;

        // Initialize the snake's body parts
        x[0] = 50;
        y[0] = 50;
        for (int i = 1; i < bodyParts; i++) {
            x[i] = x[0] - (i * DOT_SIZE);
            y[i] = y[0];
        }
        placeFood();

        timer = new Timer(gameSpeed, e -> updateGame());
        timer.setDelay(gameSpeed);
        timer.start();
    }
    private void initKeyBindings() {
        gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "moveLeft");
        gamePanel.getActionMap().put("moveLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (direction != 'R') direction = 'L';
            }
        });

        gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "moveRight");
        gamePanel.getActionMap().put("moveRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (direction != 'L') direction = 'R';
            }
        });

        gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "moveUp");
        gamePanel.getActionMap().put("moveUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (direction != 'D') direction = 'U';
            }
        });

        gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "moveDown");
        gamePanel.getActionMap().put("moveDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (direction != 'U') direction = 'D';
            }
        });

        gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "pauseGame");
        gamePanel.getActionMap().put("pauseGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseGame();
            }
        });

        gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "restartGame");
        gamePanel.getActionMap().put("restartGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
            }
        });

        gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), "increaseSpeed");
        gamePanel.getActionMap().put("increaseSpeed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                increaseSpeed();
            }
        });

        gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "decreaseSpeed");
        gamePanel.getActionMap().put("decreaseSpeed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                decreaseSpeed();
            }
        });
    }

    private void draw(Graphics g) {
        if (running) {
            g.setColor(Color.GREEN);
            for (int i = 0; i < bodyParts; i++) {
                g.fillRect(x[i], y[i], DOT_SIZE, DOT_SIZE);
            }

            g.setColor(Color.RED);
            g.fillRect(foodX, foodY, DOT_SIZE, DOT_SIZE);

            g.setColor(Color.WHITE);
            g.drawString("Score: " + fruitsEaten, 10, 20);
            g.drawString("High Score: " + highScore, 10, 40);
        } else {
            gameOver(g);
        }
    }

    private void gameOver(Graphics g) {
        String msg = "Game Over";
        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = getFontMetrics(small);

        g.setColor(Color.WHITE);
        g.setFont(small);
        g.drawString(msg, (WIDTH - metr.stringWidth(msg)) / 2, HEIGHT / 2 - 20);

        g.drawString("Score: " + fruitsEaten, (WIDTH - metr.stringWidth("Score: " + fruitsEaten)) / 2, HEIGHT / 2);
        g.drawString("High Score: " + highScore, (WIDTH - metr.stringWidth("High Score: " + highScore)) / 2, HEIGHT / 2 + 20);
    }


    private void updateGame() {
        if (running && !paused) {
            moveSnake();
            checkFoodCollision();
            checkCollision();
        }
        gamePanel.repaint();
    }

    private void moveSnake() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U':
                y[0] -= DOT_SIZE;
                break;
            case 'D':
                y[0] += DOT_SIZE;
                break;
            case 'L':
                x[0] -= DOT_SIZE;
                break;
            case 'R':
                x[0] += DOT_SIZE;
                break;
        }
    }

    private void checkCollision() {
        // Check for wall collision
        if (x[0] < 0 || x[0] >= WIDTH || y[0] < 0 || y[0] >= HEIGHT) {
            running = false;
        }

        // Check for self-collision
        for (int i = bodyParts; i > 0; i--) {
            if ((i > 4) && (x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
                break;
            }
        }

        if (!running) {
            timer.stop();
        }
    }

    private void placeFood() {
        int r = random.nextInt(RAND_POS);
        foodX = ((r * DOT_SIZE) + DOT_SIZE / 2);
        r = random.nextInt(RAND_POS);
        foodY = ((r * DOT_SIZE) + DOT_SIZE / 2);
    }

    private void checkFoodCollision() {
        if ((x[0] >= foodX - DOT_SIZE / 2) && (x[0] <= foodX + DOT_SIZE / 2) &&
                (y[0] >= foodY - DOT_SIZE / 2) && (y[0] <= foodY + DOT_SIZE / 2)) {
            bodyParts++;
            placeFood();
            fruitsEaten++;
            if (fruitsEaten > highScore) {
                highScore = fruitsEaten;
            }
        }
    }


    private void pauseGame() {
        if (running) {
            if (paused) {
                paused = false;
                timer.start();
            } else {
                paused = true;
                timer.stop();
            }
        }
    }

    private void restartGame() {
        initGame();
    }

    private void increaseSpeed() {
        if (gameSpeed > 20) {
            gameSpeed -= 20;
            timer.setDelay(gameSpeed);
        }
    }

    private void decreaseSpeed() {
        if (gameSpeed < 200) {
            gameSpeed += 20;
            timer.setDelay(gameSpeed);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SnakeLiteGame game = new SnakeLiteGame();
                game.setVisible(true);
            }
        });
    }
}
