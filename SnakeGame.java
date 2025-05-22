import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class SnakeGame extends Frame {
    static final int WIDTH = 800;
    static final int HEIGHT = 800;
    static final int BOX_SIZE = 20;
    static final int FRAME_THICKNESS = 20;

    SnakeList slist;
    List<Food> foods;
    List<Bomb> bombs;
    boolean gameOver = false;

    public static void main(String[] args) {
        SnakeGame game = new SnakeGame();
        game.setSize(WIDTH + FRAME_THICKNESS * 2, HEIGHT + FRAME_THICKNESS * 2);
        game.setVisible(true);
        game.start();
    }

    public SnakeGame() {
        slist = new SnakeList(new Point(40, 60));
        foods = new ArrayList<>();
        bombs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            foods.add(new Food());
        }
        addBomb();

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (slist.ySpeed == 0) {
                    if (e.getKeyCode() == KeyEvent.VK_UP) {
                        slist.dir(0, -1);
                    }
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        slist.dir(0, 1);
                    }
                }
                if (slist.xSpeed == 0) {
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        slist.dir(-1, 0);
                    }
                    if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        slist.dir(1, 0);
                    }
                }
            }
        });

        setBackground(Color.BLACK);
    }

    public void start() {
        while (true) {
            if (gameOver) {
                repaint();
                break;
            }
            for (Food food : foods) {
                if (slist.isEaten(food)) {
                    slist.addScore(food.getPoints());
                    food.ranLocation();
                    slist.addLast(new Point(slist.head.v.x, slist.head.v.y));
                }
            }
            for (Bomb bomb : bombs) {
                if (slist.isEaten(bomb)) {
                    gameOver = true;
                    endGame();
                }
            }
            slist.update();
            slist.collision();
            repaint();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void paint(Graphics g) {
        // Draw the frame
        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), FRAME_THICKNESS);
        g.fillRect(0, 0, FRAME_THICKNESS, getHeight());
        g.fillRect(getWidth() - FRAME_THICKNESS, 0, FRAME_THICKNESS, getHeight());
        g.fillRect(0, getHeight() - FRAME_THICKNESS, getWidth(), FRAME_THICKNESS);

        if (!gameOver) {
            for (Food food : foods) {
                food.show(g);
            }
            for (Bomb bomb : bombs) {
                bomb.show(g);
            }
            slist.show(g);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Score: " + slist.getScore(), 20 , 55);
        } else {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("Game Over!!!", getWidth() / 2 - 150, getHeight() / 2 - 25);
            g.drawString("Score: " + slist.getScore(), getWidth() / 2 - 100, getHeight() / 2 + 25);
        }
    }

    public void endGame() {
        repaint();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(0);
            }
        }, 2000);
    }

    public void addBomb() {
        Bomb bomb = new Bomb();
        bombs.add(bomb);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                bombs.remove(bomb);
                repaint();
            }
        }, 10000); // Bomb disappears after 10 seconds
    }

    class SnakeList {
        float xSpeed = 1;
        float ySpeed = 0;
        int w = BOX_SIZE;
        Node head = null;
        Node tail = null;
        int size = 0;
        int maxSize = 200;
        int score = 0;

        private class Node {
            Node next;
            Node prev;
            Point v;

            public Node(Point v, Node next, Node prev) {
                this.v = v;
                this.next = next;
                this.prev = prev;
            }
        }

        public SnakeList(Point headSnake) {
            this.head = this.tail = new Node(headSnake, null, null);
            size++;
        }

        public int Size() {
            return size - 1;
        }

        public int getScore() {
            return score;
        }

        public boolean isEmpty() {
            return Size() == 0;
        }

        public void addLast(Point tailSnake) {
            if (Size() <= maxSize) {
                Node thisNode = new Node(tailSnake, null, this.tail);
                this.tail.next = thisNode;
                this.tail = thisNode;
                size++;
            }
        }

        public void show(Graphics g) {
            Node current = tail;
            while (current != head) {
                g.setColor(new Color(36, 152, 14));
                g.fillRect(current.v.x + FRAME_THICKNESS, current.v.y + FRAME_THICKNESS, w, w);
                current = current.prev;
            }
            g.setColor(new Color(25, 120, 7));
            g.fillOval(head.v.x + FRAME_THICKNESS, head.v.y + FRAME_THICKNESS, w, w);
        }

        public void update() {
            Node current = tail;
            while (current.prev != null) {
                current.v = new Point(current.prev.v);
                current = current.prev;
            }
            head.v.x += xSpeed * w;
            head.v.y += ySpeed * w;

            if (head.v.x < 0 || head.v.x >= WIDTH || head.v.y < 0 || head.v.y >= HEIGHT) {
                gameOver = true;
                endGame();
            }
        }

        public void dir(float x, float y) {
            xSpeed = x;
            ySpeed = y;
        }

        public boolean isEaten(Food f) {
            return head.v.equals(new Point(f.x, f.y));
        }

        public boolean isEaten(Bomb b) {
            return head.v.equals(new Point(b.x, b.y));
        }

        public void collision() {
            Node current = tail;
            while (current.prev != null) {
                if (head.v.equals(current.v)) {
                    gameOver = true;
                    endGame();
                }
                current = current.prev;
            }
        }

        public void addScore(int points) {
            score += points;
        }
    }

    class Food {
        int x, y;
        Color color;
        int points;

        public Food() {
            ranLocation();
            setColorAndPoints();
        }

        private void setColorAndPoints() {
            Random rand = new Random();
            int colorChoice = rand.nextInt(5);
            switch (colorChoice) {
                case 0:
                    color = Color.red;
                    points = 1;
                    break;
                case 1:
                    color = Color.YELLOW;
                    points = 2;

                    break;
                case 2:
                    color = Color.magenta;
                    points = 3;
                    break;
                case 3:
                    color = Color.CYAN;
                    points = 4;
                    break;
                case 4:
                    color = Color.ORANGE;
                    points = 5;
                    break;
            }
        }

        public void ranLocation() {
            Random rand = new Random();
            x = (rand.nextInt(WIDTH / BOX_SIZE - 2) + 1) * BOX_SIZE;
            y = (rand.nextInt(HEIGHT / BOX_SIZE - 2) + 1) * BOX_SIZE;
        }

        public void show(Graphics g) {
            g.setColor(color);
            g.fillRect(x + FRAME_THICKNESS, y + FRAME_THICKNESS, BOX_SIZE, BOX_SIZE);
        }

        public int getPoints() {
            return points;
        }
    }

    class Bomb {
        int x, y;
        Color color = Color.white;

        public Bomb() {
            ranLocation();
        }

        public void ranLocation() {
            Random rand = new Random();
            x = (rand.nextInt(WIDTH / BOX_SIZE - 2) + 1) * BOX_SIZE;
            y = (rand.nextInt(HEIGHT / BOX_SIZE - 2) + 1) * BOX_SIZE;
        }

        public void show(Graphics g) {
            g.setColor(color);
            g.fillRect(x + FRAME_THICKNESS, y + FRAME_THICKNESS, BOX_SIZE, BOX_SIZE);
        }
    }
}
