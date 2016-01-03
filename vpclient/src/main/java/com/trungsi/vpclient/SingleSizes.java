/**
 * 
 */
package com.trungsi.vpclient;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.WebElement;

/**
 * @author trungsi
 *
 */
public class SingleSizes implements Sizes {

	private WebElement productSize;
	private String textSize;
	private List<String> sizes;
	
	public SingleSizes(WebElement productSize) {
		this.productSize = productSize;
		textSize = productSize.getText();
		sizes = Arrays.asList(textSize);
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Size> iterator() {
		Iterator<String> iter = sizes.iterator();
		
		return new Iterator<Size>() {

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public Size next() {
				String size = iter.next();
				return new SingleSize(size);
			}
			
		};
	}

	/* (non-Javadoc)
	 * @see com.trungsi.vpclient.Sizes#getSizeValues()
	 */
	@Override
	public List<String> getSizeValues() {
		return sizes;
	}

	static class SingleSize implements Size {

		private String size;
		
		public SingleSize(String size) {
			this.size = size;
		}

		@Override
		public void select() {
			// TODO Auto-generated method stub
			// do nothing
		}

		@Override
		public String getValue() {
			return size;
		}
		
	}
}
