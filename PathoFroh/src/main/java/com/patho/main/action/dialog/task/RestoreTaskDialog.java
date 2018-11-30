package com.patho.main.action.dialog.task;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.GlobalEditViewHandler;
import com.patho.main.action.handler.WorklistViewHandler;
import com.patho.main.common.Dialog;
import com.patho.main.dao.UtilDAO;
import com.patho.main.model.ListItem;
import com.patho.main.model.patient.Task;
import com.patho.main.service.TaskService;
import com.patho.main.ui.interfaces.ListItemsAutoCompete;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope(value = "session")
@Getter
@Setter
@Slf4j
public class RestoreTaskDialog extends AbstractDialog implements ListItemsAutoCompete {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private WorklistViewHandler worklistViewHandler;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private GlobalEditViewHandler globalEditViewHandler;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskService taskService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UtilDAO utilDAO;

	/**
	 * Contains all available attachments
	 */
	private List<ListItem> predefinedListItems;

	/**
	 * commentary for restoring
	 */
	private String commentary;

	/**
	 * True if task was archived
	 */
	private boolean archiveSuccessful;

	public void initAndPrepareBean(Task task) {
		if (initBean(task))
			prepareDialog();
	}

	public boolean initBean(Task task) {

		setPredefinedListItems(utilDAO.getAllStaticListItems(ListItem.StaticList.TASK_RESTORE));

		super.initBean(task, Dialog.TASK_RESTORE);

		return true;
	}

	public void restoreTask() {
		taskService.restoreTask(getTask(), getCommentary());
	}
}
