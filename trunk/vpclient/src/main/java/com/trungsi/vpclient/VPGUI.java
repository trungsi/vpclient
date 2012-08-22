/**
 * 
 */
package com.trungsi.vpclient;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

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
		setUpProxy(vphome);
		
		setUpLog4j();
		
		
		JFrame frame = new JFrame("VPClient");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		final JPanel mainPanel = new JPanel(new MigLayout());
		
		JPanel formPanel = new JPanel(new MigLayout());
		mainPanel.add(formPanel, "wrap");
		final JPanel infoPanel = new JPanel(new MigLayout());
		JScrollPane scrollPane = new JScrollPane(infoPanel);
		scrollPane.setPreferredSize(new Dimension(500,500));
		mainPanel.add(scrollPane, "grow");
		
		JLabel loginLabel = new JLabel("Login");
		formPanel.add(loginLabel, "align right");
		final JTextField loginField = new JTextField(getDefault(VPClient.USER, context), 30);
		formPanel.add(loginField, "wrap");
		
		JLabel passwordLabel = new JLabel("Password");
		formPanel.add(passwordLabel, "align right");
		final JPasswordField passwordField = new JPasswordField(getDefault(VPClient.PWD, context), 30);
		formPanel.add(passwordField, "wrap");
		
		JLabel selectedSaleLabel = new JLabel("Selected Sale");
		formPanel.add(selectedSaleLabel, "align right");
		final JTextField selectedSaleField = new JTextField(getDefault(VPClient.SELECTED_SALE, context), 30);
		formPanel.add(selectedSaleField, "wrap");
		
		JLabel selectedCatsLabel = new JLabel("Selected Categories");
		formPanel.add(selectedCatsLabel, "align right");
		final JTextField selectedCatsField = new JTextField(getDefault(VPClient.SELECTED_CATS, context), 30);
		formPanel.add(selectedCatsField, "wrap");
		
		JLabel ignoreSubCatsLabel = new JLabel("Ignore Sub Categories");
		formPanel.add(ignoreSubCatsLabel, "align right");
		final JTextField ignoreSubCatsField = new JTextField(getDefault(VPClient.IGNORE_SUB_CATS, context), 30);
		formPanel.add(ignoreSubCatsField, "wrap");
		
		JLabel womanJeanSizesLabel = new JLabel("Woman Jean Sizes");
		formPanel.add(womanJeanSizesLabel, "align right");
		final JTextField womanJeanSizesField = new JTextField(getDefault(VPClient.WOMAN_JEAN_SIZES, context, "26 |W26|T.36|T. 36"), 30);
		formPanel.add(womanJeanSizesField, "wrap");
		
		JLabel womanShoesSizesLabel = new JLabel("Woman Shoes Sizes");
		formPanel.add(womanShoesSizesLabel, "align right");
		final JTextField womanShoesSizesField = new JTextField(getDefault(VPClient.WOMAN_SHOES_SIZES, context, "37 |T.37"), 30);
		formPanel.add(womanShoesSizesField, "wrap");
		
		JLabel womanLingerieSizesLabel = new JLabel("Woman Lingerie Sizes");
		formPanel.add(womanLingerieSizesLabel, "align right");
		final JTextField womanLingerieSizesField = new JTextField(getDefault(VPClient.WOMAN_LINGERIE_SIZES, context, "95D LOL"), 30);
		formPanel.add(womanLingerieSizesField, "wrap");
		
		JLabel womanClothingSizesLabel = new JLabel("Woman Clothing Sizes");
		formPanel.add(womanClothingSizesLabel, "align right");
		final JTextField womanClothingSizesField = new JTextField(getDefault(VPClient.WOMAN_CLOTHING_SIZES, context, "36 |T.36 (FR)|T.36 |T. 36|34/36| S |.S "), 30);
		formPanel.add(womanClothingSizesField, "wrap");
		
		JLabel girlShoesSizesLabel = new JLabel("Girl Shoes Sizes");
		formPanel.add(girlShoesSizesLabel, "align right");
		final JTextField girlShoesSizesField = new JTextField(getDefault(VPClient.GIRL_SHOES_SIZES, context, "23 |T.23|T. 23"), 30);
		formPanel.add(girlShoesSizesField, "wrap");
		
		JLabel girlClothingSizesLabel = new JLabel("Girl Clothing Sizes");
		formPanel.add(girlClothingSizesLabel, "align right");
		final JTextField girlClothingSizesField = new JTextField(getDefault(VPClient.GIRL_CLOTHING_SIZES, context, "3 ans"), 30);
		formPanel.add(girlClothingSizesField, "wrap");
		
		JLabel manJeanSizesLabel = new JLabel("Man Jean Sizes");
		formPanel.add(manJeanSizesLabel, "align right");
		final JTextField manJeanSizesField = new JTextField(getDefault(VPClient.MAN_JEAN_SIZES, context, "30 |W30|T.30|T.40|T. 40"), 30);
		formPanel.add(manJeanSizesField, "wrap");
		
		JLabel manShoesSizesLabel = new JLabel("Man Shoes Sizes");
		formPanel.add(manShoesSizesLabel, "align right");
		final JTextField manShoesSizesField = new JTextField(getDefault(VPClient.MAN_SHOES_SIZES, context, "40.5| 41 |T.41|T. 41"), 30);
		formPanel.add(manShoesSizesField, "wrap");
		
		JLabel manCostumeSizesLabel = new JLabel("Man Costume Sizes");
		formPanel.add(manCostumeSizesLabel, "align right");
		final JTextField manCostumeSizesField = new JTextField(getDefault(VPClient.MAN_COSTUME_SIZES, context, "M |.M |T.40|T. 40"), 30);
		formPanel.add(manCostumeSizesField, "wrap");
		
		JLabel manClothingSizesLabel = new JLabel("Man Clothing Sizes");
		formPanel.add(manClothingSizesLabel, "align right");
		final JTextField manClothingSizesField = new JTextField(getDefault(VPClient.MAN_CLOTHING_SIZES, context, "M |.M | 38 | 40 |T.40|T. 40"), 30);
		formPanel.add(manClothingSizesField, "wrap");
		
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
									infoPanel.add(clientPanel, "wrap");
									infoPanel.validate();
									infoPanel.revalidate();
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
		
		formPanel.add(button, "align right");
		
		frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
		frame.setSize(500, 800);
		//frame.pack();
		
		frame.setVisible(true);

	}

	private static void setUpLog4j() {
		PropertyConfigurator.configure(VPGUI.class.getResourceAsStream("/log4j.properties"));
	}

	private static void setUpProxy(String vphome) {
		String proxyFileName = vphome + "/proxy.properties";
		File proxyFile = new File(proxyFileName);
		if (proxyFile.exists()) {
			Properties props = new Properties();
			try {
				props.load(new FileInputStream(proxyFile));
				System.getProperties().putAll(props);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Properties props = new Properties();
			props.setProperty(MyHtmlUnitDriver.HTTP_PROXY_HOST, "");
			props.setProperty(MyHtmlUnitDriver.HTTP_PROXY_PORT, "");
			props.setProperty(MyHtmlUnitDriver.HTTP_PROXY_USERNAME, "");
			props.setProperty(MyHtmlUnitDriver.HTTP_PROXY_PASSWORD, "");
			
			try {
				props.store(new FileOutputStream(proxyFile), "Auto generated file");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
