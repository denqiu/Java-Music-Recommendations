package algorithms;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;

public class TextPopUp extends KeyAdapter implements DocumentListener {
	public enum Text {
		STARTS_WITH_INPUT, CONTAINS_INPUT;
	}
	
	private JTextField text;
	private JComboBox<Object> comboBox;
	private DefaultComboBoxModel<Object> model;
	private Object read;
	private Text t;
	private int index = 0;
	
	public void setAction() {
		setText(); set();
	}

	public void setPressed() {
		setText(); set();
	}
	
	public void set() {
	}
	
	public void setUpdate(Object read, Text t) {
		this.read = read; this.t = t;
	}
	
	public void setUpdate(Object read) {
		setUpdate(read, null);
	}
	
	public void setText() {
		text.setText(selectedItem());
	}
	
	public String selectedItem() {
		String selectedItem;
		try {
			selectedItem = comboBox.getSelectedItem().toString();
		} catch (NullPointerException n) {
			selectedItem = "";
		} 
		return selectedItem;
	}
	
	public JTextField text() {
		return text;
	}
	
	public JComboBox<Object> comboBox() {
		return comboBox;
	}
	
	public Text getText() {
		return t;
	}
	
	private boolean isAdjusting(JComboBox<Object> cbInput) {
		if (cbInput.getClientProperty("is_adjusting") instanceof Boolean)
			return (Boolean) cbInput.getClientProperty("is_adjusting");
		return false;
	}

	private void setAdjusting(JComboBox<Object> cbInput, boolean adjusting) {
		cbInput.putClientProperty("is_adjusting", adjusting);
	}
	
	private void readFile(Object read, DefaultComboBoxModel<Object> model, String input) {
		ArrayList<String> readFile = new ArrayList<String>();
		if (read instanceof String) {
			try {
				String file = (String) read;
				File f = new File(file);
				Scanner sc = new Scanner(f);
				while (sc.hasNextLine()) {
					if (t != null && !input.isEmpty())
						readFile.add(sc.nextLine());
					else
						model.addElement(sc.nextLine());
				}
				sc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (read instanceof String[]) {
			String[] items = (String[]) read;
			for (String i : items) {
				if (t != null && !input.isEmpty())
					readFile.add(i);
				else
					model.addElement(i);
			}
		}
		for (String r : readFile)
			if (t == (Text.STARTS_WITH_INPUT) ? r.regionMatches(true, 0, input, 0, input.length()) : t == Text.CONTAINS_INPUT ? contains(r, input) : null)
				model.addElement(r);
	}
	
	public static boolean contains(String region, String input) {
		char low = Character.toLowerCase(input.charAt(0)), up = Character.toUpperCase(input.charAt(0)), c;
	    for (int i = region.length() - input.length(); i >= 0; i--) {
	        c = region.charAt(i);
	        if (c != low && c != up)
	            continue;
	       return region.regionMatches(true, i, input, 0, input.length());
	    }
	    return false;
	}

	public TextPopUp(Object read, JTextField text) {
		this.text = text;
		model = new DefaultComboBoxModel<Object>();
		comboBox = new JComboBox<Object>(model) {
			private static final long serialVersionUID = 1L;
			public Dimension getPreferredSize() {
				return new Dimension(super.getPreferredSize().width, 0);
			}
		};
		setAdjusting(comboBox, false);
		if (read instanceof String) {
			String file = (String) read;
			readFile(new File(file), model, null);
		} else if (read instanceof String[]) {
			String[] items = (String[]) read;
			readFile(items, model, null);
		}
		comboBox.setSelectedItem((text.getText().isEmpty()) ? null : text.getText());
		new ComboBox().addMouseListener();
		text.addKeyListener(this);
		text.getDocument().addDocumentListener(this);
		text.setLayout(new BorderLayout());
		text.add(comboBox, BorderLayout.SOUTH);	
	}
	
	private class ComboBox extends MouseAdapter implements ActionListener {
		@Override
		public void mousePressed(MouseEvent m) {
			if (!isAdjusting(comboBox))
				if (comboBox.getSelectedItem() != null)
					if (m.getButton() == MouseEvent.BUTTON1)
						setAction();
		}
		@Override
		public void actionPerformed(ActionEvent a) { //issue is to figure out how to successfully integrate right click to prevent items from being added to textfield
			if (!isAdjusting(comboBox))
				if (comboBox.getSelectedItem() != null)
					setAction();			
		}
		private void addMouseListener() {
			try {
				comboBox.addActionListener(this);
				/*Field popup = BasicComboBoxUI.class.getDeclaredField("popup");
				popup.setAccessible(true);
				((JViewport) ((JScrollPane) ((BasicComboPopup) popup.get(comboBox.getUI())).getComponents()[0]).getComponents()[0]).getComponents()[0].addMouseListener(this);*/
			} catch (SecurityException | IllegalArgumentException /*| NoSuchFieldException | IllegalAccessException*/ e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void keyPressed(KeyEvent k) {
		setAdjusting(comboBox, true);
		boolean[] move = {k.getKeyCode() == KeyEvent.VK_UP, k.getKeyCode() == KeyEvent.VK_DOWN};
		if (move[0] || move[1]) {
			if (model.getSize() > 0) {
				k.setSource(comboBox);
				comboBox.dispatchEvent(k);
				int[] indexes = {0, comboBox.getItemCount()-1};
				index = (move[0]) ? index-1 : (move[1]) ? index+1 : index;
				if (index < indexes[0]) {
					comboBox.setSelectedItem(comboBox.getItemAt(indexes[1]));
					index = indexes[1];
				} else if (index > indexes[1]) {
					comboBox.setSelectedItem(comboBox.getItemAt(indexes[0]));
					index = indexes[0];
				}
			}
		}
		if (k.getKeyCode() == KeyEvent.VK_ENTER) {
			if (selectedItem() != null) {
				setPressed();
				comboBox.setPopupVisible(false);
			}
		}
		if (k.getKeyCode() == KeyEvent.VK_ESCAPE)
			comboBox.setPopupVisible(false);
		setAdjusting(comboBox, false);
	}
	
	@Override
	public void insertUpdate(DocumentEvent d) {
		updateList();
	}

	@Override
	public void removeUpdate(DocumentEvent d) {
		updateList();
	}

	private void updateList() {
		setAdjusting(comboBox, true);
		model.removeAllElements();
		List<String> input = Arrays.asList(text.getText().split(","));
		readFile(read, model, input.get(input.size() - 1).trim());
		for (boolean b : new boolean[] {false, model.getSize() > 0})
			comboBox.setPopupVisible(b);
		setAdjusting(comboBox, false);
	}
	
	@Override
	public void changedUpdate(DocumentEvent d) {
	}
}
