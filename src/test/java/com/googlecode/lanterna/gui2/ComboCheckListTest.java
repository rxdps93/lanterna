package com.googlecode.lanterna.gui2;

import java.io.IOException;
import java.util.Arrays;

public class ComboCheckListTest extends TestBase {

	public static void main(String[] args) throws IOException, InterruptedException {
		new ComboCheckListTest().run(args);
	}
	
	@Override
	public void init(WindowBasedTextGUI textGUI) {
		
		final BasicWindow window = new BasicWindow("ComboCheckListTest");
		Panel mainPanel = new Panel();
		
		final ComboCheckList<String> comboList = new ComboCheckList<String>();
		
		for (String item : Arrays.asList("Alabama", "Michigan", "Wisconsin", "Idaho",
				"Oregon", "Iowa", "Kansas", "Texas", "North Carolina")) {
			comboList.addItem(item);
		}
		
		mainPanel.addComponent(Panels.horizontal(comboList.withBorder(Borders.singleLine("Test"))));
		
		mainPanel.addComponent(new Button("OK", new Runnable() {
			@Override
			public void run() {
				System.out.println(comboList.getCheckedItems().toString());
				window.close();
			}
		}));
		window.setComponent(mainPanel);
		textGUI.addWindow(window);
	}

}
