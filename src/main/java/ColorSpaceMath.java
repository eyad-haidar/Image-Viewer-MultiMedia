/**
 * Color-space conversions for PixelLab (RGB, CMYK, HSV, YUV, LAB, YCbCr).
 */
public final class ColorSpaceMath {

	public enum System {
		RGB, CMYK, HSV, YUV, LAB, YCbCr
	}

	private ColorSpaceMath() {
	}

	public static int channelCount(System system) {
		return system == System.CMYK ? 4 : 3;
	}

	public static String[] channelNames(System system) {
		switch (system) {
		case RGB:
			return new String[] { "R", "G", "B" };
		case CMYK:
			return new String[] { "C", "M", "Y", "K" };
		case HSV:
			return new String[] { "H", "S", "V" };
		case YUV:
			return new String[] { "Y", "U", "V" };
		case LAB:
			return new String[] { "L", "a", "b" };
		case YCbCr:
			return new String[] { "Y", "Cb", "Cr" };
		default:
			return new String[] { "?", "?", "?" };
		}
	}

	/** Neutral component value when a channel is disabled (before compose). */
	public static float neutralWhenDisabled(System system, int channel) {
		switch (system) {
		case RGB:
			return 0f;
		case CMYK:
			return channel == 3 ? 1f : 0f;
		case HSV:
			return channel == 0 ? 0f : (channel == 1 ? 0f : 1f);
		case YUV:
			return channel == 0 ? 0.5f : 0.5f;
		case LAB:
			return channel == 0 ? 0.5f : 0.5f;
		case YCbCr:
			return channel == 0 ? 0.5f : 0.5f;
		default:
			return 0f;
		}
	}

	public static float[] rgbToComponents(int rgb, System system) {
		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = rgb & 0xFF;
		switch (system) {
		case RGB:
			return new float[] { r / 255f, g / 255f, b / 255f };
		case CMYK:
			return rgbToCmyk(r, g, b);
		case HSV:
			return rgbToHsv(r, g, b);
		case YUV:
			return rgbToYuv(r, g, b);
		case LAB:
			return rgbToLab(r, g, b);
		case YCbCr:
			return rgbToYCbCr(r, g, b);
		default:
			return new float[] { r / 255f, g / 255f, b / 255f };
		}
	}

	public static int componentsToRgb(float[] c, System system) {
		switch (system) {
		case RGB:
			return packRgb(clamp255(c[0] * 255f), clamp255(c[1] * 255f), clamp255(c[2] * 255f));
		case CMYK:
			return cmykToRgb(c[0], c[1], c[2], c[3]);
		case HSV:
			return hsvToRgb(c[0], c[1], c[2]);
		case YUV:
			return yuvToRgb(c[0], c[1], c[2]);
		case LAB:
			return labToRgb(c[0], c[1], c[2]);
		case YCbCr:
			return yCbCrToRgb(c[0], c[1], c[2]);
		default:
			return packRgb(0, 0, 0);
		}
	}

	public static int rgbToArgb(int rgb) {
		return 0xFF000000 | (rgb & 0xFFFFFF);
	}

	public static String formatAllSystems(int rgb) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		for (System s : System.values()) {
			float[] c = rgbToComponents(rgb, s);
			sb.append("<b>").append(s.name()).append("</b> → ").append(formatComponents(c, s)).append("<br>");
		}
		sb.append("</html>");
		return sb.toString();
	}

	public static String formatComponents(float[] c, System system) {
		switch (system) {
		case RGB:
			return String.format("(%d, %d, %d)", clamp255(c[0] * 255f), clamp255(c[1] * 255f), clamp255(c[2] * 255f));
		case CMYK:
			return String.format("(%.0f%%, %.0f%%, %.0f%%, %.0f%%)", c[0] * 100, c[1] * 100, c[2] * 100, c[3] * 100);
		case HSV:
			return String.format("(%.0f°, %.0f%%, %.0f%%)", c[0] * 360f, c[1] * 100f, c[2] * 100f);
		case YUV:
			return String.format("(%.0f, %.0f, %.0f)", c[0] * 255f, c[1] * 255f, c[2] * 255f);
		case LAB:
			return String.format("(%.1f, %.1f, %.1f)", c[0] * 100f, (c[1] - 0.5f) * 256f, (c[2] - 0.5f) * 256f);
		case YCbCr:
			return String.format("(%.0f, %.0f, %.0f)", c[0] * 255f, c[1] * 255f, c[2] * 255f);
		default:
			return "()";
		}
	}

	/** Map normalized components to 3D coordinates for visualization. */
	public static float[] componentsTo3D(float[] c, System system) {
		switch (system) {
		case RGB:
			return new float[] { c[0], c[1], c[2] };
		case CMYK:
			return new float[] { c[0], c[1], c[2] };
		case HSV: {
			double rad = c[0] * 2 * Math.PI;
			float s = c[1];
			return new float[] { (float) (s * Math.cos(rad)), (float) (s * Math.sin(rad)), c[2] };
		}
		case YUV:
		case YCbCr:
			return new float[] { c[1], c[2], c[0] };
		case LAB:
			return new float[] { c[1], c[2], c[0] };
		default:
			return new float[] { 0.5f, 0.5f, 0.5f };
		}
	}

	private static float[] rgbToCmyk(int r, int g, int b) {
		float rf = r / 255f, gf = g / 255f, bf = b / 255f;
		float k = 1f - Math.max(rf, Math.max(gf, bf));
		float c, m, y;
		if (k >= 1f - 1e-6f) {
			return new float[] { 0f, 0f, 0f, 1f };
		}
		c = (1f - rf - k) / (1f - k);
		m = (1f - gf - k) / (1f - k);
		y = (1f - bf - k) / (1f - k);
		return new float[] { c, m, y, k };
	}

	private static int cmykToRgb(float c, float m, float y, float k) {
		float r = (1f - c) * (1f - k);
		float g = (1f - m) * (1f - k);
		float b = (1f - y) * (1f - k);
		return packRgb(clamp255(r * 255f), clamp255(g * 255f), clamp255(b * 255f));
	}

	private static float[] rgbToHsv(int r, int g, int b) {
		float rf = r / 255f, gf = g / 255f, bf = b / 255f;
		float max = Math.max(rf, Math.max(gf, bf));
		float min = Math.min(rf, Math.min(gf, bf));
		float delta = max - min;
		float h = 0f;
		float s = max == 0f ? 0f : delta / max;
		if (delta > 1e-6f) {
			if (max == rf) {
				h = (gf - bf) / delta;
			} else if (max == gf) {
				h = 2f + (bf - rf) / delta;
			} else {
				h = 4f + (rf - gf) / delta;
			}
			h = (h / 6f + 1f) % 1f;
		}
		return new float[] { h, s, max };
	}

	private static int hsvToRgb(float h, float s, float v) {
		if (s <= 0f) {
			int gray = clamp255(v * 255f);
			return packRgb(gray, gray, gray);
		}
		float hh = h * 6f;
		int i = (int) Math.floor(hh);
		float f = hh - i;
		float p = v * (1f - s);
		float q = v * (1f - s * f);
		float t = v * (1f - s * (1f - f));
		float r, g, b;
		switch (i % 6) {
		case 0:
			r = v;
			g = t;
			b = p;
			break;
		case 1:
			r = q;
			g = v;
			b = p;
			break;
		case 2:
			r = p;
			g = v;
			b = t;
			break;
		case 3:
			r = p;
			g = q;
			b = v;
			break;
		case 4:
			r = t;
			g = p;
			b = v;
			break;
		default:
			r = v;
			g = p;
			b = q;
			break;
		}
		return packRgb(clamp255(r * 255f), clamp255(g * 255f), clamp255(b * 255f));
	}

	private static float[] rgbToYuv(int r, int g, int b) {
		float y = (0.299f * r + 0.587f * g + 0.114f * b) / 255f;
		float u = (-0.147f * r - 0.289f * g + 0.436f * b + 128f) / 255f;
		float v = (0.615f * r - 0.515f * g - 0.100f * b + 128f) / 255f;
		return new float[] { y, u, v };
	}

	private static int yuvToRgb(float y, float u, float v) {
		float uf = u * 255f - 128f;
		float vf = v * 255f - 128f;
		float yf = y * 255f;
		return packRgb(clamp255(yf + 1.140f * vf), clamp255(yf - 0.395f * uf - 0.581f * vf),
				clamp255(yf + 2.032f * uf));
	}

	private static float[] rgbToYCbCr(int r, int g, int b) {
		float y = (0.299f * r + 0.587f * g + 0.114f * b) / 255f;
		float cb = (-0.168736f * r - 0.331264f * g + 0.5f * b + 128f) / 255f;
		float cr = (0.5f * r - 0.418688f * g - 0.081312f * b + 128f) / 255f;
		return new float[] { y, cb, cr };
	}

	private static int yCbCrToRgb(float y, float cb, float cr) {
		float yf = y * 255f;
		float cbf = cb * 255f - 128f;
		float crf = cr * 255f - 128f;
		return packRgb(clamp255(yf + 1.402f * crf), clamp255(yf - 0.344136f * cbf - 0.714136f * crf),
				clamp255(yf + 1.772f * cbf));
	}

	private static float[] rgbToLab(int r, int g, int b) {
		float lr = srgbToLinear(r / 255f);
		float lg = srgbToLinear(g / 255f);
		float lb = srgbToLinear(b / 255f);
		float x = lr * 0.4124564f + lg * 0.3575761f + lb * 0.1804375f;
		float y = lr * 0.2126729f + lg * 0.7151522f + lb * 0.0721750f;
		float z = lr * 0.0193339f + lg * 0.1191920f + lb * 0.9503041f;
		x /= 0.95047f;
		z /= 1.08883f;
		float l = (116f * labF(y) - 16f) / 100f;
		float a = (500f * (labF(x) - labF(y)) + 128f) / 255f;
		float bStar = (200f * (labF(y) - labF(z)) + 128f) / 255f;
		return new float[] { l, a, bStar };
	}

	private static int labToRgb(float l, float a, float bStar) {
		float L = l * 100f;
		float A = a * 255f - 128f;
		float B = bStar * 255f - 128f;
		float fy = (L + 16f) / 116f;
		float fx = fy + A / 500f;
		float fz = fy - B / 200f;
		float x = 0.95047f * labFInv(fx);
		float y = labFInv(fy);
		float z = 1.08883f * labFInv(fz);
		float r = x * 3.2404542f + y * -1.5371385f + z * -0.4985314f;
		float g = x * -0.9692660f + y * 1.8760108f + z * 0.0415560f;
		float b = x * 0.0556434f + y * -0.2040259f + z * 1.0572252f;
		return packRgb(clamp255(linearToSrgb(r) * 255f), clamp255(linearToSrgb(g) * 255f),
				clamp255(linearToSrgb(b) * 255f));
	}

	private static float srgbToLinear(float c) {
		return c <= 0.04045f ? c / 12.92f : (float) Math.pow((c + 0.055f) / 1.055f, 2.4);
	}

	private static float linearToSrgb(float c) {
		c = Math.max(0f, Math.min(1f, c));
		return c <= 0.0031308f ? 12.92f * c : (float) (1.055 * Math.pow(c, 1 / 2.4) - 0.055);
	}

	private static float labF(float t) {
		return t > 0.008856f ? (float) Math.cbrt(t) : (7.787f * t + 16f / 116f);
	}

	private static float labFInv(float t) {
		float t3 = t * t * t;
		return t3 > 0.008856f ? t3 : (t - 16f / 116f) / 7.787f;
	}

	private static int packRgb(int r, int g, int b) {
		return (r << 16) | (g << 8) | b;
	}

	private static int clamp255(float v) {
		return Math.max(0, Math.min(255, Math.round(v)));
	}
}
