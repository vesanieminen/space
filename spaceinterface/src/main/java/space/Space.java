package space;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import physics.PhysicalObject;

public interface Space {

	public abstract void paint(Graphics original);

	public abstract void setStepSize(double seconds);

	public abstract void step();

	public abstract void mouseMoved(MouseEvent e);

	public abstract void keyPressed(KeyEvent e);

	public abstract void keyReleased(KeyEvent e);

	public abstract void keyTyped(KeyEvent e);

	public abstract void paintPhysicalObject(Graphics2D graphics,
			PhysicalObject po);

}