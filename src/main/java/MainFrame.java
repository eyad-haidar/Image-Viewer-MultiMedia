import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainFrame extends JFrame {

	static final Color BG_COLOR = new Color(40, 44, 52);
	static final Color LIGHT_BLUE = Color.decode("#71A0FF");
	private final JFileChooser fileChooser = new JFileChooser();
	private JButton selectImageButton = null;
	private JButton deselectImageButton = null;
	private JPanel imageWorkingSpacePanel = null;
	private JPanel imageToolsPanel = null;
	private JLabel imageLabel = null;
	private JLabel imageInfoLabel = null;
	private BufferedImage image = null;
	private File imageFile = null;
	private String imageInfoString = null;
	private String imageName = null;
	private String imageExtension = null;
	private int imageWidth = 0;
	private int imageHeight = 0;
	private long imageBytes = 0;
	String[] colorSystems = { "RGB", "CMYK", "HSV", "YUV", "LAB", "YCbCr" };
	String[] imageFormats = { "PNG", "JPG", "BMP" };
	JComboBox<String> convertToColorSystem = null;
	JComboBox<String> convertToImageFormat = null;
	Stack<BufferedImage> images = null;
	private JButton saveImageButton = null;

	public MainFrame() {
		SwingUtilities.invokeLater(() -> {
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setTitle("PixelLab");
			this.setSize(1250, 950);
			this.setResizable(false);
			this.setLocationRelativeTo(null);
			this.setVisible(true);
			this.setLayout(new BorderLayout());
			this.getContentPane().setBackground(BG_COLOR);
		});
		initializePanels();
		this.add(imageToolsPanel, BorderLayout.EAST);
		this.add(imageWorkingSpacePanel, BorderLayout.CENTER);
	}

	private void initializePanels() {
		imageWorkingSpacePanel = new JPanel();
		imageToolsPanel = new JPanel();
		imageToolsPanel.setBackground(Color.red);
		imageWorkingSpacePanel.setLayout(new BorderLayout());
		imageToolsPanel.setLayout(new BoxLayout(imageToolsPanel, BoxLayout.Y_AXIS));
		initializeButtons();
		imageToolsPanel.add(selectImageButton);
		imageToolsPanel.add(deselectImageButton);
		imageToolsPanel.add(convertToColorSystem);
		imageToolsPanel.add(convertToImageFormat);
		imageToolsPanel.add(saveImageButton);
		imageWorkingSpacePanel.setBackground(Color.black);
		initailzeImageSpace();
	}

	private void initailzeImageSpace() {
		imageFile = new File("./src/main/resources/image-icon.png");
		try {
			image = ImageIO.read(imageFile);
		} catch (IOException exp) {
			exp.printStackTrace();
		}

		ImageIcon initialImage = new ImageIcon(image);
		imageLabel = new JLabel(initialImage);
		imageLabel.setBorder(BorderFactory.createLineBorder(Color.white, 2));
		imageLabel.setHorizontalAlignment(JLabel.CENTER);
		imageLabel.setVerticalAlignment(JLabel.CENTER);
		imageInfoString = new String("No image was selected, please select one from the select button.");
		imageInfoLabel = new JLabel(imageInfoString);
		imageInfoLabel.setOpaque(false);
		imageInfoLabel.setForeground(Color.white);
		imageInfoLabel.setFont(new Font("Serif", Font.BOLD, 14));
		imageInfoLabel.setHorizontalAlignment(JLabel.LEFT);
		imageInfoLabel.setVerticalAlignment(JLabel.CENTER);
		imageWorkingSpacePanel.add(imageLabel, BorderLayout.CENTER);
		imageWorkingSpacePanel.add(imageInfoLabel, BorderLayout.SOUTH);
	}

	private void initializeButtons() {
		selectImageButton = new JButton("Select image");
		deselectImageButton = new JButton("Deselect image");
		saveImageButton = new JButton("Save Image");
		selectImageButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, selectImageButton.getPreferredSize().height));
		deselectImageButton
				.setMaximumSize(new Dimension(Integer.MAX_VALUE, deselectImageButton.getPreferredSize().height));
		saveImageButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, saveImageButton.getPreferredSize().height));
		selectImageButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				selectImageButtonActionListener();
			}
		});
		deselectImageButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				deselectImageButtonActionListener();
			}
		});
		saveImageButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveImage();
			}
		});
		convertToColorSystem = new JComboBox<String>(colorSystems);
		convertToColorSystem
				.setMaximumSize(new Dimension(Integer.MAX_VALUE, convertToColorSystem.getPreferredSize().height));
		convertToColorSystem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String selectedItem = (String) convertToColorSystem.getSelectedItem();
				if (selectedItem.equalsIgnoreCase("CMYK")) {
					convertTo_CMYK();
				} else if (selectedItem.equalsIgnoreCase("YCbCr")) {
					convertTo_YCbCr();
				}
			}
		});
		convertToImageFormat = new JComboBox<String>(imageFormats);
		convertToImageFormat
				.setMaximumSize(new Dimension(Integer.MAX_VALUE, convertToImageFormat.getPreferredSize().height));
		convertToImageFormat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				String selectedItem = (String) convertToImageFormat.getSelectedItem();
				if (selectedItem.equalsIgnoreCase("PNG")) {
					convertToPNG();
				} else if (selectedItem.equalsIgnoreCase("JPG")) {
					convertToJPG();
				}
			}
		});
	}

	private void convertTo_RGP() {

	}

	private void convertTo_YCbCr() {
		
	}

	private void convertTo_CMYK() {
	}

	private void convertTo_HSV() {

	}

	private void convertTo_YUV() {

	}

	private void convertTo_LAB() {

	}

	private void convertToJPG() {
		imageExtension = "jpg";
		String newType = imageInfoString.replace("png", "jpg");
		imageInfoString = new String(newType);
		imageInfoLabel.setText(imageInfoString);
		try {
			ImageIO.write(image, "jpg", imageFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void convertToPNG() {
		imageExtension = "jpg";
		String newType = imageInfoString.replace("jpg", "png");
		imageInfoString = new String(newType);
		imageInfoLabel.setText(imageInfoString);
		try {
			ImageIO.write(image, "png", imageFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void selectImageButtonActionListener() {
		fileChooser.setFileFilter(new FileNameExtensionFilter("image files", "png", "jpg", "jpeg", "bmp"));
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setCurrentDirectory(new File("./src/main/resources"));
		int response = fileChooser.showOpenDialog(null);
		if (response == JFileChooser.APPROVE_OPTION) {
			imageFile = fileChooser.getSelectedFile();
			try {
				image = ImageIO.read(imageFile);
			} catch (IOException exp) {
				exp.printStackTrace();
			}
			imageLabel.setIcon(new ImageIcon(image));
			imageName = imageFile.getName();
			imageWidth = image.getWidth();
			imageHeight = image.getHeight();
			imageBytes = imageFile.length();
			int imageSize = (int) imageBytes / 1024;
			String imageSizeExtension = (imageSize / 1024 >= 1) ? "MB" : "KB";
			try {
				imageExtension = Files.probeContentType(imageFile.toPath()).substring(6).toLowerCase();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (imageExtension.equalsIgnoreCase("png")) {
				convertToImageFormat.setSelectedIndex(0);
			} else if (imageExtension.equalsIgnoreCase("jpeg") || imageExtension.equalsIgnoreCase("jpg")) {
				convertToImageFormat.setSelectedIndex(1);
			}
			imageInfoString = new String(
					String.format("Image Name : %s ==== Pixels : %d × %d ==== Size On Disk : %d%s ==== Type : %s",
							imageName, imageWidth, imageHeight, imageSize, imageSizeExtension, imageExtension));
			imageInfoLabel.setText(imageInfoString);
			if (images == null) {
				images = new Stack<BufferedImage>();
			}
			BufferedImage archivedImage = image;
			images.add(archivedImage);
		}
	}

	private void deselectImageButtonActionListener() {
		imageFile = new File("./src/main/resources/image-icon.png");
		imageExtension = null;
		try {
			image = ImageIO.read(imageFile);
		} catch (IOException exp) {
			exp.printStackTrace();
		}
		imageLabel.setIcon(new ImageIcon(image));
		imageInfoString = new String("No image was selected, please select one from the select button.");
		imageInfoLabel.setText(imageInfoString);
	}

	private void saveImage() {
		fileChooser.setDialogTitle("Save image");
		if (imageExtension.equalsIgnoreCase("png")) {
			fileChooser.setFileFilter(new FileNameExtensionFilter("image files", "png"));
		} else {
			fileChooser.setFileFilter(new FileNameExtensionFilter("image files", "jpg"));
		}
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setSelectedFile(imageFile);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int response = fileChooser.showSaveDialog(null);
		if (response == JFileChooser.APPROVE_OPTION) {
			try {
				File selectedDir = fileChooser.getSelectedFile();
				File saveFile = new File(selectedDir, imageFile.getName());
				ImageIO.write(image, imageExtension, saveFile);
				JOptionPane.showMessageDialog(this, "Image saved to:\n" + saveFile.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		fileChooser.setSelectedFile(imageFile);

	}
}