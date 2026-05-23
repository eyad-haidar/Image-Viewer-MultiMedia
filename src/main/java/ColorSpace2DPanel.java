import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * 2D projections of color spaces (requirement 4): RGB plane, HSV hue wheel, chroma planes.
 */
public class ColorSpace2DPanel extends JPanel {

	public interface PickListener {
		void onColorPicked(int rgb);
	}

	private ColorSpaceMath.System system = ColorSpaceMath.System.RGB;
	private int sliceChannel = 2;
	private float sliceValue = 0.5f;
	private PickListener pickListener;

	public ColorSpace2DPanel() {
		setPreferredSize(new Dimension(420, 280));
		setBackground(new Color(30, 33, 40));
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				pickAt(e.getX(), e.getY());
			}
		});
	}

	public void setPickListener(PickListener listener) {
		this.pickListener = listener;
	}

	public void setSystem(ColorSpaceMath.System system) {
		this.system = system;
		sliceChannel = system == ColorSpaceMath.System.HSV ? 2 : 2;
		repaint();
	}

	public void setSliceValue(float v) {
		sliceValue = Math.max(0f, Math.min(1f, v));
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int w = getWidth();
		int h = getHeight();
		g2.setColor(Color.WHITE);
		g2.drawString(system.name() + " 2D — click to pick color", 8, 16);

		BufferedImage plot = new BufferedImage(w, h - 24, BufferedImage.TYPE_INT_RGB);
		int ph = plot.getHeight();
		int pw = plot.getWidth();
		for (int y = 0; y < ph; y++) {
			for (int x = 0; x < pw; x++) {
				float u = x / (float) pw;
				float v = 1f - y / (float) ph;
				int rgb = colorAt2D(u, v);
				plot.setRGB(x, y, rgb);
			}
		}
		g2.drawImage(plot, 0, 20, null);
	}

	private int colorAt2D(float u, float v) {
		switch (system) {
		case RGB: {
			float r = u;
			float g = v;
			float b = sliceValue;
			return ColorSpaceMath.componentsToRgb(new float[] { r, g, b }, system);
		}
		case CMYK: {
			float c = u;
			float m = v;
			return ColorSpaceMath.componentsToRgb(new float[] { c, m, sliceValue, 0f }, system);
		}
		case HSV: {
			double ang = u * 2 * Math.PI;
			float s = v;
			float h = (float) (ang / (2 * Math.PI));
			return ColorSpaceMath.componentsToRgb(new float[] { h, s, sliceValue }, system);
		}
		case YUV:
		case YCbCr: {
			float y = sliceValue;
			return ColorSpaceMath.componentsToRgb(new float[] { y, u, v }, system);
		}
		case LAB: {
			float l = sliceValue;
			return ColorSpaceMath.componentsToRgb(new float[] { l, u, v }, system);
		}
		default:
			return 0;
		}
	}

	private void pickAt(int mx, int my) {
		int ph = getHeight() - 24;
		if (my < 20 || ph <= 0) {
			return;
		}
		float u = mx / (float) getWidth();
		float v = 1f - (my - 20) / (float) ph;
		int rgb = colorAt2D(u, v);
		if (pickListener != null) {
			pickListener.onColorPicked(rgb);
		}
	}
}
