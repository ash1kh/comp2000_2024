//working code for main milestone 1
//part 1
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new Main();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    class Canvas extends JPanel {
        private Grid grid;
        private Point mousePos;
        private Queue<Point> mouseTrail;
        private Timer timer;
        private TimerTask clearTrailTask;
        private TimerTask shortenTrailTask;
        private boolean mouseClicked;

        public Canvas() {
            setPreferredSize(new Dimension(720, 720));
            grid = new Grid(20, 20, 35); // 20x20 grid with cell size 35
            mouseTrail = new LinkedList<>();
            timer = new Timer();
            clearTrailTask = null;
            shortenTrailTask = null;
            mouseClicked = false;

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    mousePos = e.getPoint();
                    grid.highlightCellAt(mousePos);
                    if (mouseTrail.size() >= 100) {
                        mouseTrail.poll(); // Remove oldest trail point
                    }
                    mouseTrail.add(new Point(mousePos)); // Add new trail point
                    repaint();

                    // Cancel any existing timer tasks
                    if (clearTrailTask != null) {
                        clearTrailTask.cancel();
                    }

                    if (shortenTrailTask != null) {
                        shortenTrailTask.cancel();
                    }

                    // Schedule the task to gradually shorten the trail after 1 second
                    clearTrailTask = new TimerTask() {
                        @Override
                        public void run() {
                            startShorteningTrail();
                        }
                    };
                    timer.schedule(clearTrailTask, 1000); // 1000 milliseconds = 1 second
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    mouseClicked = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    mouseClicked = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            grid.paint(g);
            drawMouseTrails(g);
        }

        private void drawMouseTrails(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)); // Set transparency

            // Draw the mouse trail in gray
            g2d.setColor(Color.GRAY);
            for (Point p : mouseTrail) {
                g2d.fillOval(p.x - 5, p.y - 5, 10, 10); // Draw semi-transparent circles
            }

            // Draw the most recent point in black if the mouse is clicked
            if (mouseClicked && !mouseTrail.isEmpty()) {
                Point lastPoint = ((LinkedList<Point>) mouseTrail).getLast();
                g2d.setColor(Color.BLACK);
                g2d.fillOval(lastPoint.x - 5, lastPoint.y - 5, 10, 10);
            }
        }

        private void startShorteningTrail() {
            // Gradually shorten the trail length to zero
            shortenTrailTask = new TimerTask() {
                @Override
                public void run() {
                    if (!mouseTrail.isEmpty()) {
                        mouseTrail.poll(); // Remove the oldest point
                        repaint();
                    } else {
                        shortenTrailTask.cancel(); // Stop the timer once the trail is empty
                    }
                }
            };
            new Timer().scheduleAtFixedRate(shortenTrailTask, 0, 10); // 10 milliseconds interval to shorten the trail more quickly
        }
    }

    class Grid {
        private Cell[][] cells;
        private int rows;
        private int cols;
        private int cellSize;

        public Grid(int rows, int cols, int cellSize) {
            this.rows = rows;
            this.cols = cols;
            this.cellSize = cellSize;
            cells = new Cell[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    cells[i][j] = new Cell(j * cellSize + 10, i * cellSize + 10, cellSize, cellSize);
                }
            }
        }

        public void paint(Graphics g) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    cells[i][j].paint(g);
                }
            }
        }

        public void highlightCellAt(Point point) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    Cell cell = cells[i][j];
                    if (cell.contains(point)) {
                        cell.setHighlighted(true);
                    } else {
                        cell.setHighlighted(false);
                    }
                }
            }
        }
    }

    class Cell {
        private int x, y, width, height;
        private Color color;
        private boolean highlighted;

        public Cell(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = Color.WHITE; // Default color
            this.highlighted = false;
        }

        public void paint(Graphics g) {
            g.setColor(Color.BLACK);
            g.drawRect(x, y, width, height);
            g.setColor(highlighted ? Color.GRAY : color);
            g.fillRect(x + 1, y + 1, width - 1, height - 1); // Slightly smaller to see the border
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public void setHighlighted(boolean highlighted) {
            this.highlighted = highlighted;
        }

        public boolean contains(Point point) {
            return point.x >= x && point.x < x + width && point.y >= y && point.y < y + height;
        }
    }

    private Main() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Canvas canvas = new Canvas();
        this.setContentPane(canvas);
        this.pack();
        this.setVisible(true);
    }
}
