import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Panel for requirement 8 — displays extended image information.
 */
public class ImageInfoPanel extends JPanel {

	private final JLabel contentLabel;

	public ImageInfoPanel() {
		setLayout(new BorderLayout());
		setBackground(MainFrame.BG_COLOR);
		setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.GRAY), "Image information"));

		contentLabel = new JLabel("No image loaded.");
		contentLabel.setForeground(Color.WHITE);
		contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		contentLabel.setVerticalAlignment(JLabel.TOP);

		JScrollPane scroll = new JScrollPane(contentLabel);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getViewport().setBackground(MainFrame.BG_COLOR);
		scroll.setOpaque(false);
		add(scroll, BorderLayout.CENTER);
		setPreferredSize(new java.awt.Dimension(260, 200));
		setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 220));
	}

	public void showMetadata(ImageMetadata metadata) {
		contentLabel.setText(metadata.toHtml());
	}

	public void clear() {
		contentLabel.setText("<html><i>No image loaded.</i></html>");
	}
}
