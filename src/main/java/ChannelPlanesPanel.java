import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Shows each color-space channel as a separate grayscale image (requirement 3).
 */
public class ChannelPlanesPanel extends JPanel {

	private static final int THUMB = 72;

	public ChannelPlanesPanel() {
		setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
		setBackground(MainFrame.BG_COLOR);
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Channel planes"));
	}

	public void updateFrom(ChannelBuffer buffer) {
		removeAll();
		if (buffer == null) {
			revalidate();
			repaint();
			return;
		}
		int n = ColorSpaceMath.channelCount(buffer.getSystem());
		String[] names = ColorSpaceMath.channelNames(buffer.getSystem());
		for (int ch = 0; ch < n; ch++) {
			BufferedImage plane = buffer.getChannelPreview(ch);
			JLabel lbl = new JLabel(new ImageIcon(scale(plane, THUMB)));
			lbl.setToolTipText(names[ch]);
			lbl.setText(names[ch]);
			lbl.setVerticalTextPosition(JLabel.BOTTOM);
			lbl.setHorizontalTextPosition(JLabel.CENTER);
			lbl.setForeground(Color.WHITE);
			add(lbl);
		}
		revalidate();
		repaint();
	}

	private static BufferedImage scale(BufferedImage src, int maxSide) {
		int w = src.getWidth();
		int h = src.getHeight();
		float s = maxSide / (float) Math.max(w, h);
		int nw = Math.max(1, Math.round(w * s));
		int nh = Math.max(1, Math.round(h * s));
		BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = out.createGraphics();
		g.drawImage(src, 0, 0, nw, nh, null);
		g.dispose();
		return out;
	}
}
