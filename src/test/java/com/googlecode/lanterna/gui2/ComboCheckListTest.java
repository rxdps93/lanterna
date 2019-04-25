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
		
		final ComboCheckList<String> shortText = new ComboCheckList<String>();
		final ComboCheckList<String> mixedText = new ComboCheckList<String>();
		
		for (String item : Arrays.asList("a", "b", "c", "d", "e", "f", "g")) {
			shortText.addItem(item);
		}
		
		for (String item : Arrays.asList("Alabama", "Michigan", "Wisconsin", "Idaho",
		"Oregon", "Iowa", "West Virginia", "Texas", "North Carolina")) {
			mixedText.addItem(item);
		}
		
		mainPanel.addComponent(new Label("Short length list items:"));
		mainPanel.addComponent(Panels.horizontal(
				shortText.withBorder(Borders.singleLine())));
		
		mainPanel.addComponent(new Label("Mixed length list items:"));
		mainPanel.addComponent(Panels.horizontal(
				mixedText.withBorder(Borders.singleLine())));
		
		mainPanel.addComponent(new Button("OK", new Runnable() {
			@Override
			public void run() {
				System.out.println(shortText.getCheckedItems().toString());
				System.out.println(mixedText.getCheckedItems().toString());
				window.close();
			}
		}));
		window.setComponent(mainPanel);
		textGUI.addWindow(window);
	}

}
