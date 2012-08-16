/**
 * 
 */
package com.trungsi.vpclient;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.PropertyConfigurator;

/**
 * @author trungsi
 *
 */
public class VPGUI {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String vphome = prepareVphome(args);
		Map<String, String> context = loadContext(vphome);
		
		PropertyConfigurator.configure(VPGUI.class.getResourceAsStream("/log4j.properties"));
		
		
		JFrame frame = new JFrame("VPClient");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		final JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JPanel formPanel = new JPanel();
		formPanel.setLayout(new GridLayout(0, 2));
		
		JLabel loginLabel = new JLabel("Login");
		formPanel.add(loginLabel);
		final JTextField loginField = new JTextField(getDefault(VPClient.USER, context), 30);
		formPanel.add(loginField);
		
		JLabel passwordLabel = new JLabel("Password");
		formPanel.add(passwordLabel);
		final JPasswordField passwordField = new JPasswordField(getDefault(VPClient.PWD, context), 30);
		formPanel.add(passwordField);
		
		JLabel selectedSaleLabel = new JLabel("Selected Sale");
		formPanel.add(selectedSaleLabel);
		final JTextField selectedSaleField = new JTextField(getDefault(VPClient.SELECTED_SALE, context), 30);
		formPanel.add(selectedSaleField);
		
		JLabel selectedCatsLabel = new JLabel("Selected Categories");
		formPanel.add(selectedCatsLabel);
		final JTextField selectedCatsField = new JTextField(getDefault(VPClient.SELECTED_CATS, context), 30);
		formPanel.add(selectedCatsField);
		
		JLabel ignoreSubCatsLabel = new JLabel("Ignore Sub Categories");
		formPanel.add(ignoreSubCatsLabel);
		final JTextField ignoreSubCatsField = new JTextField(getDefault(VPClient.IGNORE_SUB_CATS, context), 30);
		formPanel.add(ignoreSubCatsField);
		
		JLabel womanJeanSizesLabel = new JLabel("Woman Jean Sizes");
		formPanel.add(womanJeanSizesLabel);
		final JTextField womanJeanSizesField = new JTextField(getDefault(VPClient.WOMAN_JEAN_SIZES, context, "26 ,W26,T.36,T. 36"), 30);
		formPanel.add(womanJeanSizesField);
		
		JLabel womanShoesSizesLabel = new JLabel("Woman Shoes Sizes");
		formPanel.add(womanShoesSizesLabel);
		final JTextField womanShoesSizesField = new JTextField(getDefault(VPClient.WOMAN_SHOES_SIZES, context, "37 ,T.37"), 30);
		formPanel.add(womanShoesSizesField);
		
		JLabel womanLingerieSizesLabel = new JLabel("Woman Lingerie Sizes");
		formPanel.add(womanLingerieSizesLabel);
		final JTextField womanLingerieSizesField = new JTextField(getDefault(VPClient.WOMAN_LINGERIE_SIZES, context, "95D LOL"), 30);
		formPanel.add(womanLingerieSizesField);
		
		JLabel womanClothingSizesLabel = new JLabel("Woman Clothing Sizes");
		formPanel.add(womanClothingSizesLabel);
		final JTextField womanClothingSizesField = new JTextField(getDefault(VPClient.WOMAN_CLOTHING_SIZES, context, "36 ,T.36 (FR),T.36 ,T. 36,34/36, S ,.S "), 30);
		formPanel.add(womanClothingSizesField);
		
		JLabel girlShoesSizesLabel = new JLabel("Girl Shoes Sizes");
		formPanel.add(girlShoesSizesLabel);
		final JTextField girlShoesSizesField = new JTextField(getDefault(VPClient.GIRL_SHOES_SIZES, context, "23 ,T.23,T. 23"), 30);
		formPanel.add(girlShoesSizesField);
		
		JLabel girlClothingSizesLabel = new JLabel("Girl Clothing Sizes");
		formPanel.add(girlClothingSizesLabel);
		final JTextField girlClothingSizesField = new JTextField(getDefault(VPClient.GIRL_CLOTHING_SIZES, context, "3 ans"), 30);
		formPanel.add(girlClothingSizesField);
		
		JLabel manJeanSizesLabel = new JLabel("Man Jean Sizes");
		formPanel.add(manJeanSizesLabel);
		final JTextField manJeanSizesField = new JTextField(getDefault(VPClient.MAN_JEAN_SIZES, context, "30 ,W30,T.30,T.40,T. 40"), 30);
		formPanel.add(manJeanSizesField);
		
		JLabel manShoesSizesLabel = new JLabel("Man Shoes Sizes");
		formPanel.add(manShoesSizesLabel);
		final JTextField manShoesSizesField = new JTextField(getDefault(VPClient.MAN_SHOES_SIZES, context, "40.5, 41 ,T.41,T. 41"), 30);
		formPanel.add(manShoesSizesField);
		
		JLabel manCostumeSizesLabel = new JLabel("Man Costume Sizes");
		formPanel.add(manCostumeSizesLabel);
		final JTextField manCostumeSizesField = new JTextField(getDefault(VPClient.MAN_COSTUME_SIZES, context, "M ,.M ,T.40,T. 40"), 30);
		formPanel.add(manCostumeSizesField);
		
		JLabel manClothingSizesLabel = new JLabel("Man Clothing Sizes");
		formPanel.add(manClothingSizesLabel);
		final JTextField manClothingSizesField = new JTextField(getDefault(VPClient.MAN_CLOTHING_SIZES, context, "M ,.M , 38 , 40 ,T.40,T. 40"), 30);
		formPanel.add(manClothingSizesField);
		
		JButton button = new JButton("Start");
		button.addActionListener(new ActionListener() {
			//@Override
			public void actionPerformed(ActionEvent e) {
				new Thread() {
					public void run() {
						Map<String, String> context = new HashMap<String, String>();
						context.put(VPClient.DRIVER_NAME, VPClient.HTML_UNIT);
						context.put(VPClient.USER, loginField.getText());
						context.put(VPClient.PWD, new String(passwordField.getPassword()));
						context.put(VPClient.SELECTED_SALE, selectedSaleField.getText());
						context.put(VPClient.SELECTED_CATS, selectedCatsField.getText());
						context.put(VPClient.IGNORE_SUB_CATS, ignoreSubCatsField.getText());
						context.put(VPClient.WOMAN_JEAN_SIZES, womanJeanSizesField.getText());
						context.put(VPClient.WOMAN_SHOES_SIZES, womanShoesSizesField.getText());
						context.put(VPClient.WOMAN_LINGERIE_SIZES, womanLingerieSizesField.getText());
						context.put(VPClient.WOMAN_CLOTHING_SIZES, womanClothingSizesField.getText());
						context.put(VPClient.GIRL_SHOES_SIZES, girlShoesSizesField.getText());
						context.put(VPClient.GIRL_CLOTHING_SIZES, girlClothingSizesField.getText());
						context.put(VPClient.MAN_JEAN_SIZES, manJeanSizesField.getText());
						context.put(VPClient.MAN_SHOES_SIZES, manShoesSizesField.getText());
						context.put(VPClient.MAN_COSTUME_SIZES, manCostumeSizesField.getText());
						context.put(VPClient.MAN_CLOTHING_SIZES, manClothingSizesField.getText());
						
						//context.put(VPClient.P, value)
						final VPClientAsync client = new VPClientAsync(context);
						client.start();
						
						try {
							SwingUtilities.invokeAndWait(new Runnable() {
								//@Override
								public void run() {
									VPClientPanel clientPanel = new VPClientPanel(client);
									mainPanel.add(clientPanel);
									mainPanel.validate();
									mainPanel.revalidate();
								}
							});
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}.start();
				
			}
		});
		
		formPanel.add(button);
		
		mainPanel.add(formPanel);
		
		frame.getContentPane().add(mainPanel, BorderLayout.NORTH);
		frame.pack();
		
		frame.setVisible(true);

	}

	private static String getDefault(String key, Map<String, String> context) {
		return getDefault(key, context, "");
	}

	private static String getDefault(String key, Map<String, String> context, String defaultValue) {
		String value = context.get(key);
		return value != null ? value : defaultValue;
	}
	
	private static Map<String, String> loadContext(String vphome) {
		String config = "config.properties";
		InputStream input = VPGUI.class.getResourceAsStream(config);
		if (input == null) {
			File file = new File(vphome + "/" + config);
			if (file.exists()) {
				try {
					input = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		HashMap<String, String> context = new HashMap<String, String>();
		if (input != null) {
			Properties props = new Properties();
			try {
				props.load(input);
				for (Entry<?,?> entry : props.entrySet()) {
					context.put(entry.getKey().toString(), entry.getValue().toString());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return context;
	}

	private static String prepareVphome(String[] args) {
		String vphome = System.getProperty("user.home") + "/vente-privee";
		if (args.length > 0) {
			vphome = args[0];
		}
		
		File dir = new File(vphome);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new RuntimeException("Cannot create directory " + vphome);
			}
		}
		
		return vphome;
	}

}
