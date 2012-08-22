/**
 * 
 */
package com.trungsi.vpclient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.google.common.eventbus.Subscribe;

import net.miginfocom.swing.MigLayout;


/**
 * @author dtran091109
 *
 */
public class VPClientPanel extends JPanel {

	private VPClientAsync client;
	private JPanel addedArticlesPanel = new JPanel(new MigLayout());
	
	public VPClientPanel(VPClientAsync client) {
		this.client = client;
		String selectedSale = client.context.get(VPClient.SELECTED_SALE);
		setLayout(new MigLayout());
		JLabel label = new JLabel(selectedSale);
		add(label);
		
		final JButton button = new JButton("Stop");
		add(button);
		button.addActionListener(new ActionListener() {
			//@Override
			public void actionPerformed(ActionEvent e) {
				VPClientPanel.this.client.interrupt();
			}
		});
		
		final JLabel stateLabel = new JLabel("Status : " + client.getState());
		add(stateLabel,"wrap");
		
		new Thread() {
			public void run() {
				
				while(VPClientPanel.this.client.getState() != 
						com.trungsi.vpclient.VPClientAsync.State.INTERRUPTED && 
						VPClientPanel.this.client.getState() != 
						com.trungsi.vpclient.VPClientAsync.State.TERMINATED) {
					SwingUtilities.invokeLater(new Runnable() {
						//@Override
						public void run() {
							stateLabel.setText("Status : " + VPClientPanel.this.client.getState());
						}
					});
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				SwingUtilities.invokeLater(new Runnable() {
					//@Override
					public void run() {
						stateLabel.setText("Status : " + VPClientPanel.this.client.getState());
						button.setEnabled(false);
					}
				});
			};
		}.start();
		
		add(addedArticlesPanel, "span,grow");
		
		this.client.register(this);
	}

	@Subscribe
	public void handleVPEvent(VPEvent event) {
		if (event instanceof AddArticleEvent) {
			final AddArticleEvent addArticleEvent = (AddArticleEvent) event;
			
			SwingUtilities.invokeLater(new Runnable() {
				//@Override
				public void run() {
					addedArticlesPanel.add(new JLabel(addArticleEvent.getText()), "wrap");
				}
			});
		}
	}
}
