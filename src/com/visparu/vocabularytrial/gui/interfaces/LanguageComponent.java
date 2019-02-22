package com.visparu.vocabularytrial.gui.interfaces;

import java.util.ArrayList;
import java.util.List;

import com.visparu.vocabularytrial.model.db.entities.LogItem;

public interface LanguageComponent
{
	
	List<LanguageComponent> instances = new ArrayList<>();
	
	static void repopulateAllLanguages()
	{
		LogItem.enter();
		LanguageComponent.instances.forEach(i -> i.repopulateLanguages());
		LogItem.exit();
	}
	
	void repopulateLanguages();
	
}
