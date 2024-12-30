import java.awt.*; 
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int BOARD_WIDTH = 360;
    int BOARD_HEIGHT = 640;

    // Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // Game states
    enum GameState { START_SCREEN, LEVEL_SELECTION, GAME_PLAY, TIPS_SCREEN }
    GameState currentState = GameState.START_SCREEN;

    // Bird properties
    int birdX = BOARD_WIDTH / 8;
    int birdY = BOARD_HEIGHT / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Pipe properties
    int pipeX = BOARD_WIDTH;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    Bird bird;
    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double score = 0;
    int velocityX = -4;
    int velocityY = 0;
    int gravity = 1;
    int openingSpace = BOARD_HEIGHT / 4;
    int pipeDelay = 2500; // Default delay

    // Buttons
    private JButton startButton;
    private JButton tipsButton;
    private JButton easyButton;
    private JButton mediumButton;
    private JButton hardButton;
    private JButton backButton;  // For "Back to Home" button

    FlappyBird() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        // Load images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // Initialize bird and pipes
        bird = new Bird(birdImg);
        pipes = new ArrayList<>();

        // Game loop
        gameLoop = new Timer(1000 / 60, this);

        // Pipe placement timer
        placePipeTimer = new Timer(pipeDelay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
    }

    void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.x = BOARD_WIDTH;
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.x = BOARD_WIDTH;
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, this.BOARD_WIDTH, this.BOARD_HEIGHT, null);

        switch (currentState) {
            case START_SCREEN:
                drawStartScreen(g);
                break;
            case LEVEL_SELECTION:
                drawLevelSelectionScreen(g);
                break;
            case GAME_PLAY:
                drawGamePlay(g);
                break;
            case TIPS_SCREEN:
                drawTipsScreen(g);
                break;
        }
    }

    void drawStartScreen(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Flappy Bird", 80, 200);

        if (startButton == null || tipsButton == null) {
            startButton = new JButton("Start Game");
            startButton.setBounds(100, 300, 160, 40);
            startButton.addActionListener(e -> {
                currentState = GameState.LEVEL_SELECTION;
                remove(startButton);
                remove(tipsButton);
                repaint();
            });

            tipsButton = new JButton("Tips");
            tipsButton.setBounds(100, 350, 160, 40);
            tipsButton.addActionListener(e -> {
                currentState = GameState.TIPS_SCREEN;
                remove(startButton);
                remove(tipsButton);
                repaint();
            });

            add(startButton);
            add(tipsButton);
        }
    }

    void drawLevelSelectionScreen(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Select Level", 110, 100);

        if (easyButton == null || mediumButton == null || hardButton == null) {
            easyButton = new JButton("Easy");
            easyButton.setBounds(140, 200, 80, 40);
            easyButton.addActionListener(e -> {
                velocityX = -3;
                openingSpace = BOARD_HEIGHT / 3;
                pipeDelay = 3000;
                gravity = 1;  
                startGame();
                remove(easyButton);
                remove(mediumButton);
                remove(hardButton);
                requestFocusInWindow();
                repaint();
            });

            mediumButton = new JButton("Medium");
            mediumButton.setBounds(140, 300, 80, 40);
            mediumButton.addActionListener(e -> {
                velocityX = -5;
                openingSpace = BOARD_HEIGHT / 4;
                pipeDelay = 1300;
                gravity = 1;  
                startGame();
                remove(easyButton);
                remove(mediumButton);
                remove(hardButton);
                requestFocusInWindow();
                repaint();
            });

            hardButton = new JButton("Hard");
            hardButton.setBounds(140, 400, 80, 40);
            hardButton.addActionListener(e -> {
                velocityX = -7;
                openingSpace = BOARD_HEIGHT / 5;
                pipeDelay = 750;
                gravity = 1 ;
                startGame();
                remove(easyButton);
                remove(mediumButton);
                remove(hardButton);
                requestFocusInWindow();
                repaint();
            });

            add(easyButton);
            add(mediumButton);
            add(hardButton);
        }
    }

    void drawGamePlay(Graphics g) {
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + (int) score, 10, 35);
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    void drawTipsScreen(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Game Tips", 120, 100);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("- Press SPACE to make the bird jump.", 50, 200);
        g.drawString("- Avoid hitting the pipes.", 50, 250);
        g.drawString("- Stay in the air as long as possible.", 50, 300);

        if (backButton == null) {
            backButton = new JButton("Back to Home");
            backButton.setBounds(100, 400, 160, 40);
            backButton.addActionListener(e -> {
                currentState = GameState.START_SCREEN;
                add(startButton);
                add(tipsButton);
                remove(backButton);
                repaint();
            });

            add(backButton);
        }
    }

    public void move() {
        velocityY += gravity;  // تأثير الجاذبية على السرعة الرأسية
        bird.y += velocityY;  // تحريك الطائر بناءً على السرعة الرأسية

        // التأكد من أن الطائر لا يغادر الشاشة
        bird.y = Math.max(bird.y, 0);

        for (Pipe pipe : pipes) {
            pipe.x += velocityX;  // تحريك الأنابيب

            // زيادة النتيجة عند مرور الطائر عبر الأنابيب
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                pipe.passed = true;
            }

            // التحقق من التصادم مع الأنابيب
            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        // التأكد من أن الطائر لا يسقط خارج الشاشة
        if (bird.y > BOARD_HEIGHT) {
            gameOver = true;
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentState == GameState.GAME_PLAY) {
            move();
            repaint();
            if (gameOver) {
                gameLoop.stop();
                placePipeTimer.stop();
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        if (currentState == GameState.START_SCREEN) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                currentState = GameState.LEVEL_SELECTION;
                repaint();
            } else if (e.getKeyCode() == KeyEvent.VK_T) {
                currentState = GameState.TIPS_SCREEN;
                repaint();
            }
        } else if (currentState == GameState.GAME_PLAY) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                if (!gameOver) {
                    velocityY = -9;
                } else {
                    restartGame();
                }
            }
        }
    }

    void startGame() {
        currentState = GameState.GAME_PLAY;
        pipes.clear();
        score = 0;
        bird.y = birdY;
        velocityY = 0;
        gameOver = false;
        gameLoop.start();
        placePipeTimer.setDelay(pipeDelay);
        placePipeTimer.start();
        repaint();
    }

    void restartGame() {
        gameOver = false;
        startGame();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird");
        FlappyBird game = new FlappyBird();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
