import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Sliders and channel enable toggles for the active color system (requirement 3).
 */
public class ComponentControlPanel extends JPanel {

	public interface Listener {
		void onChannelChanged();
	}

	private final Listener listener;
	private JPanel channelRows = new JPanel();
	private JSlider[] offsetSliders;
	private JSlider[] scaleSliders;
	private JCheckBox[] enableBoxes;
	private ChannelBuffer buffer;
	private boolean suppressEvents;

	public ComponentControlPanel(Listener listener) {
		this.listener = listener;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(MainFrame.BG_COLOR);
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), "Channel controls"));
		JLabel hint = new JLabel("<html>Offset / scale per channel · uncheck to disable</html>");
		hint.setForeground(Color.WHITE);
		hint.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		add(hint);
		channelRows.setLayout(new BoxLayout(channelRows, BoxLayout.Y_AXIS));
		channelRows.setBackground(MainFrame.BG_COLOR);
		channelRows.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		add(channelRows);
	}

	public void bind(ChannelBuffer newBuffer) {
		this.buffer = newBuffer;
		rebuildRows();
	}

	public void resetSlidersUi() {
		if (buffer == null || offsetSliders == null) {
			return;
		}
		suppressEvents = true;
		buffer.resetAdjustments();
		int n = ColorSpaceMath.channelCount(buffer.getSystem());
		for (int i = 0; i < n; i++) {
			offsetSliders[i].setValue(0);
			scaleSliders[i].setValue(100);
			enableBoxes[i].setSelected(true);
		}
		suppressEvents = false;
	}

	private void rebuildRows() {
		channelRows.removeAll();
		if (buffer == null) {
			channelRows.revalidate();
			channelRows.repaint();
			return;
		}
		ColorSpaceMath.System sys = buffer.getSystem();
		int n = ColorSpaceMath.channelCount(sys);
		String[] names = ColorSpaceMath.channelNames(sys);
		offsetSliders = new JSlider[n];
		scaleSliders = new JSlider[n];
		enableBoxes = new JCheckBox[n];

		for (int i = 0; i < n; i++) {
			JPanel row = new JPanel();
			row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
			row.setBackground(MainFrame.BG_COLOR);
			row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
			row.setAlignmentX(JLabel.LEFT_ALIGNMENT);

			enableBoxes[i] = new JCheckBox(names[i] + " enabled", true);
			enableBoxes[i].setForeground(Color.WHITE);
			enableBoxes[i].setBackground(MainFrame.BG_COLOR);
			final int ch = i;
			enableBoxes[i].addActionListener(e -> {
				if (suppressEvents) {
					return;
				}
				buffer.setEnabled(ch, enableBoxes[ch].isSelected());
				fireChange();
			});

			offsetSliders[i] = new JSlider(-100, 100, 0);
			JLabel offLabel = new JLabel(names[i] + " offset");
			offLabel.setForeground(Color.LIGHT_GRAY);
			offsetSliders[i].addChangeListener(makeSliderListener(ch, true));

			scaleSliders[i] = new JSlider(0, 200, 100);
			JLabel scaleLabel = new JLabel(names[i] + " scale %");
			scaleLabel.setForeground(Color.LIGHT_GRAY);
			scaleSliders[i].addChangeListener(makeSliderListener(ch, false));

			row.add(enableBoxes[i]);
			row.add(offLabel);
			row.add(offsetSliders[i]);
			row.add(scaleLabel);
			row.add(scaleSliders[i]);
			channelRows.add(row);
		}
		channelRows.revalidate();
		channelRows.repaint();
	}

	private ChangeListener makeSliderListener(int ch, boolean isOffset) {
		return new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (suppressEvents || buffer == null) {
					return;
				}
				if (isOffset) {
					buffer.setOffset(ch, offsetSliders[ch].getValue() / 255f);
				} else {
					buffer.setScale(ch, scaleSliders[ch].getValue() / 100f);
				}
				if (!((JSlider) e.getSource()).getValueIsAdjusting()) {
					fireChange();
				} else {
					fireChange();
				}
			}
		};
	}

	private void fireChange() {
		if (listener != null) {
			listener.onChannelChanged();
		}
	}
}
