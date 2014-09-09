package net.eliosoft.multisubs;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import net.eliosoft.multisubs.server.HttpServerManager;

public class CSVTable extends JFrame {
	private static final int MAX_MESSAGE_SIZE = 246;
	private JTable table;
	private JPanel radioPanel = new JPanel();
	private JFileChooser fc;
	private DefaultTableModel model;
	private int titlesCount = 0;
	private JButton closeButton, loadButton;
	private static HttpServerManager httpServerManager = HttpServerManager.getInstance();
	private static String[] webTexts = {};
	private SwingWorker<Void, Void> timer = null;

	/**
	 * Takes data from a CSV file and places it into a table for display.
	 */
	public CSVTable(String title) {
		super(title);
		
		table = new JTable();

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setReorderingAllowed(false);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent lse) {
				if(model != null && !lse.getValueIsAdjusting()){
					if(timer != null){
						timer.cancel(true);
						timer = null;
					}
					final ListSelectionModel lsm = (ListSelectionModel)lse.getSource();
					
					if(lsm.getMinSelectionIndex() == -1){
						webTexts = new String[titlesCount - 1];
						return;
					}
					
					final int time = Integer.parseInt((String)model.getValueAt(lsm.getMinSelectionIndex(),0));
					
					
					webTexts = new String[titlesCount - 1];
					for(int i=1 ; i<titlesCount ; i++){
						String webValue = (String) model.getValueAt(lsm.getMinSelectionIndex(),i);
						if(webValue != null){
							webTexts[i - 1] = webValue;
						} else {
							webTexts[i - 1] = "";
						}
					}
					
					if(time > 0){
						timer = new SwingWorker<Void, Void>(){

							@Override
							protected Void doInBackground() throws Exception {
								int selectedRow = lsm.getMinSelectionIndex();
								try {
									for(int i = time; i > 0 ; i--){
										table.setValueAt("<html><body><font color='red'><b>"+i+"</b></font></body></html>", selectedRow, 0);
										Thread.sleep(100);
									}
									table.setValueAt(time+"", selectedRow, 0);
									lsm.setSelectionInterval(selectedRow+1, selectedRow+1);
								} catch (InterruptedException e) {
									table.setValueAt(time+"", selectedRow, 0);
								}
								return null;
							}
							
						};
						timer.execute();
					}
				}
			}
		});
		
		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
						row, column);
				if(value != null && ((String) value).length() >= MAX_MESSAGE_SIZE){
					label.setForeground(Color.RED);
				} else {
					label.setForeground(Color.BLACK);
				}
				return label;
			}
			
		});
		JScrollPane scroll = new JScrollPane(table);

		JPanel buttonPanel = new JPanel();
		closeButton = new JButton("Close");
		loadButton = new JButton("Load File...");
		buttonPanel.add(loadButton);
		buttonPanel.add(closeButton);
		
		fc = new JFileChooser();
		loadButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (ae.getSource() == loadButton) {
			        int returnVal = fc.showOpenDialog(table);

			        if (returnVal == JFileChooser.APPROVE_OPTION) {
			        	if(timer != null){
							timer.cancel(true);
							timer = null;
						}
			            File file = fc.getSelectedFile();
			            
						try {
							FileInputStream fis = new FileInputStream(file);
							insertData(fis);
				            
				            fis.close();
						} catch (Exception e) {
							JOptionPane.showMessageDialog(table, e, e.getLocalizedMessage(), JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
						}
			        }
			   }
			}
		});

		closeButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent ae) {
				httpServerManager.stopHttp();
				System.exit(0);
			}
		});
		
		getContentPane().add(radioPanel,BorderLayout.NORTH);
		getContentPane().add(scroll, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		pack();
	}

	/**
	 * Places the data from the specified stream into this table for display.
	 * The data from the file must be in CSV format
	 * 
	 * @param is
	 *            - an input stream which could be from a file or a network
	 *            connection or URL.
	 */
	void insertData(InputStream is) {
		Scanner scan = new Scanner(is,"UTF-8");
		model = null;

		String[] array;
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			array = line.split("\t");
			String[] data = new String[array.length];
			for (int i = 0; i < array.length; i++)
				data[i] = array[i];
			
			if(model == null){
				titlesCount = data.length;
				
				model = new DefaultTableModel(data, 0){
					@Override
					public boolean isCellEditable(int arg0, int arg1) {
						return false;
					}
				};
				
			} else {
				model.addRow(data);
			}
		}
		table.setModel(model);
	}
	
	public static String[] getWebText() {
		return webTexts;
	}
	
	public static void main(String args[]) {
		CSVTable frame = new CSVTable("MultiSubs");
		frame.setVisible(true);
		try {
			httpServerManager.startHttp();
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(frame, e1.getMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}