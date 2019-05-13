package algorithms;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import algorithms.TextPopUp.Text;

public class Check extends JOptionPane implements ActionListener, DocumentListener, KeyListener, ListSelectionListener {
	private static final long serialVersionUID = 1L;
	private int[] getSelected = null;
	private boolean isValidated = false;
	private String textGenre = "", playlistUrl = "", oldText = "", newText = "", username;
	private String[] select = new String[] {"Option 1: Enter genre - Unable to get user to change the genre", "Option 2: Continue - User can change the genre"}, getTracks;
	private JPanel enterGenre = new JPanel(), refreshGenre = new JPanel(), checkPanel, textPanels;
	private JPanel[] panels = {enterGenre, refreshGenre};
	private JButton ok = button("Ok"), refresh;
	private JComboBox<String> comboBox = comboBox(select);
	final private int columnSize = 10;
	private JList<String> checkTracks;
	private JTextField g = new JTextField(15);
	private JTextField[] texts;
	private JFrame frame = new JFrame();
	
	private JButton button(String name) {
		JButton button = new JButton(name);
		button.addActionListener(this);
		return button;
	}
	
	private JComboBox<String> comboBox(String[] items) {
		JComboBox<String> comboBox = new JComboBox<String>(items);
		comboBox.addActionListener(this);
		return comboBox;
	}
	
	private ArrayList<String> setUnique(ArrayList<String> toUnique) {
		LinkedHashSet<String> unique = new LinkedHashSet<String>(toUnique);
		toUnique.clear();
		toUnique.addAll(unique);
		return toUnique;
	}
	
	public Check(String textGenre, String playlistUrl) {
		this.textGenre = textGenre; this.playlistUrl = playlistUrl;
		refresh = button(playlistUrl);
	}
	
	public Check(String[] getTracks, String textGenre, String username) {
		this.getTracks = getTracks; this.textGenre = textGenre; this.username = username;  
	}
	
	public Check(String[] getTracks, String textGenre) {
		this.getTracks = getTracks; this.textGenre = textGenre;  
	}

	public Check(ArrayList<String> getTracks, String textGenre, String username) {
		getTracks = setUnique(getTracks);
		this.getTracks = SetUp.Files.toArray(getTracks); this.textGenre = textGenre; this.username = username;  
	}
	
	public Check(ArrayList<String> getTracks, String textGenre) {
		getTracks = setUnique(getTracks);
		this.getTracks = SetUp.Files.toArray(getTracks); this.textGenre = textGenre;  
	}
	
	public String playlistGenre() {
		JPanel message = new JPanel(), options = new JPanel();
		String changeGenre = "<html>This playlist either has an incorrect or misleading genre tag or no genre tag at all.<br><center>Please change the genre tag appropriately.</center></html>";
		JLabel m = new JLabel(changeGenre), enter = new JLabel("Enter Genre: ");
		message.add(m);
		options.add(comboBox);
		TextPopUp popup = new TextPopUp(textGenre, g);
		popup.setUpdate(textGenre, Text.CONTAINS_INPUT);
		panels[0].add(enter);
		panels[0].add(g);
		panels[1].add(refresh);
		Object[] layout = new Object[] {message, options, panels[0], panels[1]};
		g.getDocument().addDocumentListener(this);
		ok.setEnabled(false);
		panels[1].setVisible(false);
		showOptionDialog(frame, layout, "Change Genre", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, new JButton[] {ok}, null);
		return (panels[0].isVisible()) ? g.getText() : null;
	}
	
	public void checkTracks() {
		textPanels = new JPanel(); checkPanel = new JPanel();
		JLabel checkLabel = new JLabel("Check tracks: ");
		ArrayList<String> checkTrack = SetUp.Files.arraylist(getTracks);
		if (checkTrack.contains(oldText))
			checkTrack.remove(oldText);
		String file = (username != null) ? SetUp.Files.SoundCloud("txt", username, false) : SetUp.Files.file("Music_Database");
		try {
			new SetUp.Files(file, checkTrack, 0);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		getTracks = SetUp.Files.toArray(checkTrack);
		checkTracks = SearchUsers.createJList(getTracks, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, columnSize, null);
		JScrollPane scroll = SearchUsers.getScroll(checkTracks, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		if (getSelected != null) {
			checkTracks.setSelectedIndices(getSelected);
			checkTracks.scrollRectToVisible(checkTracks.getCellBounds(checkTracks.getMinSelectionIndex(), checkTracks.getMaxSelectionIndex()));
		}
		checkTracks.addKeyListener(this);
		checkTracks.addListSelectionListener(this);
		checkPanel.add(checkLabel); 
		checkPanel.add(scroll);
		textPanels.setLayout(new FlowLayout());
		String[] s = (username != null) ? new String[] {"Track", "Artist", "Genre"} : new String[] {"Artist", "Track", "Year", "Genre"};
		texts = new JTextField[s.length];
		int i;
		for (i = 0; i < s.length; i++) {
			textPanels.add(new JLabel(s[i] + ":"));
			JTextField t = new JTextField(15);
			t.addKeyListener(this);
			textPanels.add(t);
			texts[i] = t;
		}
		TextPopUp popup = new TextPopUp(textGenre, texts[i-1]);
		popup.setUpdate(textGenre, Text.CONTAINS_INPUT);
		Object[] layout = new Object[] {checkPanel, textPanels};
		int check = showOptionDialog(null, layout, "Check Tracks", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[] {"Ok", "Delete"}, null);
		if (check != YES_OPTION) {
			if (check == NO_OPTION) {
				file = (username != null) ? SetUp.Files.SoundCloud("txt", username, false) : SetUp.Files.file("Music_Database");
				ArrayList<String> delete = SetUp.Files.arraylist(new File(file));
				delete.removeAll(checkTracks.getSelectedValuesList());
				Collections.sort(delete, String.CASE_INSENSITIVE_ORDER);
				try {
					new SetUp.Files(file, delete, 0);
				} catch (IOException e) {
					e.printStackTrace();
				}
				delete = setUnique(delete);
				getTracks = SetUp.Files.toArray(delete);
			} 
			checkPanel = textPanels = null;
			checkTracks();
		}
		if (Thread.currentThread().isAlive())
			System.exit(0);
	}
	
	@Override
	public void actionPerformed(ActionEvent a) {
		for (int i = 0; i < panels.length; i++) 
			panels[i].setVisible(comboBox.getSelectedItem().toString().equals(select[i]));
		ok.setEnabled((panels[0].isVisible()) ? !g.getText().isEmpty() : true);
		if (a.getSource() == ok) {
			frame.dispose();
		} else if (a.getSource() == refresh) {
			try {
				Desktop.getDesktop().browse(new URI(playlistUrl));
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		} 
	}
	
	private void setTexts() {
		String[] split = (checkTracks.getSelectedValue() != null) ? (username != null) ? checkTracks.getSelectedValue().split(",") : MusicDatabase.split(checkTracks.getSelectedValue()) : null;
		if (split != null && !isValidated) {
			for (int i = 0; i < texts.length; i++) {
				String s = split[i].trim();
				if (i == texts.length-1) {
					texts[i].setText(s);
				} else {
					JTextField t = new JTextField(s);
					t.addKeyListener(this);
					int index = 0;
					for (int j = 0; j < textPanels.getComponentCount(); j++) {
						Component c = textPanels.getComponent(j);
						if (c instanceof JTextField) {
							JTextField text = (JTextField) c;
							if (text.equals(texts[i]))
								index = j;
						}
					}
					textPanels.remove(texts[i]);
					textPanels.repaint();
					textPanels.revalidate();
					texts[i] = t;
					textPanels.add(t, index);	
				}
			}	
			isValidated = true;
		} else {
			isValidated = false;
		}
		texts[texts.length-1].setText(texts[texts.length-1].getText());
	}
	
	private void textUpdate() {
		
	}

	@Override
	public void insertUpdate(DocumentEvent d) {
		ok.setEnabled(!g.getText().isEmpty());
	}

	@Override
	public void removeUpdate(DocumentEvent d) {
		ok.setEnabled(!g.getText().isEmpty());
	}
	
	@Override
	public void keyPressed(KeyEvent k) {
		if (k.getKeyCode() == KeyEvent.VK_ENTER) {
			JTextField text = null;
			try {
				text = (JTextField) k.getSource();
			} catch (ClassCastException c) {
				return;
			}
			String s = (username != null) ? text.getSelectedText() : text.getText(), file = (username != null) ? SetUp.Files.SoundCloud("txt", username, false) : SetUp.Files.file("Music_Database");
			ArrayList<String> tracks = SetUp.Files.arraylist(new File(file));
			if (s != null) {
				String[] split = (s != null) ? text.getText().split(s) : null;
				if (split != null && username != null) {
					texts[0].setText(split[0].trim()); 
					if (split[1].contains("("))
						texts[1].setText(split[1].split("\\(")[0].trim());
					else if (split[1].contains("Fea"))
						texts[1].setText(split[1].split("Fea")[0].trim());
					else
						texts[1].setText(split[1].trim());
				}
				getSelected = checkTracks.getSelectedIndices();
				for (int c : getSelected) {
					for (int i = 0; i < tracks.size(); i++) {
						if (i == c) {
							if (username != null) {
								split = tracks.get(i).split(",");
								if (split[0].contains(s)) {
									String[] getSplit = split[0].split(s);
									if (getSplit != null) {
										newText = getSplit[0].trim();
										if (getSplit[1].contains("("))
											newText += ", " + getSplit[1].split("\\(")[0].trim();
										else if (getSplit[1].contains("Fea"))
											newText += ", " + getSplit[1].split("Fea")[0].trim();
										else
											newText += ", " + split[1].trim();
									}
									newText += ", " + split[2].trim();
								}
							} else {
								split = MusicDatabase.split(tracks.get(i));
								newText = texts[0].getText() + " - "; String comma;
								for (int j = 1; j < texts.length; j++) {
									comma = (j == texts.length-1) ? "" : ", ";
									newText += texts[j].getText() + comma;
								}
							}
							oldText = tracks.get(i);
							tracks.set(i, newText);
						}
					}
				}
			} else {
				String comma;
				for (int i = 0; i < texts.length; i++) {
					comma = (i == texts.length-1) ? "" : ", ";
					newText += texts[i].getText() + comma;
				}
				tracks.set(checkTracks.getSelectedIndex(), newText);
				getSelected = new int[] {checkTracks.getSelectedIndex()};
			}
			try {
				new SetUp.Files(file, tracks, 0);
			} catch (IOException e) {
				e.printStackTrace();
			}
			tracks = SetUp.Files.arraylist(new File(file));
			Collections.sort(tracks, String.CASE_INSENSITIVE_ORDER);
			tracks = setUnique(tracks);
			getTracks = SetUp.Files.toArray(tracks);
			getRootFrame().dispose();
		} 
	}
	
	@Override
	public void keyReleased(KeyEvent k) {
		if (k.getKeyCode() == KeyEvent.VK_UP || k.getKeyCode() == KeyEvent.VK_DOWN) {
			setTexts();
		}
	}
	
	@Override
	public void valueChanged(ListSelectionEvent l) {
		setTexts();
	}
	
	@Override
	public void changedUpdate(DocumentEvent d) {
	}
	@Override
	public void keyTyped(KeyEvent k) {		
	}
}
