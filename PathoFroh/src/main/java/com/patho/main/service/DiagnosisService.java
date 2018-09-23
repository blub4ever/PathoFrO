package com.patho.main.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.Transient;

import com.patho.main.config.util.ResourceBundle;
import com.patho.main.common.DiagnosisRevisionType;
import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;
import com.patho.main.util.exception.CustomUserNotificationExcepetion;
import com.patho.main.model.DiagnosisPreset;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.Signature;
import com.patho.main.model.patient.Diagnosis;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Sample;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.DiagnosisRepository;
import com.patho.main.repository.DiagnosisRevisionRepository;
import com.patho.main.repository.MediaRepository;
import com.patho.main.repository.PatientRepository;
import com.patho.main.repository.PrintDocumentRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocument.DocumentType;
import com.patho.main.template.PrintDocument.InitializeToken;
import com.patho.main.ui.task.DiagnosisReportUpdater;
import com.patho.main.util.helper.TaskUtil;
import com.patho.main.util.pdf.PDFGenerator;
import com.patho.main.util.pdf.PDFUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
public class DiagnosisService extends AbstractService {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ResourceBundle resourceBundle;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TransactionTemplate transactionTemplate;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DiagnosisRevisionRepository diagnosisRevisionRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DiagnosisRepository diagnosisRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PatientRepository patientRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private FavouriteListService favouriteListService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MediaRepository mediaRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private AssociatedContactService associatedContactService;

	/**
	 * Updates all diagnosisRevision of the given revisions
	 * 
	 * @param Task
	 *            task
	 */
	public Task synchronizeDiagnosesAndSamples(Task task, boolean save) {
		logger.info("Synchronize all diagnoses of task " + task.getTaskID() + " with samples");

		for (DiagnosisRevision revision : task.getDiagnosisRevisions()) {
			synchronizeDiagnosesAndSamples(revision, task.getSamples(), save);
		}

		if (save)
			return taskRepository.save(task, resourceBundle.get("log.patient.task.diagnosisRevisions.update", task));
		else
			return task;

	}

	/**
	 * Updates a diagnosisRevision with a sample list. Used for adding and removing
	 * samples after initial revision creation.
	 * 
	 * @param diagnosisRevision
	 * @param samples
	 */
	public DiagnosisRevision synchronizeDiagnosesAndSamples(DiagnosisRevision diagnosisRevision, List<Sample> samples,
			boolean save) {
		logger.info("Synchronize diagnosis list with samples");

		List<Diagnosis> diagnosesInRevision = diagnosisRevision.getDiagnoses();

		List<Sample> samplesToAddDiagnosis = new ArrayList<Sample>(samples);

		List<Diagnosis> toRemoveDiagnosis = new ArrayList<Diagnosis>();

		outerLoop: for (Diagnosis diagnosis : diagnosesInRevision) {
			// sample already in diagnosisList, removing from to add array
			for (Sample sample : samplesToAddDiagnosis) {
				if (sample.getId() == diagnosis.getSample().getId()) {
					samplesToAddDiagnosis.remove(sample);
					logger.trace("Sample found, Removing sample " + sample.getId() + " from list.");
					continue outerLoop;
				}
			}
			logger.trace("Diagnosis has no sample, removing diagnosis " + diagnosis.getId());
			// not found within samples, so sample was deleted, deleting
			// diagnosis as well.
			toRemoveDiagnosis.add(diagnosis);
		}

		// removing diagnose if necessary
		for (Diagnosis diagnosis : toRemoveDiagnosis) {
			removeDiagnosis(diagnosis);
		}

		// adding new diagnoses if there are new samples
		for (Sample sample : samplesToAddDiagnosis) {
			logger.trace("Adding new diagnosis for sample " + sample.getId());
			createDiagnosis(diagnosisRevision, sample, false);
		}
		if (save)
			return diagnosisRevisionRepository.save(diagnosisRevision,
					resourceBundle.get("log.patient.task.diagnosisRevision.new", diagnosisRevision));
		else
			return diagnosisRevision;
	}

	/**
	 * Creates a new diagnosis and adds it to the given diagnosisRevision
	 * 
	 * @param revision
	 * @param sample
	 * @return
	 */
	public Task createDiagnosis(DiagnosisRevision revision, Sample sample, boolean save) {
		logger.info("Creating new diagnosis");

		Diagnosis diagnosis = new Diagnosis();
		diagnosis.setSample(sample);
		diagnosis.setParent(revision);
		revision.getDiagnoses().add(diagnosis);

		if (save)
			return taskRepository.save(revision.getTask(),
					resourceBundle.get("log.patient.task.diagnosisRevision.diagnosis.new", diagnosis));
		else
			return revision.getTask();
	}

	/**
	 * Removes a diagnosis from the parent and deletes it.
	 * 
	 * @param diagnosis
	 * @return
	 */
	public void removeDiagnosis(Diagnosis diagnosis) {
		logger.info("Removing diagnosis " + diagnosis.getName());

		diagnosis.setSample(null);

		diagnosis.getParent().getDiagnoses().remove(diagnosis);

		diagnosisRepository.delete(diagnosis,
				resourceBundle.get("log.patient.task.diagnosisRevision.diagnosis.remove", diagnosis));
	}

	/**
	 * Creates a diagnosisRevision, adds it to the given task and creates also all
	 * needed diagnoses
	 * 
	 * @param parent
	 * @param type
	 * @return
	 */
	public Task createDiagnosisRevision(Task task, DiagnosisRevisionType type) {
		return createDiagnosisRevision(task, type,
				TaskUtil.getDiagnosisRevisionName(task.getDiagnosisRevisions(), new DiagnosisRevision("", type)));
	}

	/**
	 * Creates a diagnosisRevision, adds it to the given task and creates also all
	 * needed diagnoses
	 * 
	 * @param parent
	 * @param type
	 * @return
	 */
	public Task createDiagnosisRevision(Task task, DiagnosisRevisionType type, String name) {
		logger.info("Creating new diagnosisRevision");

		DiagnosisRevision diagnosisRevision = new DiagnosisRevision();
		diagnosisRevision.setType(type);
		diagnosisRevision.setSignatureOne(new Signature());
		diagnosisRevision.setSignatureTwo(new Signature());
		diagnosisRevision.setCreationDate(System.currentTimeMillis());
		diagnosisRevision.setName(name);

		return addDiagnosisRevision(task, diagnosisRevision);
	}

	/**
	 * Adds an diagnosis revision to the task
	 * 
	 * @param parent
	 * @param diagnosisRevision
	 */
	@Transactional
	public Task addDiagnosisRevision(Task task, DiagnosisRevision diagnosisRevision) {
		logger.info("Adding diagnosisRevision to task");
		diagnosisRevision.setParent(task);
		diagnosisRevision.setSignatureOne(new Signature());
		diagnosisRevision.setSignatureTwo(new Signature());
		task.getDiagnosisRevisions().add(diagnosisRevision);

		// saving to database
		// diagnosisRevisionRepository.save(diagnosisRevision,
		// resourceBundle.get("log.patient.task.diagnosisRevision.new",
		// diagnosisRevision));

		// creating a diagnosis for every sample
		for (Sample sample : task.getSamples()) {
			task = createDiagnosis(diagnosisRevision, sample, false);
		}

		// saving to database
		return taskRepository.save(task,
				resourceBundle.get("log.patient.task.diagnosisRevision.new", diagnosisRevision));
	}

	/**
	 * Deleting a DiagnosisRevision and all included diagnoese
	 * 
	 * @param revision
	 * @return
	 */
	public Task removeDiagnosisRevision(Task task, DiagnosisRevision revision) throws CustomUserNotificationExcepetion {
		logger.info("Removing diagnosisRevision " + revision.getName());

		if (task.getDiagnosisRevisions().size() > 1) {

			task.getDiagnosisRevisions().remove(revision);

			task = taskRepository.save(revision.getParent(),
					resourceBundle.get("log.patient.task.diagnosisRevision.delete", revision.toString()));

			diagnosisRevisionRepository.delete(revision);
			return task;
		} else {
			throw new CustomUserNotificationExcepetion("growl.error", "growl.diagnosis.delete.last");
		}
	}

	/**
	 * Sets a diangosis as completed
	 * 
	 * @param diagnosisRevision
	 * @param notificationPending
	 */
	public Task approveDiangosis(Task task, DiagnosisRevision diagnosisRevision, boolean notificationPending) {
		diagnosisRevision.setCompletionDate(System.currentTimeMillis());
		diagnosisRevision.setNotificationPending(notificationPending);

		task = taskRepository.save(task,
				resourceBundle.get("log.patient.task.diagnosisRevision.approved", diagnosisRevision.getName()));

		generateDefaultDiagnosisReport(task, diagnosisRevision);
		return task;
	}

	/**
	 * Generated or updates the default diagnosis report for a diagnosis
	 * 
	 * @param task
	 * @param diagnosisRevision
	 * @return
	 */
	public Task generateDefaultDiagnosisReport(Task task, DiagnosisRevision diagnosisRevision) {
		logger.debug("Generating new report");
		return new DiagnosisReportUpdater().updateDiagnosisReportNoneBlocking(task, diagnosisRevision);
	}

	/**
	 * Updates a diagnosis with out a diagnosis prototype
	 * 
	 * @param task
	 * @param diagnosis
	 * @param diagnosisAsText
	 * @param extendedDiagnosisText
	 * @param malign
	 * @param icd10
	 * @return
	 */
	public Task updateDiagnosisWithoutPrototype(Task task, Diagnosis diagnosis, String diagnosisAsText,
			String extendedDiagnosisText, boolean malign, String icd10) {
		diagnosis.setDiagnosisPrototype(null);

		return updateDiagnosis(task, diagnosis, diagnosisAsText, extendedDiagnosisText, malign, icd10);
	}

	/**
	 * Updates a diagnosis with a diagnosispreset.
	 * 
	 * @param task
	 * @param diagnosis
	 * @param preset
	 * @return
	 */
	public Task updateDiagnosisWithPrototype(Task task, Diagnosis diagnosis, DiagnosisPreset preset) {
		logger.debug("Updating diagnosis with prototype");

		diagnosis.setDiagnosisPrototype(preset);

		return updateDiagnosis(task, diagnosis, preset.getDiagnosis(), preset.getExtendedDiagnosisText(),
				preset.isMalign(), preset.getIcd10());
	}

	/**
	 * Updates a diagnosis with the given data. Also updates notification via letter
	 * 
	 * @param task
	 * @param diagnosis
	 * @param diagnosisAsText
	 * @param extendedDiagnosisText
	 * @param malign
	 * @param icd10
	 * @return
	 */
	public Task updateDiagnosis(Task task, Diagnosis diagnosis, String diagnosisAsText, String extendedDiagnosisText,
			boolean malign, String icd10) {
		logger.debug("Updating diagnosis to " + diagnosisAsText);

		diagnosis.setDiagnosis(diagnosisAsText);
		diagnosis.setMalign(malign);
		diagnosis.setIcd10(icd10);

		// only setting diagnosis text if one sample and no text has been
		// added
		// jet
		if (diagnosis.getParent().getText() == null || diagnosis.getParent().getText().isEmpty()) {
			diagnosis.getParent().setText(extendedDiagnosisText);
			logger.debug("Updating revision extended text");
		}

		// updating all contacts on diagnosis change, an determine if the
		// contact should receive a physical case report
		task = associatedContactService.updateNotificationsForPhysicalDiagnosisReport(task);

		task = taskRepository.save(task, resourceBundle.get("log.patient.task.diagnosisRevision.diagnosis.update", task,
				diagnosis, diagnosis.getDiagnosis()));

		return task;
	}
}
