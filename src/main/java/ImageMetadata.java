import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Collects image file and pixel metadata (requirement 8).
 */
public class ImageMetadata {

	private final String fileName;
	private final String filePath;
	private final String format;
	private final String mimeType;
	private final long fileSizeBytes;
	private final int width;
	private final int height;
	private final long totalPixels;
	private final String aspectRatio;
	private final int imageType;
	private final String imageTypeLabel;
	private final boolean hasAlpha;
	private final int bitsPerPixel;
	private final String colorModel;
	private final String activeColorSystem;
	private final String saveFormat;

	public ImageMetadata(File file, BufferedImage image, String format, String activeColorSystem, String saveFormat,
			long fileSizeBytes) throws IOException {
		this.fileName = file.getName();
		this.filePath = file.getAbsolutePath();
		this.format = format != null ? format.toUpperCase() : "—";
		this.mimeType = probeMime(file);
		this.fileSizeBytes = fileSizeBytes > 0 ? fileSizeBytes : (file.exists() ? file.length() : 0);
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.totalPixels = (long) width * height;
		this.aspectRatio = formatAspectRatio(width, height);
		this.imageType = image.getType();
		this.imageTypeLabel = describeImageType(image.getType());
		this.hasAlpha = image.getColorModel().hasAlpha();
		this.bitsPerPixel = image.getColorModel().getPixelSize();
		this.colorModel = image.getColorModel().getClass().getSimpleName();
		this.activeColorSystem = activeColorSystem;
		this.saveFormat = saveFormat != null ? saveFormat.toUpperCase() : "—";
	}

	private static String probeMime(File file) {
		try {
			String mime = Files.probeContentType(file.toPath());
			return mime != null ? mime : "unknown";
		} catch (IOException e) {
			return "unknown";
		}
	}

	private static String formatAspectRatio(int w, int h) {
		if (h == 0) {
			return "—";
		}
		int g = gcd(w, h);
		return (w / g) + ":" + (h / g) + String.format(" (%.2f)", w / (double) h);
	}

	private static int gcd(int a, int b) {
		return b == 0 ? a : gcd(b, a % b);
	}

	private static String describeImageType(int type) {
		switch (type) {
		case BufferedImage.TYPE_INT_RGB:
			return "INT_RGB";
		case BufferedImage.TYPE_INT_ARGB:
			return "INT_ARGB";
		case BufferedImage.TYPE_INT_ARGB_PRE:
			return "INT_ARGB_PRE";
		case BufferedImage.TYPE_3BYTE_BGR:
			return "3BYTE_BGR";
		case BufferedImage.TYPE_4BYTE_ABGR:
			return "4BYTE_ABGR";
		case BufferedImage.TYPE_BYTE_GRAY:
			return "BYTE_GRAY";
		case BufferedImage.TYPE_USHORT_GRAY:
			return "USHORT_GRAY";
		default:
			return "TYPE_" + type;
		}
	}

	public String toHtml() {
		return "<html>"
				+ row("Name", fileName)
				+ row("Path", filePath)
				+ row("Format", format)
				+ row("MIME", mimeType)
				+ row("File size", formatFileSize(fileSizeBytes))
				+ row("Dimensions", width + " × " + height + " px")
				+ row("Total pixels", String.format("%,d", totalPixels))
				+ row("Megapixels", String.format("%.2f MP", totalPixels / 1_000_000.0))
				+ row("Aspect ratio", aspectRatio)
				+ row("Bit depth", bitsPerPixel + " bpp")
				+ row("Alpha channel", hasAlpha ? "Yes" : "No")
				+ row("Color model", colorModel)
				+ row("BufferedImage type", imageTypeLabel)
				+ row("Active color system", activeColorSystem)
				+ row("Save format (dropdown)", saveFormat)
				+ "</html>";
	}

	private static String row(String label, String value) {
		return "<b>" + label + ":</b> " + escape(value) + "<br>";
	}

	private static String escape(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;");
	}

	public static String formatFileSize(long bytes) {
		if (bytes < 1024) {
			return bytes + " B";
		}
		if (bytes < 1024 * 1024) {
			return String.format("%.2f KB (%d bytes)", bytes / 1024.0, bytes);
		}
		return String.format("%.2f MB (%d bytes)", bytes / (1024.0 * 1024.0), bytes);
	}
}
