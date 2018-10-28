package com.patho.main.action.dialog.diagnosis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.DiagnosisRevisionType;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.DiagnosisService;
import com.patho.main.util.dialogReturn.DiagnosisPhaseUpdateEvent;
import com.patho.main.util.helper.TaskUtil;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

@Configurable
@Setter
@Getter
public class CreateDiagnosisRevisionDialog extends AbstractDialog<CreateDiagnosisRevisionDialog> {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DiagnosisService diagnosisService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	/**
	 * Name of the new revision
	 */
	private String newRevisionName;

	/**
	 * Type of the new revision
	 */
	private DiagnosisRevisionType newRevisionType;

	/**
	 * If true the names will be autogenerated
	 */
	private boolean generateNames;

	/**
	 * Types of all available revisionTypes to create
	 */
	private DiagnosisRevisionType[] selectableRevisionTypes;

	/**
	 * List containing all old revisions and a new revision. The string contains the
	 * proposed new name
	 */
	private List<DiagnosisRevisionContainer> revisionList = new ArrayList<DiagnosisRevisionContainer>();

	/**
	 * If true the dialog will only allow renaming
	 */
	private int accIndex;

	public CreateDiagnosisRevisionDialog initAndPrepareBean(Task task) {
		if (initBean(task))
			prepareDialog();

		return this;
	}

	/**
	 * If rename is true no new diagnosis revision will be created
	 * 
	 * @param task
	 * @param rename
	 * @return
	 */
	public boolean initBean(Task task) {

		DiagnosisRevisionType[] types = new DiagnosisRevisionType[3];
		types[0] = DiagnosisRevisionType.DIAGNOSIS_REVISION;
		types[1] = DiagnosisRevisionType.DIAGNOSIS_CORRECTION;
		types[2] = DiagnosisRevisionType.DIAGNOSIS_COUNCIL;

		setTask(task);

		setSelectableRevisionTypes(types);
		setNewRevisionType(types[0]);

		setGenerateNames(true);

		setRevisionList(DiagnosisRevisionContainer.factory(task,
				new ArrayList<DiagnosisRevision>(
						Arrays.asList(new DiagnosisRevision(getNewRevisionName(), getNewRevisionType()))),
				isGenerateNames()));

		updateDiagnosisRevisionList();
		updateNewRevision();

		return super.initBean(task, Dialog.DIAGNOSIS_REVISION_CREATE);
	}

	/**
	 * Updates the revision list, will remove or add revision from the task, will
	 * not remove new tasks
	 */
	public void updateDiagnosisRevisionList() {
		setRevisionList(DiagnosisRevisionContainer.factory(task,
				new ArrayList<DiagnosisRevision>(
						Arrays.asList(new DiagnosisRevision(getNewRevisionName(), getNewRevisionType()))),
				isGenerateNames()));
	}

	public void updateNewRevision() {
		setNewRevisionName(TaskUtil.getDiagnosisRevisionName(task.getDiagnosisRevisions(),
				new DiagnosisRevision("", getNewRevisionType())));

		setAccIndex(generateNames ? 0 : -1);
	}

	public void updateIndexAndRevisionList() {
		setAccIndex(generateNames ? 0 : -1);
		updateDiagnosisRevisionList();
	}

	public void updateAll() {
		updateNewRevision();
		updateDiagnosisRevisionList();
	}

	/**
	 * Copies the original name as the new name
	 * 
	 * @param diagnosisRevision
	 */
	public void copyOldNameFromDiagnosisRevision(DiagnosisRevisionContainer diagnosisRevisionContainer) {
		diagnosisRevisionContainer.setNewName(diagnosisRevisionContainer.getName());
	}

	/**
	 * Saves name changes and adds new revision
	 */
	@Transactional
	public void addDiagnosisAndHide() {
		logger.debug("Creating new diagnosis revision " + newRevisionName);

		task = diagnosisService.renameDiagnosisRevisions(task, getRevisionList());
		task = diagnosisService.createDiagnosisRevision(task, newRevisionType, newRevisionName, null);

		super.hideDialog(new DiagnosisPhaseUpdateEvent(task));
	}

	/**
	 * Class for storing new names
	 * 
	 * @author andi
	 *
	 */
	@Getter
	@Setter
	@AllArgsConstructor
	public static class DiagnosisRevisionContainer extends DiagnosisRevision {

		@Delegate
		private DiagnosisRevision revision;

		private String newName;

		public static List<DiagnosisRevisionContainer> factory(Task task, List<DiagnosisRevision> newRevisions,
				boolean generateNames) {
			List<DiagnosisRevisionContainer> result = new ArrayList<DiagnosisRevisionContainer>();

			Set<DiagnosisRevision> allRevsisions = new LinkedHashSet<DiagnosisRevision>(task.getDiagnosisRevisions());

			if (newRevisions != null)
				allRevsisions.addAll(newRevisions);

			for (DiagnosisRevision diagnosisRevision : task.getDiagnosisRevisions()) {
				String name = generateNames ? TaskUtil.getDiagnosisRevisionName(allRevsisions, diagnosisRevision)
						: diagnosisRevision.getName();

				result.add(new DiagnosisRevisionContainer(diagnosisRevision, name));
			}

			return result;
		}
	}

}
