package com.googlecode.lanterna.gui2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.input.KeyStroke;

/**
 * This is a modified implementation of a combo box that allows for multiple item selection.
 * 
 * @param <V> Type to use for the items in the combo box
 * @author rxdps93
 */
public class ComboCheckList<V> extends AbstractInteractableComponent<ComboCheckList<V>> {

	/**
	 * Listener interface that can be used to catch user events on the combo check list
	 */
	public interface Listener {

		/**
		 * This method is called whenever the user checks or unchecks an item
		 * @param index the index of the item the user interacted with
		 * @param checked the new status of the item
		 */
		void onStatusChange(int index, boolean checked);
	}

	private final List<V> items;
	private final List<Boolean> itemStatus;
	private final List<Listener> listeners;

	private PopupWindow popupWindow;

	private boolean dropDownFocused;
	private int dropDownNumberOfRows;

	/**
	 * Creates a new {@code ComboCheckList} initialized with no items. By default 10 items will be
	 * displayed at once, more than that and there will be a scroll bar.
	 */
	public ComboCheckList() {

		this.items = new ArrayList<V>();
		this.itemStatus = new ArrayList<Boolean>();
		this.listeners = new CopyOnWriteArrayList<Listener>();
		this.popupWindow = null;
		this.dropDownFocused = true;
		this.dropDownNumberOfRows = 10;
	}

	/**
	 * Adds a new item to the list, at the end. It is unchecked.
	 * @param item Item to add to the list
	 * @return Itself
	 */
	public synchronized ComboCheckList<V> addItem(V item) {
		return this.addItem(item, false);
	}

	/**
	 * Adds a new item to the list, at the end with the specified checked state.
	 * @param item Item to add to the list
	 * @param checkedState If <code>true</code>, the new item will be initially checked
	 * @return Itself
	 */
	public synchronized ComboCheckList<V> addItem(V item, boolean checkedState) {
		items.add(item);
		itemStatus.add(checkedState);
		invalidate();
		return this;
	}

	/**
	 * Removes all items from the combo checklist
	 * @return Itself
	 */
	public synchronized ComboCheckList<V> clearItems() {
		items.clear();
		itemStatus.clear();
		invalidate();
		return this;
	}

	/**
	 * Removes a particular item from the component if it is present, otherwise does nothing.
	 * @param item Item to remove from the component
	 * @return Itself
	 */
	public synchronized ComboCheckList<V> removeItem(V item) {
		int index = items.indexOf(item);
		if (index == -1) {
			return this;
		}
		return removeItem(index);
	}

	/**
	 * Removes an item from the component at a particular index
	 * @param index Index of the item to remove
	 * @return Itself
	 * @throws IndexOutOfBoundsException if the index is out of range
	 */
	public synchronized ComboCheckList<V> removeItem(int index) {
		items.remove(index);
		itemStatus.remove(index);
		invalidate();
		return this;
	}

	/**
	 * Updates the component so the item at the particular index is swapped out with the supplied value in the
	 * {@code item} parameter
	 * @param index Index of the item to swap out
	 * @param item Item to replace with
	 * @return Itself
	 */
	public synchronized ComboCheckList<V> setItem(int index, V item) {
		if (item == null) {
			throw new IllegalArgumentException("Cannot add null elements to a ComboCheckList");
		}
		items.set(index, item);
		invalidate();
		return this;
	}

	/**
	 * Returns {@code true} if the given item is currently checked. If the item does not exist it will return {@code null}.
	 * @param item Item to check the status of
	 * @return {@code true} if checked, {@code false} if unchecked, {@code null} otherwise
	 */
	public synchronized Boolean isChecked(V item) {
		int index = items.indexOf(item);
		if (index == -1)
			return null;
		return itemStatus.get(index);
	}

	/**
	 * Returns {@code true} if the item at the given index is currently checked.
	 * @param index Index of the item to return the status of
	 * @return {@code true} if checked, {@code false} if unchecked, {@code null} if the index is out of bounds
	 */
	public synchronized Boolean isChecked(int index) {
		if (index < 0 || index >= itemStatus.size())
			return null;

		return itemStatus.get(index);
	}

	/**
	 * Checks or unchecks the given object based on the provided boolean
	 * @param object Item to change the status of
	 * @param checked The status to assign to the object
	 * @return Itself
	 */
	public synchronized ComboCheckList<V> setChecked(V object, boolean checked) {
		int index = items.indexOf(object);
		if (index != -1) {
			setChecked(index, checked);
		}
		return self();
	}

	/**
	 * Checks or unchecks the object at the given index
	 * @param index The index of the object to update
	 * @param checked the status to apply
	 */
	private void setChecked(final int index, final boolean checked) {
		itemStatus.set(index, checked);
		runOnGUIThreadIfExistsOtherwiseRunDirect(new Runnable() {
			@Override
			public void run() {
				for (Listener listener : listeners) {
					listener.onStatusChange(index, checked);
				}
			}
		});
	}

	/**
	 * Returns a list of all the items checked in the list
	 * @return a list containing each checked item
	 */
	public synchronized List<V> getCheckedItems() {
		List<V> result = new ArrayList<V>();
		for (int i = 0; i < itemStatus.size(); i++) {
			if (itemStatus.get(i)) {
				result.add(getItem(i));
			}
		}
		return result;
	}

	/**
	 * Returns the number of items in the list
	 * @return the number of items in the list
	 */
	public synchronized int getItemCount() {
		return items.size();
	}

	/**
	 * Returns the item at the given index, null if no item is present
	 * @param index the index of the item to retrieve
	 * @return the item, otherwise null
	 */
	public synchronized V getItem(int index) {
		return items.get(index);
	}

	/**
	 * Returns the status text that is displayed in the collapsed component
	 * @return the status text that is displayed in the collapsed component
	 */
	public String getText() {
		return String.format("%d of %d checked", getCheckedItems().size(), getItemCount());
	}

    /**
     * Returns {@code true} if the users input focus is currently on the drop-down button of the combo check list, so that
     * pressing enter would trigger the popup window. This is generally used by renderers only.
     * @return {@code true} if the input focus is on the drop-down "button" of the combo check list
     */
	public boolean isDropDownFocused() {
		return dropDownFocused;
	}

    /**
     * Returns the number of items to display in drop down at one time, if there are more items in the model there will
     * be a scrollbar to help the user navigate. If this returns 0, the component will always grow to show all items in
     * the list, which might cause undesired effects if you put really a lot of items into the combo box.
     *
     * @return Number of items (rows) that will be displayed in the component, or 0 if the component will always grow to
     * accommodate
     */
	public int getDropDownNumberOfRows() {
		return dropDownNumberOfRows;
	}

    /**
     * Sets the number of items to display in drop down at one time, if there are more items in the model there will
     * be a scrollbar to help the user navigate. Use this method if your components have large models that fills up
     * the whole screen. Set it to 0 if you don't want to limit the number.
     * @param dropDownNumberOfRows Max number of items (rows) to display at one time in the component
     */
	public void setDropDownNumberOfRows(int dropDownNumberOfRows) {
		this.dropDownNumberOfRows = dropDownNumberOfRows;
	}

    /**
     * Adds a new listener to the {@code ComboCheckList} that will be called on certain user actions
     * @param listener Listener to attach to this {@code ComboCheckList}
     * @return Itself
     */
	public ComboCheckList<V> addListener(Listener listener) {
		if (listener != null && !listeners.contains(listener)) {
			listeners.add(listener);
		}
		return this;
	}

    /**
     * Removes a listener from this {@code ComboCheckList} so that if it had been added earlier, it will no longer be
     * called on user actions
     * @param listener Listener to remove from this {@code ComboCheckList}
     * @return Itself
     */
	public ComboCheckList<V> removeListener(Listener listener) {
		listeners.remove(listener);
		return this;
	}

	@Override
	protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
		dropDownFocused = true;
	}

	@Override
	protected synchronized void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
		if(popupWindow != null) {
			popupWindow.close();
			popupWindow = null;
		}
	}

	@Override
	protected InteractableRenderer<ComboCheckList<V>> createDefaultRenderer() {
		return new DefaultComboCheckListRenderer<V>();
	}

	@Override
	public synchronized Result handleKeyStroke(KeyStroke keyStroke) {
		switch (keyStroke.getKeyType()) {
		case ArrowDown:
			if (popupWindow != null) {
				popupWindow.checkList.handleKeyStroke(keyStroke);
				return Result.HANDLED;
			}
			return Result.MOVE_FOCUS_DOWN;

		case ArrowUp:
			if (popupWindow != null) {
				popupWindow.checkList.handleKeyStroke(keyStroke);
				return Result.HANDLED;
			}
			return Result.MOVE_FOCUS_UP;

		case PageUp:
		case PageDown:
		case Home:
		case End:
			if (popupWindow != null) {
				popupWindow.checkList.handleKeyStroke(keyStroke);
				return Result.HANDLED;
			}
			break;
			
		case Character:
			if (keyStroke.getCharacter().equals(' ')) {
				if (popupWindow != null) {
					popupWindow.checkList.handleInput(keyStroke);
					invalidate();
				} else {
					popupWindow = new PopupWindow();
					popupWindow.setPosition(toGlobal(new TerminalPosition(0, 1)));
					((WindowBasedTextGUI) getTextGUI()).addWindow(popupWindow);
				}
			}
			break;
			
		case Enter:
			if (popupWindow != null) {
				popupWindow.checkList.handleInput(keyStroke);
				invalidate();
			} else {
				popupWindow = new PopupWindow();
				popupWindow.setPosition(toGlobal(new TerminalPosition(0, 1)));
				((WindowBasedTextGUI) getTextGUI()).addWindow(popupWindow);
			}
			break;

		case Escape:
			if (popupWindow != null) {
				closePopupWindow();
				return Result.HANDLED;
			}
			break;

		default:
		}
		return super.handleKeyStroke(keyStroke);
	}
	
	private void closePopupWindow() {
		
		for (V item : items) {
			setChecked(item, popupWindow.checkList.isChecked(item));
		}
		
		popupWindow.close();
		popupWindow = null;
	}

	private class PopupWindow extends BasicWindow {

		private final CheckBoxList<V> checkList;

		public PopupWindow() {
			setHints(Arrays.asList(Hint.NO_FOCUS, Hint.FIXED_POSITION));
			checkList = new CheckBoxList<V>(ComboCheckList.this.getSize().withRows(getItemCount()));
			for (int i = 0; i < getItemCount(); i++) {
				V item = items.get(i);
				boolean state = itemStatus.get(i);
				checkList.addItem(item, state);
			}
			TerminalSize dropDownListPreferredSize = checkList.getPreferredSize();
			if (dropDownNumberOfRows > 0) {
				checkList.setPreferredSize(dropDownListPreferredSize.withRows(
						Math.min(dropDownNumberOfRows, dropDownListPreferredSize.getRows())));
			}
			setComponent(checkList);
		}
		
		@Override
		public synchronized Theme getTheme() {
			return ComboCheckList.this.getTheme();
		}
	}
	
    /**
     * Helper interface that doesn't add any new methods but makes coding new combo check list renderers a little bit more clear
     */
	public static abstract class ComboCheckListRenderer<V> implements InteractableRenderer<ComboCheckList<V>> {
		
	}
	
    /**
     * This class is the default renderer implementation which will be used unless overridden. The combochecklist is rendered
     * like a text box with an arrow point down to the right of it, which can receive focus and triggers the popup.
     * @param <V> Type of items in the combo check list
     */
	public static class DefaultComboCheckListRenderer<V> extends ComboCheckListRenderer<V> {

        /**
         * Default constructor
         */
		public DefaultComboCheckListRenderer() {
			
		}
		
		@Override
		public TerminalPosition getCursorLocation(ComboCheckList<V> component) {
			if (component.isDropDownFocused()) {
				if (component.getThemeDefinition().isCursorVisible()) {
					return new TerminalPosition(component.getSize().getColumns() - 1, 0);
				} else {
					return null;
				}
			} else {
				return null;
			}
		}

		@Override
		public TerminalSize getPreferredSize(ComboCheckList<V> component) {
            TerminalSize size = TerminalSize.ONE.withColumns(
                    (component.getItemCount() == 0 ? TerminalTextUtils.getColumnWidth(component.getText()) : 0) + 2);
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized(component) {
                for(int i = 0; i < component.getItemCount(); i++) {
                    V item = component.getItem(i);
                    size = size.max(new TerminalSize(
                    		Math.max(TerminalTextUtils.getColumnWidth(item.toString()),
                    				TerminalTextUtils.getColumnWidth(component.getText())) + 2 + 3, 1));   // +3 to make up for checkbox space requirements
                }
            }
            return size;
		}

		@Override
		public void drawComponent(TextGUIGraphics graphics, ComboCheckList<V> component) {
			ThemeDefinition themeDefinition = component.getThemeDefinition();
			graphics.applyThemeStyle(themeDefinition.getNormal());
			
			graphics.fill(' ');
			int textArea = graphics.getSize().getColumns() - 2;
			
			String textToDraw = TerminalTextUtils.fitString(component.getText(), 0, textArea);
			graphics.putString(0, 0, textToDraw);
			graphics.setCharacter(textArea, 0, themeDefinition.getCharacter("POPUP_SEPARATOR", Symbols.SINGLE_LINE_VERTICAL));
			if (component.isFocused() && component.isDropDownFocused()) {
				graphics.applyThemeStyle(themeDefinition.getSelected());
			}
			graphics.setCharacter(textArea + 1, 0, themeDefinition.getCharacter("POPUP", Symbols.TRIANGLE_DOWN_POINTING_BLACK));
		}
		
	}

}
