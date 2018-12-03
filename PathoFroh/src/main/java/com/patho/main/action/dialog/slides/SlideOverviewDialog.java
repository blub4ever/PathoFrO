package com.patho.main.action.dialog.slides;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.miscellaneous.DeleteDialog.DeleteEvent;
import com.patho.main.action.dialog.slides.StainingPhaseExitDialog.StainingPhaseExitData;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.Dialog;
import com.patho.main.model.ListItem;
import com.patho.main.model.patient.Block;
import com.patho.main.model.patient.Sample;
import com.patho.main.model.patient.Slide;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.ListItemRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.SlideService;
import com.patho.main.service.WorkPhaseService;
import com.patho.main.ui.StainingTableChooser;
import com.patho.main.ui.task.TaskStatus;
import com.patho.main.util.dialogReturn.ReloadTaskEvent;
import com.patho.main.util.dialogReturn.StainingPhaseUpdateEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class SlideOverviewDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ListItemRepository listItemRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private WorkPhaseService workPhaseService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private SlideService slideService;

	private ArrayList<StainingTableChooser<?>> flatTaskEntityList;

	/**
	 * Contains all available case histories
	 */
	private List<ListItem> slideCommentary;

	/**
	 * Is used for selecting a chooser from the generated list (generated by task).
	 * It is used to edit the names of the entities by an overlaypannel
	 */
	private StainingTableChooser<?> selectedStainingTableChooser;

	public SlideOverviewDialog initAndPrepareBean(Task task) {
		if (initBean(task))
			prepareDialog();

		return this;
	}

	public boolean initBean(Task task) {
		super.initBean(task, Dialog.SLIDE_OVERVIEW);
		setSlideCommentary(
				listItemRepository.findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.SLIDES, false));
		updateData();
		return true;
	}

	public void updateData() {
		Optional<Task> oTask = taskRepository.findOptionalByIdAndInitialize(getTask().getId(), true, true, true, true,
				true);
		setTask(oTask.get());
		getTask().generateTaskStatus();
		setFlatTaskEntityList(StainingTableChooser.factory(getTask(), false));
	}

	/**
	 * Return handler for various sub dialogs
	 * 
	 * @param event
	 */
	public void onDefaultDialogReturn(SelectEvent event) {
		if (event.getObject() != null)
			// updating stating phase, e.g. after deletion or creation of a new slide
			if (event.getObject() instanceof StainingPhaseUpdateEvent) {
				logger.debug("Update Staining Phase");
				updateData();
				// if staining phase ends, show end staining phase dialog (e.g. after deletion
				// of entities)
				if (workPhaseService.updateStainigPhase(getTask())) {
					logger.debug("Staining Phase completed");
					// open end staining phase dialog
					MessageHandler.executeScript("clickButtonFromBean('dialogContent:stainingPhaseExitFromDialog')");
					// if staining phase does not end, check if restaining phase is entered, show
					// dialog for creating a new diagnosis revision
				} else {
					workPhaseService.startStainingPhase(task);
					updateData();
					// clicking button from backend in order to open dialog on close the select //
					// dialog
					if (TaskStatus.checkIfReStainingFlag(getTask()) && !TaskStatus.checkIfStainingCompleted(getTask())
							&& getTask().getDiagnosisRevisions().size() == 1) {
						logger.debug("Opening dialog for creating a diagnosis revision");
						MessageHandler
								.executeScript("clickButtonFromBean('dialogContent:addDiagnosisRevisionFromDialog')");
					}
				}
				
				return;
				// return of slide dialog 
			} else if (event.getObject() instanceof DeleteEvent) {

				DeleteEvent deleteEvent = (DeleteEvent) event.getObject();

				logger.debug("Deleteing task entity object");

				Task t = slideService.deleteSlideAndPersist((Slide) deleteEvent.getObject());

				onDefaultDialogReturn(
						new SelectEvent(event.getComponent(), event.getBehavior(), new StainingPhaseUpdateEvent(t)));
				return;
				// reload task event, e.g. after version conflict
			} else if (event.getObject() instanceof ReloadTaskEvent) {
				updateData();
				// end staingphase confirmed, close dilaog and forward
			} else if (event.getObject() instanceof StainingPhaseExitData) {
				logger.debug("Staining phase exit dialog return, forwarding to globalEditViewHandler");
				hideDialog(event.getObject());
				// unknown event, close dialog and reload
			} else {
				hideDialog(new ReloadTaskEvent());
			}
	}

	/**
	 * Hides the dialog with an reloadTaskEvent
	 */
	public void hideDialog() {
		hideDialog(new ReloadTaskEvent());
	}

	/**
	 * Saves task changes
	 * 
	 * @param resourcesKey
	 * @param arr
	 */
	public void save(String resourcesKey, Object... arr) {
		logger.debug("Saving task " + getTask().getTaskID());
		setTask(taskRepository.save(getTask(), resourceBundle.get(resourcesKey, arr)));
		getTask().generateTaskStatus();
		setFlatTaskEntityList(StainingTableChooser.factory(getTask(), false));
	}
}
