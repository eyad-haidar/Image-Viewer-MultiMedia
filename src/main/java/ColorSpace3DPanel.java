import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

/**
 * 3D color-space view with rotation, zoom, and color picking (requirements 4 & 5).
 */
public class ColorSpace3DPanel extends JPanel {

	public interface PickListener {
		void onColorPicked(int rgb);
	}

	private static final float[] CUBE = { 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1 };
	private static final int[][] EDGES = { { 0, 1 }, { 1, 2 }, { 2, 3 }, { 3, 0 }, { 4, 5 }, { 5, 6 }, { 6, 7 }, { 7, 4 },
			{ 0, 4 }, { 1, 5 }, { 2, 6 }, { 3, 7 } };

	private ColorSpaceMath.System system = ColorSpaceMath.System.RGB;
	private float rotX = 0.5f;
	private float rotY = 0.8f;
	private float zoom = 1.2f;
	private float[] cloudPoints;
	private int[] cloudRgb;
	private float[] highlight3D;
	private int highlightRgb = -1;
	private PickListener pickListener;
	private int lastMouseX;
	private int lastMouseY;
	private boolean dragging;

	public ColorSpace3DPanel() {
		setPreferredSize(new Dimension(420, 320));
		setBackground(new Color(25, 28, 35));

		MouseAdapter press = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				lastMouseX = e.getX();
				lastMouseY = e.getY();
				dragging = true;
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (!dragging) {
					return;
				}
				int dx = Math.abs(e.getX() - lastMouseX);
				int dy = Math.abs(e.getY() - lastMouseY);
				if (dx < 4 && dy < 4) {
					pickAt(e.getX(), e.getY());
				}
				dragging = false;
			}
		};
		addMouseListener(press);
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				rotY += (e.getX() - lastMouseX) * 0.01f;
				rotX += (e.getY() - lastMouseY) * 0.01f;
				lastMouseX = e.getX();
				lastMouseY = e.getY();
				repaint();
			}
		});
		addMouseWheelListener(e -> {
			zoom *= e.getWheelRotation() < 0 ? 1.08f : 0.92f;
			zoom = Math.max(0.4f, Math.min(3.5f, zoom));
			repaint();
		});
	}

	public void setPickListener(PickListener listener) {
		this.pickListener = listener;
	}

	public void setSystem(ColorSpaceMath.System system) {
		this.system = system;
		repaint();
	}

	public void setPointCloud(float[] points, int[] rgbColors) {
		this.cloudPoints = points;
		this.cloudRgb = rgbColors;
		repaint();
	}

	public void setHighlight(int rgb, ColorSpaceMath.System sys) {
		this.highlightRgb = rgb;
		if (rgb >= 0) {
			highlight3D = ColorSpaceMath.componentsTo3D(ColorSpaceMath.rgbToComponents(rgb, sys), sys);
		} else {
			highlight3D = null;
		}
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int cx = getWidth() / 2;
		int cy = getHeight() / 2;
		float scale = Math.min(getWidth(), getHeight()) * 0.35f * zoom;

		g2.setColor(Color.LIGHT_GRAY);
		g2.setFont(g2.getFont().deriveFont(11f));
		g2.drawString(system.name() + " 3D — drag: rotate, wheel: zoom, click: pick", 8, 16);

		drawWireframe(g2, cx, cy, scale);

		if (cloudPoints != null) {
			g2.setColor(new Color(100, 180, 255, 90));
			for (int i = 0; i + 2 < cloudPoints.length; i += 3) {
				float[] p = project(cloudPoints[i], cloudPoints[i + 1], cloudPoints[i + 2], cx, cy, scale);
				g2.fill(new Ellipse2D.Float(p[0] - 1.5f, p[1] - 1.5f, 3, 3));
			}
		}

		if (highlight3D != null) {
			float[] p = project(highlight3D[0], highlight3D[1], highlight3D[2], cx, cy, scale);
			g2.setColor(new Color(highlightRgb));
			g2.fill(new Ellipse2D.Float(p[0] - 6, p[1] - 6, 12, 12));
			g2.setColor(Color.WHITE);
			g2.setStroke(new BasicStroke(2f));
			g2.draw(new Ellipse2D.Float(p[0] - 6, p[1] - 6, 12, 12));
		}

		drawAxisLabels(g2, cx, cy, scale);
	}

	private void drawWireframe(Graphics2D g2, int cx, int cy, float scale) {
		if (system == ColorSpaceMath.System.RGB || system == ColorSpaceMath.System.CMYK) {
			float[][] v = new float[8][2];
			for (int i = 0; i < 8; i++) {
				v[i] = project(CUBE[i * 3], CUBE[i * 3 + 1], CUBE[i * 3 + 2], cx, cy, scale);
			}
			g2.setStroke(new BasicStroke(1.2f));
			for (int[] e : EDGES) {
				if (system == ColorSpaceMath.System.RGB) {
					g2.setColor(new Color(CUBE[e[0] * 3] > 0.5f ? 255 : 80, CUBE[e[0] * 3 + 1] > 0.5f ? 255 : 80,
							CUBE[e[0] * 3 + 2] > 0.5f ? 255 : 80, 120));
				} else {
					g2.setColor(new Color(180, 180, 200, 120));
				}
				g2.draw(new Line2D.Float(v[e[0]][0], v[e[0]][1], v[e[1]][0], v[e[1]][1]));
			}
		} else if (system == ColorSpaceMath.System.HSV) {
			drawHsvCone(g2, cx, cy, scale);
		} else {
			drawAxisBox(g2, cx, cy, scale);
		}
	}

	private void drawHsvCone(Graphics2D g2, int cx, int cy, float scale) {
		g2.setColor(new Color(200, 200, 220, 150));
		int segments = 36;
		float[] prev = null;
		for (int i = 0; i <= segments; i++) {
			float h = i / (float) segments;
			float[] bottom = project((float) Math.cos(h * 2 * Math.PI), (float) Math.sin(h * 2 * Math.PI), 0, cx, cy,
					scale);
			float[] top = project(0, 0, 1, cx, cy, scale);
			if (prev != null) {
				g2.draw(new Line2D.Float(prev[0], prev[1], bottom[0], bottom[1]));
			}
			g2.draw(new Line2D.Float(bottom[0], bottom[1], top[0], top[1]));
			prev = bottom;
		}
	}

	private void drawAxisBox(Graphics2D g2, int cx, int cy, float scale) {
		g2.setColor(new Color(120, 120, 140, 180));
		float[] o = project(0, 0, 0, cx, cy, scale);
		float[] x = project(1, 0, 0, cx, cy, scale);
		float[] y = project(0, 1, 0, cx, cy, scale);
		float[] z = project(0, 0, 1, cx, cy, scale);
		g2.draw(new Line2D.Float(o[0], o[1], x[0], x[1]));
		g2.draw(new Line2D.Float(o[0], o[1], y[0], y[1]));
		g2.draw(new Line2D.Float(o[0], o[1], z[0], z[1]));
	}

	private void drawAxisLabels(Graphics2D g2, int cx, int cy, float scale) {
		String[] labels = axisLabelsFor(system);
		float[][] ends = { project(1.05f, 0, 0, cx, cy, scale), project(0, 1.05f, 0, cx, cy, scale),
				project(0, 0, 1.05f, cx, cy, scale) };
		g2.setColor(Color.WHITE);
		for (int i = 0; i < 3 && i < labels.length; i++) {
			g2.drawString(labels[i], ends[i][0], ends[i][1]);
		}
	}

	private static String[] axisLabelsFor(ColorSpaceMath.System s) {
		switch (s) {
		case RGB:
			return new String[] { "R", "G", "B" };
		case CMYK:
			return new String[] { "C", "M", "Y" };
		case HSV:
			return new String[] { "S·cosH", "S·sinH", "V" };
		case YUV:
			return new String[] { "U", "V", "Y" };
		case YCbCr:
			return new String[] { "Cb", "Cr", "Y" };
		case LAB:
			return new String[] { "a", "b", "L" };
		default:
			return new String[] { "X", "Y", "Z" };
		}
	}

	private float[] project(float x, float y, float z, int cx, int cy, float scale) {
		float x1 = x - 0.5f;
		float y1 = y - 0.5f;
		float z1 = z - 0.5f;
		float cosY = (float) Math.cos(rotY);
		float sinY = (float) Math.sin(rotY);
		float cosX = (float) Math.cos(rotX);
		float sinX = (float) Math.sin(rotX);
		float xr = x1 * cosY - z1 * sinY;
		float zr = x1 * sinY + z1 * cosY;
		float yr = y1 * cosX - zr * sinX;
		float zf = y1 * sinX + zr * cosX;
		float persp = 1f / (2.5f + zf);
		return new float[] { cx + xr * scale * persp, cy - yr * scale * persp };
	}

	private void pickAt(int mx, int my) {
		int cx = getWidth() / 2;
		int cy = getHeight() / 2;
		float scale = Math.min(getWidth(), getHeight()) * 0.35f * zoom;
		float best = Float.MAX_VALUE;
		int bestRgb = -1;

		if (cloudPoints != null && cloudRgb != null) {
			int count = cloudRgb.length;
			for (int i = 0; i < count; i++) {
				int base = i * 3;
				if (base + 2 >= cloudPoints.length) {
					break;
				}
				float[] p = project(cloudPoints[base], cloudPoints[base + 1], cloudPoints[base + 2], cx, cy,
						scale);
				float d = (p[0] - mx) * (p[0] - mx) + (p[1] - my) * (p[1] - my);
				if (d < best) {
					best = d;
					bestRgb = cloudRgb[i];
				}
			}
		}

		if (bestRgb < 0 && system == ColorSpaceMath.System.RGB) {
			float nx = (mx - cx) / scale + 0.5f;
			float ny = 0.5f - (my - cy) / scale;
			bestRgb = ColorSpaceMath.componentsToRgb(
					new float[] { clamp01(nx), clamp01(ny), 0.5f }, ColorSpaceMath.System.RGB);
		}

		if (bestRgb >= 0 && pickListener != null) {
			pickListener.onColorPicked(bestRgb);
		}
	}

	private static float clamp01(float v) {
		return Math.max(0f, Math.min(1f, v));
	}
}
