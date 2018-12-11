package com.patho.main.model.patient;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.patho.main.common.ContactRole;
import com.patho.main.common.Eye;
import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.common.TaskPriority;
import com.patho.main.config.PathoConfig;
import com.patho.main.model.Accounting;
import com.patho.main.model.AssociatedContact;
import com.patho.main.model.Council;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.Person;
import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.interfaces.DataList;
import com.patho.main.model.interfaces.ID;
import com.patho.main.model.interfaces.LogAble;
import com.patho.main.model.interfaces.Parent;
import com.patho.main.model.interfaces.PatientRollbackAble;
import com.patho.main.model.util.audit.Audit;
import com.patho.main.model.util.audit.AuditAble;
import com.patho.main.model.util.audit.AuditListener;
import com.patho.main.ui.task.TaskStatus;
import com.patho.main.util.helper.TimeUtil;
import com.patho.main.util.hibernate.RootAware;

import lombok.Getter;
import lombok.Setter;

@Entity
@Audited
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
@SequenceGenerator(name = "task_sequencegenerator", sequenceName = "task_sequence")
@Getter
@Setter
@EntityListeners(AuditListener.class)
public class Task
		implements Parent<Patient>, LogAble, PatientRollbackAble<Patient>, DataList, ID, RootAware<Patient>, AuditAble {

	@Transient
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Id
	@GeneratedValue(generator = "task_sequencegenerator")
	@Column(unique = true, nullable = false)
	private long id;

	@Version
	private long version;

	/**
	 * Generated Task ID as String
	 */
	@Column(length = 6)
	private String taskID = "";

	/**
	 * The Patient of the task;
	 */
	@ManyToOne(targetEntity = Patient.class)
	private Patient parent;

	/**
	 * If true the program will provide default names for samples and blocks
	 */
	@Column
	private boolean useAutoNomenclature;

	@Embedded
	private Audit audit;

	/**
	 * The date of the sugery
	 */
	@Column
	private long dateOfSugery = 0;

	/**
	 * Date of reception of the first material
	 */
	@Column
	private long dateOfReceipt = 0;

	/**
	 * Priority of the task
	 */
	@Enumerated(EnumType.ORDINAL)
	private TaskPriority taskPriority;

	/**
	 * The dueDate
	 */
	@Column
	private long dueDate = 0;

	/**
	 * Station�r/ambulant/Extern
	 */
	@Column
	private byte typeOfOperation;

	/**
	 * Details of the case
	 */
	@Column(columnDefinition = "VARCHAR")
	private String caseHistory = "";

	/**
	 * Details of the case
	 */
	@Column(columnDefinition = "VARCHAR")
	private String commentary = "";

	/**
	 * Insurance of the patient
	 */
	@Column(columnDefinition = "VARCHAR")
	private String insurance;

	/**
	 * Ward of the patient
	 */
	@Column
	private String ward = "";

	/**
	 * Ey of the samples right/left/both
	 */
	@Enumerated(EnumType.STRING)
	private Eye eye = Eye.UNKNOWN;

	/**
	 * date of staining completion
	 */
	@Column
	private long stainingCompletionDate = 0;

	/**
	 * Date of diagnosis finalization
	 */
	@Column
	private long diagnosisCompletionDate = 0;

	/**
	 * The date of the completion of the notificaiton.
	 */
	@Column
	private long notificationCompletionDate = 0;

	/**
	 * The date of the finalization.
	 */
	@Column
	private long finalizationDate = 0;

	/**
	 * True if the task can't is completed
	 */
	@Column
	private boolean finalized;

	/**
	 * Unique slide counter is increased for every added slide;
	 */
	private int slideCounter = 0;

	/**
	 * Liste aller Personen die �ber die Diangose informiert werden sollen.
	 */
	// TODO fetch lazy
	@OneToMany(cascade = { CascadeType.REMOVE, CascadeType.DETACH }, fetch = FetchType.EAGER, mappedBy = "task")
	@Fetch(value = FetchMode.SUBSELECT)
	@OrderBy("id ASC")
	@NotAudited
	private Set<AssociatedContact> contacts = new HashSet<AssociatedContact>();

	/**
	 * List with all samples
	 */
	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SUBSELECT)
	@OrderBy("id ASC")
	private List<Sample> samples = new ArrayList<Sample>();

	/**
	 * All diangnoses
	 */
	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@OrderBy("id ASC")
	private Set<DiagnosisRevision> diagnosisRevisions;

	/**
	 * Generated PDFs of this task, lazy
	 */
	@OneToMany()
	@LazyCollection(LazyCollectionOption.TRUE)
	@Fetch(value = FetchMode.SUBSELECT)
	@OrderBy("audit.createdOn DESC")
	private Set<PDFContainer> attachedPdfs = new HashSet<PDFContainer>();

	/**
	 * List of all councils of this task, lazy
	 */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "task")
	@OrderBy("dateOfRequest DESC")
	private Set<Council> councils = new HashSet<Council>();

	/**
	 * List of all favorite Lists in which the task is listed
	 */
	@ManyToMany(fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SUBSELECT)
	@OrderBy("id ASC")
	@NotAudited
	private List<FavouriteList> favouriteLists;

	@OneToOne(fetch = FetchType.LAZY)
	private Accounting accounting;

	/********************************************************
	 * Transient Variables
	 ********************************************************/
	/**
	 * If set to true, this task is shown in the navigation column on the left hand
	 * side, however there are actions to perform or not.
	 */
	@Transient
	private boolean active;

	/**
	 * Contains static list entries for the gui, improves reload speed
	 */
	@Transient
	private TaskStatus taskStatus;

	/********************************************************
	 * Transient Variables
	 ********************************************************/

	public Task() {
	}

	public Task(long id) {
		this.id = id;
	}

	/**
	 * Initializes a task with important values.
	 * 
	 * @param parent
	 */
	public Task(Patient parent) {

		long currentDay = TimeUtil.setDayBeginning(System.currentTimeMillis());
		setDateOfReceipt(currentDay);
		setDueDate(currentDay);
		setDateOfSugery(currentDay);

		// 20xx -2000 = tasknumber
		setParent(parent);
	}

	/********************************************************
	 * Transient
	 ********************************************************/

	@Transient
	public void updateAllNames() {
		updateAllNames(useAutoNomenclature, false);
	}

	@Transient
	public void updateAllNames(boolean useAutoNomenclature, boolean ignoreManuallyNamedItems) {
		getSamples().stream().forEach(p -> p.updateAllNames(useAutoNomenclature, ignoreManuallyNamedItems));
	}

	@Transient
	public AssociatedContact getPrimarySurgeon() {
		return getPrimaryContact(ContactRole.SURGEON, ContactRole.EXTERNAL_SURGEON);
	}

	@Transient
	public AssociatedContact getPrimaryPrivatePhysician() {
		return getPrimaryContact(ContactRole.PRIVATE_PHYSICIAN);
	}

	@Transient
	public AssociatedContact getPrimaryContactAsString(String... contactRole) {
		ContactRole[] role = new ContactRole[contactRole.length];

		for (int i = 0; i < role.length; i++) {
			role[i] = ContactRole.valueOf(contactRole[i]);
		}

		return getPrimaryContact(role);
	}

	/**
	 * Returns a associatedContact marked als primary with the given role.
	 * 
	 * @param contactRole
	 * @return
	 */
	@Transient
	public AssociatedContact getPrimaryContact(ContactRole... contactRole) {
		for (AssociatedContact associatedContact : contacts) {
			for (int i = 0; i < contactRole.length; i++) {
				if (associatedContact.getRole() == contactRole[i])
					return associatedContact;
			}
		}

		return null;
	}

	/**
	 * Creates linear list of all slides of the given task. The StainingTableChosser
	 * is used as holder class in order to offer an option to select the slides by
	 * clicking on a checkbox. Archived elements will not be shown if showArchived
	 * is false.
	 */

	@Transient
	public void generateTaskStatus() {
		logger.debug("Generating taskstatus for " + getTaskID() + " " + hashCode());
		if (getTaskStatus() == null)
			setTaskStatus(new TaskStatus(this));
		else
			getTaskStatus().updateStatus();
	}

	@Transient
	public boolean isListedInFavouriteList(PredefinedFavouriteList... predefinedFavouriteLists) {
		long[] arr = new long[predefinedFavouriteLists.length];
		int i = 0;
		for (PredefinedFavouriteList predefinedFavouriteList : predefinedFavouriteLists) {
			arr[i] = predefinedFavouriteList.getId();
			i++;
		}

		return isListedInFavouriteList(arr);
	}

	@Transient
	public boolean isListedInFavouriteList(long... idArr) {
		for (long id : idArr) {
			if (getFavouriteLists().stream().anyMatch(p -> p.getId() == id))
				return true;
		}
		return false;
	}

	@Transient
	public boolean isListedInFavouriteList(FavouriteList... favouriteLists) {
		long[] arr = new long[favouriteLists.length];
		int i = 0;
		for (FavouriteList favouriteList : favouriteLists) {
			arr[i] = favouriteList.getId();
			i++;
		}

		return isListedInFavouriteList(arr);
	}

	@Override
	@Transient
	public String toString() {
		return "Task: " + getTaskID() + (getId() != 0 ? ", ID: " + getId() : "");
	}

	@Transient
	public boolean isActiveOrActionPending() {
		return isActiveOrActionPending(false);
	}

	/**
	 * Returns true if the task is marked as active or an action is pending. If
	 * activeOnly is true only the active attribute of the task will be evaluated.
	 * 
	 * @param task
	 * @return
	 */
	@Transient
	public boolean isActiveOrActionPending(boolean activeOnly) {
		if (activeOnly)
			return isActive();

		if (isActive())
			return true;

		if (isListedInFavouriteList(PredefinedFavouriteList.StainingList, PredefinedFavouriteList.ReStainingList,
				PredefinedFavouriteList.StayInStainingList, PredefinedFavouriteList.DiagnosisList,
				PredefinedFavouriteList.ReDiagnosisList, PredefinedFavouriteList.StayInDiagnosisList,
				PredefinedFavouriteList.NotificationList, PredefinedFavouriteList.StayInNotificationList))
			return true;

		return false;
	}

	@Transient
	@Override
	public boolean equals(Object obj) {

		if (obj instanceof Task && ((Task) obj).getId() == getId())
			return true;

		return super.equals(obj);
	}

	@Transient
	public boolean containsContact(Person person) {
		if (getContacts() != null)
			return getContacts().stream().anyMatch(p -> p.getPerson().equals(person));
		return false;
	}

	@Transient
	public boolean containsContact(AssociatedContact associatedContact) {
		if (getContacts() != null)
			return getContacts().stream().anyMatch(p -> p.equals(associatedContact));
		return false;
	}

	@Transient
	public int getNextSlideNumber() {
		return ++slideCounter;
	}

	/********************************************************
	 * Transient
	 ********************************************************/

	/********************************************************
	 * Transient Getter/Setter
	 ********************************************************/
	@Transient
	public boolean isMalign() {
		return getDiagnosisRevisions().stream().anyMatch(p -> p.isMalign());
	}

	@Transient
	public Date getDateOfSugeryAsDate() {
		return new Date(getDateOfSugery());
	}

	public void setDateOfSugeryAsDate(Date date) {
		setDateOfSugery(TimeUtil.setDayBeginning(date).getTime());
	}

	@Transient
	public Date getDateOfReceiptAsDate() {
		return new Date(getDateOfReceipt());
	}

	public void setDateOfReceiptAsDate(Date date) {
		setDateOfReceipt(TimeUtil.setDayBeginning(date).getTime());
	}

	@Transient
	public Date getDueDateAsDate() {
		return new Date(getDueDate());
	}

	public void setDueDateAsDate(Date date) {
		setDueDate(TimeUtil.setDayBeginning(date).getTime());
	}

	/**
	 * Returns true if priority is set to TaskPriority.Time
	 */
	@Transient
	public boolean isDueDateSelected() {
		if (getTaskPriority() == TaskPriority.TIME)
			return true;
		return false;
	}

	/**
	 * Sets if the given parameter is true TaskPriority.Time, if false
	 * TaskPriority.NONE
	 * 
	 * @param dueDate
	 */
	public void setDueDateSelected(boolean dueDate) {
		if (dueDate)
			setTaskPriority(TaskPriority.TIME);
		else
			setTaskPriority(TaskPriority.NONE);
	}

	@Transient
	@Override
	public Patient getPatient() {
		return getParent();
	}

	/**
	 * Returns the parent task
	 */
	@Override
	@Transient
	public Task getTask() {
		return this;
	}

	@Override
	@Transient
	public String getPublicName() {
		return getTaskID();
	}

	@Override
	@Transient
	public Patient root() {
		return parent;
	}

	/**
	 * File Repository Base of the corresponding patient
	 */
	@Override
	@Transient
	public File getFileRepositoryBase() {
		return new File(PathoConfig.FileSettings.FILE_REPOSITORY_PATH_TOKEN + String.valueOf(getParent().getId()));
	}
}
