import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainFrame extends JFrame {

	public static final Color BG_COLOR = new Color(40, 44, 52);
	public static final Color LIGHT_BLUE = Color.decode("#71A0FF");

	private final JFileChooser fileChooser = new JFileChooser();
	private JButton selectImageButton;
	private JButton deselectImageButton;
	private JButton resetImageButton;
	private JButton saveImageButton;
	private JPanel imageWorkingSpacePanel;
	private JPanel imageToolsPanel;
	private JLabel imageLabel;
	private JLabel imageInfoLabel;
	private JLabel colorValuesLabel;
	private BufferedImage image;
	private BufferedImage originalImage;
	private BufferedImage pristineOriginal;
	private ChannelBuffer channelBuffer;
	private File imageFile;
	private String imageName;
	private String imageExtension;
	private int imageWidth;
	private int imageHeight;
	private long imageBytes;
	private final String[] colorSystems = { "RGB", "CMYK", "HSV", "YUV", "LAB", "YCbCr" };
	private final String[] imageFormats = { "PNG", "JPG", "BMP" };
	private JComboBox<String> convertToColorSystem;
	private JComboBox<String> convertToImageFormat;
	private ComponentControlPanel componentPanel;
	private ChannelPlanesPanel channelPlanesPanel;
	private ColorSpace3DPanel colorSpace3DPanel;
	private ColorSpace2DPanel colorSpace2DPanel;
	private JSlider slice2DSlider;
	private boolean suppressColorComboEvents;
	private boolean suppressFormatComboEvents;
	private ColorSpaceMath.System activeSystem = ColorSpaceMath.System.RGB;

	public MainFrame() {
		SwingUtilities.invokeLater(() -> {
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setTitle("PixelLab — Multimedia Lab");
			setSize(1400, 1000);
			setResizable(true);
			setLocationRelativeTo(null);
			setLayout(new BorderLayout());
			getContentPane().setBackground(BG_COLOR);
			setVisible(true);
		});
		initializePanels();
		add(imageWorkingSpacePanel, BorderLayout.CENTER);
		add(imageToolsPanel, BorderLayout.EAST);
	}

	private void initializePanels() {
		imageWorkingSpacePanel = new JPanel(new BorderLayout());
		imageWorkingSpacePanel.setBackground(Color.BLACK);

		imageToolsPanel = new JPanel();
		imageToolsPanel.setBackground(BG_COLOR);
		imageToolsPanel.setLayout(new BoxLayout(imageToolsPanel, BoxLayout.Y_AXIS));
		imageToolsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		imageToolsPanel.setPreferredSize(new Dimension(280, 0));

		initializeButtons();
		componentPanel = new ComponentControlPanel(this::applyChannelAdjustments);
		channelPlanesPanel = new ChannelPlanesPanel();

		imageToolsPanel.add(selectImageButton);
		imageToolsPanel.add(deselectImageButton);
		imageToolsPanel.add(resetImageButton);
		imageToolsPanel.add(Box.createVerticalStrut(6));
		JLabel colorSysLbl = new JLabel("Color system:");
		colorSysLbl.setForeground(Color.WHITE);
		colorSysLbl.setAlignmentX(JButton.LEFT_ALIGNMENT);
		imageToolsPanel.add(colorSysLbl);
		imageToolsPanel.add(convertToColorSystem);
		imageToolsPanel.add(componentPanel);
		imageToolsPanel.add(channelPlanesPanel);
		imageToolsPanel.add(Box.createVerticalStrut(6));
		JLabel formatLbl = new JLabel("Image format:");
		formatLbl.setForeground(Color.WHITE);
		formatLbl.setAlignmentX(JButton.LEFT_ALIGNMENT);
		imageToolsPanel.add(formatLbl);
		imageToolsPanel.add(convertToImageFormat);
		imageToolsPanel.add(saveImageButton);

		initializeImageSpace();
		initializeColorSpaceViews();
	}

	private void initializeButtons() {
		selectImageButton = styledButton("Select image");
		deselectImageButton = styledButton("Deselect image");
		resetImageButton = styledButton("Reset image");
		saveImageButton = styledButton("Save image");

		selectImageButton.addActionListener(e -> selectImage());
		deselectImageButton.addActionListener(e -> deselectImage());
		resetImageButton.addActionListener(e -> resetToOriginal());
		saveImageButton.addActionListener(e -> saveImage());

		convertToColorSystem = new JComboBox<>(colorSystems);
		convertToColorSystem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		convertToColorSystem.addActionListener(e -> {
			if (suppressColorComboEvents || originalImage == null) {
				return;
			}
			activeSystem = parseSystem((String) convertToColorSystem.getSelectedItem());
			loadColorSystem(activeSystem);
		});

		convertToImageFormat = new JComboBox<>(imageFormats);
		convertToImageFormat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		convertToImageFormat.addActionListener(e -> {
			if (suppressFormatComboEvents || image == null || imageExtension == null) {
				return;
			}
			String sel = (String) convertToImageFormat.getSelectedItem();
			if ("PNG".equalsIgnoreCase(sel)) {
				convertImageFormat("png");
			} else if ("JPG".equalsIgnoreCase(sel)) {
				convertImageFormat("jpg");
			}
		});
	}

	private JButton styledButton(String text) {
		JButton b = new JButton(text);
		b.setMaximumSize(new Dimension(Integer.MAX_VALUE, b.getPreferredSize().height));
		b.setAlignmentX(LEFT_ALIGNMENT);
		return b;
	}

	private void initializeImageSpace() {
		imageFile = new File("./src/main/resources/image-icon.png");
		try {
			image = ImageIO.read(imageFile);
		} catch (IOException exp) {
			exp.printStackTrace();
		}

		imageLabel = new JLabel(new ImageIcon(image));
		imageLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
		imageLabel.setHorizontalAlignment(JLabel.CENTER);
		imageLabel.setVerticalAlignment(JLabel.CENTER);
		installImagePickListener();
		installDragAndDrop();

		imageInfoLabel = new JLabel("No image selected — use Select or drag & drop.");
		styleInfoLabel(imageInfoLabel);

		colorValuesLabel = new JLabel(" ");
		styleInfoLabel(colorValuesLabel);
		colorValuesLabel.setVerticalAlignment(JLabel.TOP);

		JPanel south = new JPanel(new BorderLayout());
		south.setBackground(Color.BLACK);
		south.add(imageInfoLabel, BorderLayout.NORTH);
		south.add(colorValuesLabel, BorderLayout.CENTER);

		imageWorkingSpacePanel.add(new JScrollPane(imageLabel), BorderLayout.CENTER);
		imageWorkingSpacePanel.add(south, BorderLayout.SOUTH);
	}

	private void initializeColorSpaceViews() {
		colorSpace3DPanel = new ColorSpace3DPanel();
		colorSpace2DPanel = new ColorSpace2DPanel();
		slice2DSlider = new JSlider(0, 100, 50);
		slice2DSlider.setForeground(Color.WHITE);
		slice2DSlider.addChangeListener(e -> {
			colorSpace2DPanel.setSliceValue(slice2DSlider.getValue() / 100f);
		});

		ColorSpace3DPanel.PickListener pick3d = this::onColorPicked;
		ColorSpace2DPanel.PickListener pick2d = this::onColorPicked;
		colorSpace3DPanel.setPickListener(pick3d);
		colorSpace2DPanel.setPickListener(pick2d);

		JPanel sliceRow = new JPanel(new BorderLayout());
		sliceRow.setBackground(BG_COLOR);
		JLabel sliceLbl = new JLabel("2D slice (3rd axis):");
		sliceLbl.setForeground(Color.WHITE);
		sliceRow.add(sliceLbl, BorderLayout.WEST);
		sliceRow.add(slice2DSlider, BorderLayout.CENTER);

		JPanel tab2d = new JPanel(new BorderLayout());
		tab2d.setBackground(BG_COLOR);
		tab2d.add(sliceRow, BorderLayout.NORTH);
		tab2d.add(colorSpace2DPanel, BorderLayout.CENTER);

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("3D color space", colorSpace3DPanel);
		tabs.addTab("2D projection", tab2d);

		imageWorkingSpacePanel.add(tabs, BorderLayout.SOUTH);
	}

	private void styleInfoLabel(JLabel label) {
		label.setOpaque(false);
		label.setForeground(Color.WHITE);
		label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		label.setHorizontalAlignment(JLabel.LEFT);
	}

	private void installImagePickListener() {
		imageLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (image == null || originalImage == null) {
					return;
				}
				int imgW = image.getWidth();
				int imgH = image.getHeight();
				int lblW = imageLabel.getWidth();
				int lblH = imageLabel.getHeight();
				if (lblW <= 0 || lblH <= 0) {
					return;
				}
				double scale = Math.min(lblW / (double) imgW, lblH / (double) imgH);
				int drawW = (int) (imgW * scale);
				int drawH = (int) (imgH * scale);
				int offX = (lblW - drawW) / 2;
				int offY = (lblH - drawH) / 2;
				int px = (int) ((e.getX() - offX) / scale);
				int py = (int) ((e.getY() - offY) / scale);
				if (px >= 0 && py >= 0 && px < imgW && py < imgH) {
					onColorPicked(image.getRGB(px, py) & 0xFFFFFF);
				}
			}
		});
	}

	private void installDragAndDrop() {
		new DropTarget(imageLabel, new DropTargetAdapter() {
			@Override
			public void drop(DropTargetDropEvent dtde) {
				try {
					dtde.acceptDrop(DnDConstants.ACTION_COPY);
					Transferable t = dtde.getTransferable();
					if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
						@SuppressWarnings("unchecked")
						List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
						if (!files.isEmpty()) {
							loadImageFile(files.get(0));
						}
					}
					dtde.dropComplete(true);
				} catch (Exception ex) {
					dtde.dropComplete(false);
				}
			}
		});
	}

	private void onColorPicked(int rgb) {
		rgb &= 0xFFFFFF;
		colorValuesLabel.setText(ColorSpaceMath.formatAllSystems(rgb));
		colorSpace3DPanel.setHighlight(rgb, activeSystem);
	}

	private void applyChannelAdjustments() {
		if (channelBuffer == null) {
			return;
		}
		image = channelBuffer.toImage();
		channelPlanesPanel.updateFrom(channelBuffer);
		refreshDisplay();
		updateVisualizationCloud();
	}

	private void loadColorSystem(ColorSpaceMath.System system) {
		activeSystem = system;
		channelBuffer = ChannelBuffer.fromImage(originalImage, system);
		componentPanel.bind(channelBuffer);
		image = channelBuffer.toImage();
		channelPlanesPanel.updateFrom(channelBuffer);
		colorSpace3DPanel.setSystem(system);
		colorSpace2DPanel.setSystem(system);
		refreshDisplay();
		updateVisualizationCloud();
	}

	private void updateVisualizationCloud() {
		if (channelBuffer == null) {
			return;
		}
		ChannelBuffer.VisualizationSample sample = channelBuffer.sampleForVisualization(2500);
		colorSpace3DPanel.setPointCloud(sample.positions3d, sample.rgbColors);
	}

	private ColorSpaceMath.System parseSystem(String name) {
		return ColorSpaceMath.System.valueOf(name);
	}

	private void selectImage() {
		fileChooser.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg", "bmp"));
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setCurrentDirectory(new File("./src/main/resources"));
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			loadImageFile(fileChooser.getSelectedFile());
		}
	}

	private void loadImageFile(File file) {
		try {
			imageFile = file;
			image = ImageIO.read(file);
			pristineOriginal = copyImage(image);
			originalImage = copyImage(image);
			imageName = file.getName();
			imageBytes = file.length();
			imageExtension = probeExtension(file);
			suppressFormatComboEvents = true;
			suppressColorComboEvents = true;
			syncFormatComboBox();
			convertToColorSystem.setSelectedIndex(0);
			suppressFormatComboEvents = false;
			suppressColorComboEvents = false;
			loadColorSystem(ColorSpaceMath.System.RGB);
			componentPanel.resetSlidersUi();
			colorValuesLabel.setText("Click image or color space to inspect values.");
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "Could not load image: " + ex.getMessage());
		}
	}

	private String probeExtension(File file) throws IOException {
		String probed = Files.probeContentType(file.toPath());
		if (probed != null && probed.contains("/")) {
			return normalizeExtension(probed.substring(probed.indexOf('/') + 1));
		}
		int dot = file.getName().lastIndexOf('.');
		return dot > 0 ? normalizeExtension(file.getName().substring(dot + 1)) : "png";
	}

	private void deselectImage() {
		imageFile = new File("./src/main/resources/image-icon.png");
		imageExtension = null;
		originalImage = null;
		channelBuffer = null;
		try {
			image = ImageIO.read(imageFile);
		} catch (IOException exp) {
			exp.printStackTrace();
		}
		imageLabel.setIcon(new ImageIcon(image));
		imageInfoLabel.setText("No image selected — use Select or drag & drop.");
		colorValuesLabel.setText(" ");
		colorSpace3DPanel.setPointCloud(null, null);
	}

	private void resetToOriginal() {
		if (pristineOriginal == null) {
			return;
		}
		originalImage = copyImage(pristineOriginal);
		suppressColorComboEvents = true;
		convertToColorSystem.setSelectedIndex(0);
		suppressColorComboEvents = false;
		loadColorSystem(ColorSpaceMath.System.RGB);
		componentPanel.resetSlidersUi();
		colorValuesLabel.setText("Image reset to original.");
	}

	private void convertImageFormat(String format) {
		if (image == null || imageExtension == null) {
			return;
		}
		String target = normalizeExtension(format);
		if (normalizeExtension(imageExtension).equals(target)) {
			return;
		}
		try {
			BufferedImage toEncode = image;
			if ("jpg".equals(target) && image.getColorModel().hasAlpha()) {
				toEncode = flattenAlphaToRgb(image);
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			if (!ImageIO.write(toEncode, target, out)) {
				JOptionPane.showMessageDialog(this, "Could not encode as " + target.toUpperCase());
				return;
			}
			image = ImageIO.read(new ByteArrayInputStream(out.toByteArray()));
			originalImage = copyImage(image);
			imageExtension = target;
			imageBytes = out.size();
			int dot = imageName.lastIndexOf('.');
			if (dot > 0) {
				imageName = imageName.substring(0, dot + 1) + target;
			}
			loadColorSystem(activeSystem);
			syncFormatComboBox();
			refreshDisplay();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Format conversion failed: " + e.getMessage());
		}
	}

	private BufferedImage flattenAlphaToRgb(BufferedImage src) {
		BufferedImage rgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = rgb.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, src.getWidth(), src.getHeight());
		g.drawImage(src, 0, 0, null);
		g.dispose();
		return rgb;
	}

	private void refreshDisplay() {
		if (image == null) {
			return;
		}
		imageWidth = image.getWidth();
		imageHeight = image.getHeight();
		imageLabel.setIcon(new ImageIcon(image));
		updateImageInfoLabel();
	}

	private void updateImageInfoLabel() {
		if (imageName == null || imageExtension == null) {
			return;
		}
		int imageSize = (int) imageBytes / 1024;
		if (imageSize == 0 && imageBytes > 0) {
			imageSize = 1;
		}
		String imageSizeExtension = (imageSize / 1024 >= 1) ? "MB" : "KB";
		int displaySize = (imageSize / 1024 >= 1) ? imageSize / 1024 : imageSize;
		imageInfoLabel.setText(String.format(
				"<html><b>%s</b> &nbsp;|&nbsp; %d×%d px &nbsp;|&nbsp; %d%s &nbsp;|&nbsp; %s &nbsp;|&nbsp; Space: <b>%s</b></html>",
				imageName, imageWidth, imageHeight, displaySize, imageSizeExtension, imageExtension.toUpperCase(),
				activeSystem.name()));
	}

	private void syncFormatComboBox() {
		suppressFormatComboEvents = true;
		if ("png".equalsIgnoreCase(imageExtension)) {
			convertToImageFormat.setSelectedIndex(0);
		} else if ("jpg".equalsIgnoreCase(imageExtension) || "jpeg".equalsIgnoreCase(imageExtension)) {
			convertToImageFormat.setSelectedIndex(1);
		}
		suppressFormatComboEvents = false;
	}

	private void saveImage() {
		if (image == null || imageExtension == null) {
			return;
		}
		fileChooser.setDialogTitle("Save image");
		if (imageExtension.equalsIgnoreCase("png")) {
			fileChooser.setFileFilter(new FileNameExtensionFilter("PNG", "png"));
		} else {
			fileChooser.setFileFilter(new FileNameExtensionFilter("JPEG", "jpg", "jpeg"));
		}
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setSelectedFile(new File(imageName));
		if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			try {
				File saveFile = fileChooser.getSelectedFile();
				BufferedImage toSave = image;
				if (imageExtension.equalsIgnoreCase("jpg") && image.getColorModel().hasAlpha()) {
					toSave = flattenAlphaToRgb(image);
				}
				ImageIO.write(toSave, normalizeExtension(imageExtension), saveFile);
				JOptionPane.showMessageDialog(this, "Saved:\n" + saveFile.getAbsolutePath());
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Save failed: " + e.getMessage());
			}
		}
	}

	private BufferedImage copyImage(BufferedImage src) {
		int type = src.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
		BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), type);
		Graphics2D g = copy.createGraphics();
		g.drawImage(src, 0, 0, null);
		g.dispose();
		return copy;
	}

	private String normalizeExtension(String ext) {
		if (ext == null) {
			return "";
		}
		String e = ext.toLowerCase();
		return "jpeg".equals(e) ? "jpg" : e;
	}
}
