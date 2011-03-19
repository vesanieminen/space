package solarsystem;

import javax.swing.JFrame;

import physics.PhysicalObject;
import physics.Physics;
import space.Space;


import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class SolarSystem extends JFrame implements MouseWheelListener,
        MouseMotionListener, KeyListener, Space {
    public static final double EARTH_WEIGHT = 5.9736e24;
    private static final double ASTRONOMICAL_UNIT = 149597870.7e3;

    private static final long serialVersionUID = 1532817796535372081L;

    private static final double G = 6.67428e-11; // m3/kgs2
    private static List<PhysicalObject> objects = new ArrayList<PhysicalObject>();
    static double centrex = 0.0;
    static double centrey = 0.0;
    static double scale = 10;
    private static boolean showWake = false;
    private static int step = 0;
    private static int nrOfObjects = 75;
    private static int frameRate = 25;

    static JFrame frame;

    public SolarSystem() {
        setBackground(Color.BLACK);
        SolarSystem.frame = this;
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
        final SolarSystem space = new SolarSystem();
        space.addMouseWheelListener(space);
        space.addMouseMotionListener(space);
        space.addKeyListener(space);
        space.setSize(800, 820);

        space.setStepSize(3600 * 24 * 7);

        double outerLimit = ASTRONOMICAL_UNIT * 20;

        for (int i = 0; i < nrOfObjects; i++) {
            double angle = randSquare() * 2 * Math.PI;
            double radius = (0.1 + 0.9 * Math.sqrt(randSquare())) * outerLimit;
            double weightKilos = 1e3 * EARTH_WEIGHT * (Math.pow(0.00001 + 0.99999 * randSquare(), 12));
            double x = radius * Math.sin(angle);
            double y = radius * Math.cos(angle);
            double speedRandom = Math.sqrt(1 / radius) * 2978000*1500 * (0.4 + 0.6 * randSquare());

            double vx = speedRandom * Math.sin(angle - Math.PI / 2);
            double vy = speedRandom * Math.cos(angle - Math.PI / 2);
            add(weightKilos, x, y, vx, vy, 1);
        }

        scale = outerLimit / space.getWidth();

        add(EARTH_WEIGHT * 20000, 0, 0, 0, 0, 1);
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

    private static double randSquare() {
        double random = Math.random();
        return random * random;
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
        for (PhysicalObject aff : objects) {
            double fx = 0;
            double fy = 0;
            for (PhysicalObject oth : objects) {
                if (aff == oth)
                    continue;
                double[] d = new double[]{aff.x - oth.x, aff.y - oth.y};
                double r2 = Math.pow(d[0], 2) + Math.pow(d[1], 2);
                double f = G * aff.mass * oth.mass / r2;
                double sqrtOfR2 = Math.sqrt(r2);
                fx += f * d[0] / sqrtOfR2;
                fy += f * d[1] / sqrtOfR2;
            }
            double ax = fx / aff.mass;
            double ay = fy / aff.mass;
            aff.x = aff.x - ax * Math.pow(Physics.seconds, 2) / 2 + aff.vx * Physics.seconds;
            aff.y = aff.y - ay * Math.pow(Physics.seconds, 2) / 2 + aff.vy * Physics.seconds;
            aff.vx = aff.vx - ax * Physics.seconds;
            aff.vy = aff.vy - ay * Physics.seconds;
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
                if (Math.sqrt(Math.pow(one.x - other.x, 2) + Math.pow(one.y - other.y, 2)) < 5e9) {
                    one.absorb(other);
                    remove.add(other);
                }
            }
        }
        objects.removeAll(remove);
    }


    /* (non-Javadoc)
	 * @see space.SpaceInterface#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
    @Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
        scale = scale + scale * (Math.min(9, e.getWheelRotation())) / 10 + 0.0001;
        getGraphics().clearRect(0, 0, getWidth(), getHeight());
    }

    private static Point lastDrag = null;


    /* (non-Javadoc)
	 * @see space.SpaceInterface#mouseDragged(java.awt.event.MouseEvent)
	 */
    @Override
	public void mouseDragged(final MouseEvent e) {
        if (lastDrag == null) {
            lastDrag = e.getPoint();
        }
        centrex = centrex - ((e.getX() - lastDrag.x) * scale);
        centrey = centrey - ((e.getY() - lastDrag.y) * scale);
        lastDrag = e.getPoint();
        getGraphics().clearRect(0, 0, getWidth(), getHeight());
    }


    /* (non-Javadoc)
	 * @see space.SpaceInterface#mouseMoved(java.awt.event.MouseEvent)
	 */
    @Override
	public void mouseMoved(MouseEvent e) {
        lastDrag = null;
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
        graphics.setColor(SolarSystem.weightToColor(po.mass));
        int diameter = po.mass >= SolarSystem.EARTH_WEIGHT * 10000 ? 7 : 2;
        int xtmp = (int) ((po.x - SolarSystem.centrex) / SolarSystem.scale + SolarSystem.frame.getSize().width / 2);
        int ytmp = (int) ((po.y - SolarSystem.centrey) / SolarSystem.scale + SolarSystem.frame.getSize().height / 2);
        graphics.fillOval(
                xtmp-diameter/2,
                ytmp-diameter/2,
                diameter,
                diameter);
    }    

}
