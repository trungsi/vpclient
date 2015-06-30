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

	private final VPClientAsync client;

	private final JLabel totalAddedLabel;
	
	private final long start = System.currentTimeMillis();
	
	public VPClientPanel(VPClientAsync client) {
		this.client = client;
		String selectedSale = client.getSelectedSale().getName();
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
		add(stateLabel);
		
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
						stateLabel.setText("Status : " + VPClientPanel.this.client.getState() + ", E.T. : " + (System.currentTimeMillis() - start)/1000);
						button.setEnabled(false);
					}
				});
			}
        }.start();
		
		totalAddedLabel = new JLabel("Cart : 0");
		add(totalAddedLabel, "wrap");

        JPanel addedArticlesPanel = new JPanel(new MigLayout());
        add(addedArticlesPanel, "span,grow");
		
		this.client.register(this);
	}

	@Subscribe
	public void handleVPEvent(final BasketUpdateEvent event) {
		SwingUtilities.invokeLater(new Runnable() {
			//@Override
			public void run() {
				totalAddedLabel.setText("Cart : " + event.getBasket().getBasketSize());
				totalAddedLabel.revalidate();
			}
		});
	
	}
}
