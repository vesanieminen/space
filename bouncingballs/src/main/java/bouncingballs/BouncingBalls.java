package bouncingballs;

import javax.swing.JFrame;

import physics.PhysicalObject;
import physics.Physics;
import space.Space;


import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class BouncingBalls extends JFrame implements KeyListener, Space {
    public static final double EARTH_WEIGHT = 5.9736e24;
    static boolean IS_BREAKOUT = false; // Opens bottom, only active if IS_BOUNCING_BALLS is true

    private static final long serialVersionUID = 1532817796535372081L;

    private static List<PhysicalObject> objects = new ArrayList<PhysicalObject>();
    static double centrex = 0.0;
    static double centrey = 0.0;
    static double scale = 10;
    private static boolean showWake = false;
    private static int step = 0;
    private static int nrOfObjects = 75;
    private static int frameRate = 25;

    static JFrame frame;

    public BouncingBalls() {
        setBackground(Color.BLACK);
        BouncingBalls.frame = this;
    }

    /* (non-Javadoc)
	 * @see space.SpaceInterface#paint(java.awt.Graphics)
	 */
	@Override
    public void paint(Graphics original) {
        if (original != null) {
            BufferedImage buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = buffer.createGraphics();

            if (!showWake) {
                graphics.clearRect(0, 0, getWidth(), getHeight());
            }
            for (PhysicalObject po : objects) {
                paintPhysicalObject(graphics, po);
                String string = "Objects:" + objects.size() + " scale:" + scale + " steps:" + step + " frame rate: " + frameRate;
                setTitle(string);
            }
            original.drawImage(buffer, 0, 0, getWidth(), getHeight(), null);
        }

    }

    public static Color weightToColor(double weight) {
        if (weight < 1e10) return Color.GREEN;
        if (weight < 1e12) return Color.CYAN;
        if (weight < 1e14) return Color.MAGENTA;
        if (weight < 1e16) return Color.BLUE;
        if (weight < 1e18) return Color.GRAY;
        if (weight < 1e20) return Color.RED;
        if (weight < 1e22) return Color.ORANGE;
        if (weight < 1e25) return Color.PINK;
        if (weight < 1e28) return Color.YELLOW;
        return Color.WHITE;
    }

    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        final BouncingBalls space = new BouncingBalls();
        space.addKeyListener(space);
        space.setSize(800, 820);

        nrOfObjects = 50;
        space.setStepSize(1); // One second per iteration
        for (int i = 0; i < nrOfObjects; i++) {
            // radius,weight in [1,20]
            double radiusAndWeight = 1 + 19 * Math.random();
            //x,y in [max radius, width or height - max radius]
            BouncingBalls.add(radiusAndWeight, 20 + 760 * Math.random(), 20 + 760 * Math.random(), 3 - 6 * Math.random(), 3 - 6 * Math.random(), radiusAndWeight);
        }
        scale = 1;
        centrex = 400;
        centrey = 390; //Must compensate for title bar
        space.setVisible(true);
        while (true) {
            final long start = System.currentTimeMillis();
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    space.collide();
                    space.step();
                }
            });
            try {
                long ahead = 1000 / frameRate - (System.currentTimeMillis() - start);
                if (ahead > 50) {
                    Thread.sleep(ahead);
                    if(frameRate<25) frameRate++;
                } else {
                    Thread.sleep(50);
                    frameRate--;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /* (non-Javadoc)
	 * @see space.SpaceInterface#setStepSize(double)
	 */
    @Override
	public void setStepSize(double seconds) {
        Physics.seconds = seconds;
    }

    public static PhysicalObject add(double weightKilos, double x, double y,
                                     double vx, double vy, double radius) {
        PhysicalObject physicalObject = new PhysicalObject(weightKilos, x, y,
                vx, vy, radius);
        objects.add(physicalObject);
        return physicalObject;
    }

    /* (non-Javadoc)
	 * @see space.SpaceInterface#step()
	 */
    @Override
	public void step() {
        for (PhysicalObject physicalObject : objects) {
            physicalObject.x = physicalObject.x + physicalObject.vx * Physics.seconds;
            physicalObject.y = physicalObject.y + physicalObject.vy * Physics.seconds;
        }

        step++;
        paint(getGraphics());

    }

    private void collide() {
        List<PhysicalObject> remove = new ArrayList<PhysicalObject>();
        for (PhysicalObject one : objects) {
            if (remove.contains(one))
                continue;
            for (PhysicalObject other : objects) {
                if (one == other || remove.contains(other)) {
                    continue;
                }
                double distance = Math.sqrt(Math.pow(one.x - other.x, 2) + Math.pow(one.y - other.y, 2));
                double collsionDistance = one.radius + other.radius;
                if (distance < collsionDistance) {
                    one.hitBy(other);
                }
            }
            // Wall collision reverses speed in that direction
            if (one.x - one.radius < 0) {
                one.vx = -one.vx;
            }
            if (one.x + one.radius > 800) {
                one.vx = -one.vx;
            }
            if (one.y - one.radius < 0) {
                one.vy = -one.vy;
            }
            if (one.y + one.radius > 800 && !IS_BREAKOUT) {
                one.vy = -one.vy;
            } else if (one.y - one.radius > 800) {
                remove.add(one);
            }
        }
        objects.removeAll(remove);
    }

    /* (non-Javadoc)
	 * @see space.SpaceInterface#mouseMoved(java.awt.event.MouseEvent)
	 */
    @Override
	public void mouseMoved(MouseEvent e) {
    }


    /* (non-Javadoc)
	 * @see space.SpaceInterface#keyPressed(java.awt.event.KeyEvent)
	 */
    @Override
	public void keyPressed(KeyEvent e) {
    }


    /* (non-Javadoc)
	 * @see space.SpaceInterface#keyReleased(java.awt.event.KeyEvent)
	 */
    @Override
	public void keyReleased(KeyEvent e) {
    }


    /* (non-Javadoc)
	 * @see space.SpaceInterface#keyTyped(java.awt.event.KeyEvent)
	 */
    @Override
	public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == 'w')
            showWake = !showWake;
    }
    
    /* (non-Javadoc)
	 * @see space.SpaceInterface#paintPhysicalObject(java.awt.Graphics2D, physics.PhysicalObject)
	 */
    @Override
	public void paintPhysicalObject(Graphics2D graphics, PhysicalObject po) {
        graphics.setColor(Color.WHITE);
        int xtmp = (int) ((po.x - BouncingBalls.centrex)  + BouncingBalls.frame.getSize().width / 2);
        int ytmp = (int) ((po.y - BouncingBalls.centrey)  + BouncingBalls.frame.getSize().height / 2);
        graphics.fillOval(
                (int) (xtmp - po.radius ),
                (int) (ytmp - po.radius ),
                (int) (2 * po.radius),
                (int) (2 * po.radius));
    }    

}
