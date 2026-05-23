import java.awt.image.BufferedImage;

/**
 * Per-pixel decomposition of an image into color-space channels with adjustable
 * scale, offset, and enable flags (requirements 2 & 3).
 */
public class ChannelBuffer {

	private final ColorSpaceMath.System system;
	private final int width;
	private final int height;
	private final float[][] planes;
	private final float[] scale;
	private final float[] offset;
	private final boolean[] enabled;

	public ChannelBuffer(ColorSpaceMath.System system, int width, int height) {
		this.system = system;
		this.width = width;
		this.height = height;
		int n = ColorSpaceMath.channelCount(system);
		planes = new float[n][width * height];
		scale = new float[n];
		offset = new float[n];
		enabled = new boolean[n];
		for (int i = 0; i < n; i++) {
			scale[i] = 1f;
			offset[i] = 0f;
			enabled[i] = true;
		}
	}

	public static ChannelBuffer fromImage(BufferedImage source, ColorSpaceMath.System system) {
		int w = source.getWidth();
		int h = source.getHeight();
		ChannelBuffer buf = new ChannelBuffer(system, w, h);
		int n = ColorSpaceMath.channelCount(system);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int idx = y * w + x;
				float[] c = ColorSpaceMath.rgbToComponents(source.getRGB(x, y), system);
				for (int ch = 0; ch < n; ch++) {
					buf.planes[ch][idx] = c[ch];
				}
			}
		}
		return buf;
	}

	public BufferedImage toImage() {
		BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int n = ColorSpaceMath.channelCount(system);
		float[] c = new float[n];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int idx = y * width + x;
				for (int ch = 0; ch < n; ch++) {
					if (enabled[ch]) {
						c[ch] = clamp01(planes[ch][idx] * scale[ch] + offset[ch]);
					} else {
						c[ch] = ColorSpaceMath.neutralWhenDisabled(system, ch);
					}
				}
				out.setRGB(x, y, ColorSpaceMath.componentsToRgb(c, system));
			}
		}
		return out;
	}

	public ColorSpaceMath.System getSystem() {
		return system;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setScale(int channel, float value) {
		scale[channel] = value;
	}

	public void setOffset(int channel, float value) {
		offset[channel] = value;
	}

	public void setEnabled(int channel, boolean on) {
		enabled[channel] = on;
	}

	public void resetAdjustments() {
		int n = ColorSpaceMath.channelCount(system);
		for (int i = 0; i < n; i++) {
			scale[i] = 1f;
			offset[i] = 0f;
			enabled[i] = true;
		}
	}

	/** Grayscale preview of one channel (0–255). */
	public BufferedImage getChannelPreview(int channel) {
		BufferedImage gray = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int idx = y * width + x;
				int v = Math.max(0, Math.min(255, Math.round(planes[channel][idx] * 255f)));
				int rgb = (v << 16) | (v << 8) | v;
				gray.setRGB(x, y, rgb);
			}
		}
		return gray;
	}

	public float[] getComponentsAt(int x, int y) {
		int n = ColorSpaceMath.channelCount(system);
		float[] c = new float[n];
		int idx = y * width + x;
		for (int ch = 0; ch < n; ch++) {
			if (enabled[ch]) {
				c[ch] = clamp01(planes[ch][idx] * scale[ch] + offset[ch]);
			} else {
				c[ch] = ColorSpaceMath.neutralWhenDisabled(system, ch);
			}
		}
		return c;
	}

	public static final class VisualizationSample {
		public final float[] positions3d;
		public final int[] rgbColors;

		public VisualizationSample(float[] positions3d, int[] rgbColors) {
			this.positions3d = positions3d;
			this.rgbColors = rgbColors;
		}
	}

	public VisualizationSample sampleForVisualization(int maxPoints) {
		int step = Math.max(1, (width * height) / Math.max(1, maxPoints));
		int n = ColorSpaceMath.channelCount(system);
		java.util.ArrayList<Float> pts = new java.util.ArrayList<>();
		java.util.ArrayList<Integer> rgbs = new java.util.ArrayList<>();
		for (int idx = 0; idx < width * height; idx += step) {
			float[] c = new float[n];
			for (int ch = 0; ch < n; ch++) {
				c[ch] = planes[ch][idx];
			}
			float[] p = ColorSpaceMath.componentsTo3D(c, system);
			pts.add(p[0]);
			pts.add(p[1]);
			pts.add(p[2]);
			rgbs.add(ColorSpaceMath.componentsToRgb(c, system));
		}
		float[] arr = new float[pts.size()];
		for (int i = 0; i < pts.size(); i++) {
			arr[i] = pts.get(i);
		}
		int[] colors = new int[rgbs.size()];
		for (int i = 0; i < rgbs.size(); i++) {
			colors[i] = rgbs.get(i);
		}
		return new VisualizationSample(arr, colors);
	}

	private static float clamp01(float v) {
		return Math.max(0f, Math.min(1f, v));
	}
}
