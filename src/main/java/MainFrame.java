import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
	private int imageWidth = 0;
	private int imageHeight = 0;
	private long imageBytes = 0;
	String[] colorSystems = { "RGB", "CMYK", "HSV", "YUV", "LAB", "YCbCr" };
	String[] imageFormats = { "PNG", "JPEG", "JPG", "GIF" };
	JComboBox<String> convertToColorSystem = null;
	JComboBox<String> convertToImageFormat = null;

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
		imageToolsPanel.setLayout(new BoxLayout(imageToolsPanel, BoxLayout.Y_AXIS));
		initializeButtons();
		imageToolsPanel.add(selectImageButton);
		imageToolsPanel.add(deselectImageButton);
		imageToolsPanel.add(convertToColorSystem);
		imageToolsPanel.add(convertToImageFormat);
		imageWorkingSpacePanel.setLayout(new BorderLayout());
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
		selectImageButton = new JButton("select image");
		deselectImageButton = new JButton("deselect image");
		selectImageButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, selectImageButton.getPreferredSize().height));
		deselectImageButton
				.setMaximumSize(new Dimension(Integer.MAX_VALUE, deselectImageButton.getPreferredSize().height));
		selectImageButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				selectImageButtonActionListener(event);
			}
		});
		deselectImageButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				deselectImageButtonActionListener(event);
			}
		});
		convertToColorSystem = new JComboBox<String>(colorSystems);
		convertToColorSystem
				.setMaximumSize(new Dimension(Integer.MAX_VALUE, convertToColorSystem.getPreferredSize().height));
		convertToImageFormat = new JComboBox<String>(imageFormats);
		convertToImageFormat
				.setMaximumSize(new Dimension(Integer.MAX_VALUE, convertToImageFormat.getPreferredSize().height));

	}

	private void selectImageButtonActionListener(ActionEvent event) {
		fileChooser.setFileFilter(new FileNameExtensionFilter("image files", "png", "jpg", "jpeg", "gif"));
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setCurrentDirectory(new File("."));
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
			imageInfoString = new String(String.format("Image Name : %s ==== Pixels : %d × %d ==== Size On Disk : %d%s",
					imageName, imageWidth, imageHeight, imageSize, imageSizeExtension));
			imageInfoLabel.setText(imageInfoString);
		}
	}

	private void deselectImageButtonActionListener(ActionEvent event) {
		imageFile = new File("./src/main/resources/image-icon.png");
		try {
			image = ImageIO.read(imageFile);
		} catch (IOException exp) {
			exp.printStackTrace();
		}
		imageLabel.setIcon(new ImageIcon(image));
		imageInfoString = new String("No image was selected, please select one from the select button.");
		imageInfoLabel.setText(imageInfoString);
	}
}