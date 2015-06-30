/**
 * 
 */
package com.trungsi.vpclient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
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
		
		
		JFrame frame = new JFrame("Vente Privée Client");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		final JPanel mainPanel = new JPanel(new MigLayout());
		
		JPanel formPanel = new JPanel(new MigLayout());
		mainPanel.add(formPanel, "wrap");


		final JPanel infoPanel = new JPanel(new MigLayout());
		JScrollPane scrollPane = new JScrollPane(infoPanel);
		scrollPane.setPreferredSize(new Dimension(1000,800));
		mainPanel.add(scrollPane, "grow");

        JPanel accountPanel = new JPanel(new MigLayout());
        formPanel.add(accountPanel, "span, grow");

		JLabel loginLabel = new JLabel("Login");
        accountPanel.add(loginLabel, "align right");
		final JTextField loginField = new JTextField(getDefault(Context.USER, context), 15);
		//formPanel.add(loginField, "wrap");

        accountPanel.add(loginField);

		JLabel passwordLabel = new JLabel("Password");
        accountPanel.add(passwordLabel, "align right");
		final JPasswordField passwordField = new JPasswordField(getDefault(Context.PWD, context), 15);
        //accountPanel.add(passwordField, "wrap");
        accountPanel.add(passwordField);
        final JLabel basketLabel = new JLabel("Basket: 0");
        accountPanel.add(basketLabel);

		JLabel selectedSaleLabel = new JLabel("Selected Sale");
		formPanel.add(selectedSaleLabel, "align right");
		//final JTextField selectedSaleField = new JTextField(getDefault(VPClient.SELECTED_SALE, context), 30);
		
		final DefaultComboBoxModel<Sale> aModel = new DefaultComboBoxModel<>(
									new Sale[] {new Sale("Please select a sale", "", "")});
		final JComboBox<Sale> selectedSaleList = new JComboBox<>(aModel);
		selectedSaleList.setMaximumRowCount(20);
		selectedSaleList.setRenderer(new ListCellRenderer<Sale>() {
			final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

			@Override
			public Component getListCellRendererComponent(
					JList<? extends Sale> list,
					Sale value, int index, boolean isSelected,
					boolean cellHasFocus) {
				String text = "<html><b>" +
						value.getName() + "</b>";
				if (!cellHasFocus) {
                    text += "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i>" + value.getDatesSale() + "</i>";
                }
						//(!list.isShowing() ? "" : "<br/>" + sale.get("dateSales")) +
				text +=	"</html>";
				Component comp = defaultRenderer.getListCellRendererComponent(list, 
						text, index, isSelected, cellHasFocus);
				
				if (index%2 == 0 && !isSelected)
					comp.setBackground(Color.WHITE);
				
				return comp;
			}
		});
		
		//selectedSaleLabel.sets
		formPanel.add(selectedSaleList);
		final JButton loadSalesButton = new JButton("Load Sales");
		loadSalesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadSalesButton.setEnabled(false);
				new Thread() {
					public void run() {
						Context context = new Context();
						context.put(Context.USER, loginField.getText());
						context.put(Context.PWD, new String(passwordField.getPassword()));
						
						VPClientAsync vpClient = new VPClientAsync(context);
						final List<Sale> salesList = vpClient.getSalesList();
						
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								for (Sale sale : salesList) {
									aModel.addElement(sale);
								}
								loadSalesButton.setEnabled(true);
                                selectedSaleList.validate();
							}
						});

                        final int basketSize = vpClient.getBasketSize();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                basketLabel.setText("Basket : " + basketSize);
                            }
                        });
					}
				}.start();
			}
		});
		formPanel.add(loadSalesButton, "wrap");
		
		JLabel selectedCatsLabel = new JLabel("Selected Categories");
		formPanel.add(selectedCatsLabel, "align right");
		final JTextField selectedCatsField = new JTextField(getDefault(Context.SELECTED_CATS, context), 30);
		formPanel.add(selectedCatsField, "wrap");
		
		JLabel ignoreSubCatsLabel = new JLabel("Ignore Sub Categories");
		formPanel.add(ignoreSubCatsLabel, "align right");
		final JTextField ignoreSubCatsField = new JTextField(getDefault(Context.IGNORE_SUB_CATS, context), 30);
		formPanel.add(ignoreSubCatsField, "wrap");

        JLabel exclusiveArticlesLabel = new JLabel("Exclusive articles");
        formPanel.add(exclusiveArticlesLabel, "align right");
        final JTextField exclusiveArticlesField = new JTextField(getDefault(Context.EXCLUSIVE_ARTICLES, context), 30);
        formPanel.add(exclusiveArticlesField, "wrap");

        JLabel womanJeanSizesLabel = new JLabel("Woman Jean Sizes");
		formPanel.add(womanJeanSizesLabel, "align right");
		final JTextField womanJeanSizesField = new JTextField(getDefault(Context.WOMAN_JEAN_SIZES, context, "26 |W26|T.36|T. 36"), 30);
		formPanel.add(womanJeanSizesField, "wrap");
		
		JLabel womanShoesSizesLabel = new JLabel("Woman Shoes Sizes");
		formPanel.add(womanShoesSizesLabel, "align right");
		final JTextField womanShoesSizesField = new JTextField(getDefault(Context.WOMAN_SHOES_SIZES, context, "37 |T.37"), 30);
		formPanel.add(womanShoesSizesField, "wrap");
		
		JLabel womanLingerieSizesLabel = new JLabel("Woman Lingerie Sizes");
		formPanel.add(womanLingerieSizesLabel, "align right");
		final JTextField womanLingerieSizesField = new JTextField(getDefault(Context.WOMAN_LINGERIE_SIZES, context, "95D LOL"), 30);
		formPanel.add(womanLingerieSizesField, "wrap");
		
		JLabel womanShirtSizesLabel = new JLabel("Woman Shirt Sizes");
		formPanel.add(womanShirtSizesLabel, "align right");
		final JTextField womanShirtSizesField = new JTextField(getDefault(Context.WOMAN_SHIRT_SIZES, context, "T. 34|T.34|T. 36|T.36| XS "), 30);
		formPanel.add(womanShirtSizesField, "wrap");
		
		JLabel womanClothingSizesLabel = new JLabel("Woman Clothing Sizes");
		formPanel.add(womanClothingSizesLabel, "align right");
		final JTextField womanClothingSizesField = new JTextField(getDefault(Context.WOMAN_CLOTHING_SIZES, context, "34 |T.34 (FR)|T.34 |T. 34|34/36| S |.S "), 30);
		formPanel.add(womanClothingSizesField, "wrap");
		
		JLabel girlShoesSizesLabel = new JLabel("Girl Shoes Sizes");
		formPanel.add(girlShoesSizesLabel, "align right");
		final JTextField girlShoesSizesField = new JTextField(getDefault(Context.GIRL_SHOES_SIZES, context, "26 |T.26|T. 26"), 30);
		formPanel.add(girlShoesSizesField, "wrap");
		
		JLabel girlClothingSizesLabel = new JLabel("Girl Clothing Sizes");
		formPanel.add(girlClothingSizesLabel, "align right");
		final JTextField girlClothingSizesField = new JTextField(getDefault(Context.GIRL_CLOTHING_SIZES, context, "4 ans"), 30);
		formPanel.add(girlClothingSizesField, "wrap");
		
		JLabel manJeanSizesLabel = new JLabel("Man Jean Sizes");
		formPanel.add(manJeanSizesLabel, "align right");
		final JTextField manJeanSizesField = new JTextField(getDefault(Context.MAN_JEAN_SIZES, context, "30 |W30|T.30|T.40|T. 40"), 30);
		formPanel.add(manJeanSizesField, "wrap");
		
		JLabel manShoesSizesLabel = new JLabel("Man Shoes Sizes");
		formPanel.add(manShoesSizesLabel, "align right");
		final JTextField manShoesSizesField = new JTextField(getDefault(Context.MAN_SHOES_SIZES, context, "40.5| 41 |T.41|T. 41"), 30);
		formPanel.add(manShoesSizesField, "wrap");
		
		JLabel manCostumeSizesLabel = new JLabel("Man Costume Sizes");
		formPanel.add(manCostumeSizesLabel, "align right");
		final JTextField manCostumeSizesField = new JTextField(getDefault(Context.MAN_COSTUME_SIZES, context, "M |.M |T.40|T. 40"), 30);
		formPanel.add(manCostumeSizesField, "wrap");
		
		JLabel manShirtSizesLabel = new JLabel("Man Shirt Sizes");
		formPanel.add(manShirtSizesLabel, "align right");
		final JTextField manShirtSizesField = new JTextField(getDefault(Context.MAN_SHIRT_SIZES, context, "T.39|T. 39| M "), 30);
		formPanel.add(manShirtSizesField, "wrap");
		
		JLabel manClothingSizesLabel = new JLabel("Man Clothing Sizes");
		formPanel.add(manClothingSizesLabel, "align right");
		final JTextField manClothingSizesField = new JTextField(getDefault(Context.MAN_CLOTHING_SIZES, context, "M |.M | 38 | 40 |T.40|T. 40"), 30);
		formPanel.add(manClothingSizesField, "wrap");
		
		JButton button = new JButton("Start");
		button.addActionListener(new ActionListener() {
			//@Override
			public void actionPerformed(ActionEvent e) {
				new Thread() {
					public void run() {
						Context context = new Context();
						context.put(Context.USER, loginField.getText());
						context.put(Context.PWD, new String(passwordField.getPassword()));
						Sale selectedSale = (Sale) selectedSaleList.getSelectedItem();
						/*context.put(Context.SELECTED_SALE, selectedSale.get("name"));
						context.put(Context.SELECTED_SALE_DATE, selectedSale.get("dateSales"));
						context.put(Context.SELECTED_SALE_LINK, selectedSale.get("link"));*/
						
						context.put(Context.SELECTED_CATS, selectedCatsField.getText());
						context.put(Context.IGNORE_SUB_CATS, ignoreSubCatsField.getText());
                        context.put(Context.EXCLUSIVE_ARTICLES, exclusiveArticlesField.getText());

						context.put(Context.WOMAN_JEAN_SIZES, womanJeanSizesField.getText());
						context.put(Context.WOMAN_SHOES_SIZES, womanShoesSizesField.getText());
						context.put(Context.WOMAN_LINGERIE_SIZES, womanLingerieSizesField.getText());
						context.put(Context.WOMAN_SHIRT_SIZES, womanShirtSizesField.getText());
						context.put(Context.WOMAN_CLOTHING_SIZES, womanClothingSizesField.getText());
						context.put(Context.GIRL_SHOES_SIZES, girlShoesSizesField.getText());
						context.put(Context.GIRL_CLOTHING_SIZES, girlClothingSizesField.getText());
						context.put(Context.MAN_JEAN_SIZES, manJeanSizesField.getText());
						context.put(Context.MAN_SHOES_SIZES, manShoesSizesField.getText());
						context.put(Context.MAN_COSTUME_SIZES, manCostumeSizesField.getText());
						context.put(Context.MAN_SHIRT_SIZES, manShirtSizesField.getText());
						context.put(Context.MAN_CLOTHING_SIZES, manClothingSizesField.getText());
						
						//context.put(VPClient.P, value)
						final VPClientAsync client = new VPClientAsync(context);
						client.start(selectedSale);
						
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
						} catch (InvocationTargetException|InterruptedException e) {
							e.printStackTrace();
						}
					}
				}.start();
				
			}
		});
		
		formPanel.add(button, "align right");
		
		frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
		frame.setSize(1000, 800);
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
			} catch (Exception e) {
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
			} catch (Exception e) {
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
	
	public static Context loadContext(String vphome) {
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
		
		Context context = new Context();
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

        System.out.println(context);
        return context;
	}

	private static String prepareVphome(String[] args) {
		String vphome = System.getProperty("user.home") + "/vente-privee";
        System.out.println(vphome);

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
