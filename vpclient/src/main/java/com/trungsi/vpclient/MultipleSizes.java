/**
 * 
 */
package com.trungsi.vpclient;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * @author trungsi
 *
 */
public class MultipleSizes implements Sizes {

	private WebElement selectElems;
	private Select select;
	private List<String> sizes;
	
	public MultipleSizes(WebElement selectElems) {
		this.selectElems = selectElems;
		select = new Select(this.selectElems);
		System.out.println(select.getOptions());
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Size> iterator() {
		Iterator<WebElement> iter = select.getOptions().iterator();
		return new Iterator<Size>() {

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public Size next() {
				return new OptionSize(iter.next());
			}};
	}

	/* (non-Javadoc)
	 * @see com.trungsi.vpclient.Sizes#getSizeValues()
	 */
	@Override
	public List<String> getSizeValues() {
		if (sizes == null) {
			initSize();
		}
		return sizes;
	}

	private void initSize() {
		sizes = select.getOptions().stream().map(option -> getOptionText(option)).collect(Collectors.toList());
	}

	private String getOptionText(WebElement optionElem) {
		return optionElem.getText();
	}
	
	class OptionSize implements Size {

		private WebElement optionElem;
		public OptionSize(WebElement optionElem) {
			this.optionElem = optionElem;
		}

		@Override
		public void select() {
			select.selectByValue(optionElem.getAttribute("value"));
		}

		@Override
		public String getValue() {
			return optionElem.getText();
		}
		
	}
}
