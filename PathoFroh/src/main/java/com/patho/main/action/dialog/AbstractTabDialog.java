package com.patho.main.action.dialog;

import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Task;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public abstract class AbstractTabDialog extends AbstractDialog {

	protected AbstractTab[] tabs;

	protected AbstractTab selectedTab;

	public boolean initBean(Task task, Dialog dialog) {
		super.initBean(task, dialog);

		for (int i = 0; i < tabs.length; i++) {
			tabs[i].initTab();
		}

		onTabChange(tabs[0]);

		return true;
	}

	public boolean initBean(Dialog dialog) {
		return initBean(null, dialog);
	}

	public void onTabChange(AbstractTab tab) {
		log.debug("Changing tab to " + tab.getName());
		setSelectedTab(tab);
		tab.updateData();
	}

	public void nextTab() {
		log.trace("Next tab");
		for (int i = 0; i < tabs.length; i++) {
			if (tabs[i] == selectedTab) {
				while (i++ <= tabs.length - 1) {
					if (!tabs[i].isDisabled()) {
						onTabChange(tabs[i]);
						return;
					}
				}
			}
		}
	}

	public void previousTab() {
		log.trace("Previous step");
		for (int i = 0; i < tabs.length; i++) {
			if (tabs[i] == selectedTab) {
				while (--i >= 0) {
					if (!tabs[i].isDisabled()) {
						onTabChange(tabs[i]);
						return;
					}
				}
			}
		}
	}

	@Getter
	@Setter
	public abstract class AbstractTab {

		public void updateData() {
			return;
		}

		public boolean initTab() {
			return false;
		}

		protected String name;

		protected String viewID;

		protected String tabName;

		protected String centerInclude;

		protected boolean disabled;

		protected AbstractTab parentTab;

		public boolean isParent() {
			return parentTab != null;
		}
	}

}