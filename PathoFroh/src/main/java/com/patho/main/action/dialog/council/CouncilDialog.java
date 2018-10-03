package com.patho.main.action.dialog.council;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;

import org.apache.commons.lang3.time.DateUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.patho.main.action.DialogHandlerAction;
import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.action.handler.WorklistViewHandlerAction;
import com.patho.main.common.ContactRole;
import com.patho.main.common.CouncilState;
import com.patho.main.common.DateFormat;
import com.patho.main.common.Dialog;
import com.patho.main.template.PrintDocument.DocumentType;
import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.common.SortOrder;
import com.patho.main.config.PathoConfig;
import com.patho.main.model.BioBank;
import com.patho.main.model.Council;
import com.patho.main.model.ListItem;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.Physician;
import com.patho.main.model.interfaces.DataList;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.CouncilRepository;
import com.patho.main.repository.ListItemRepository;
import com.patho.main.repository.PhysicianRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.CouncilService;
import com.patho.main.service.PDFService;
import com.patho.main.service.PDFService.PDFReturn;
import com.patho.main.template.print.ui.document.AbstractDocumentUi;
import com.patho.main.template.print.ui.document.report.CouncilReportUi;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configurable
@Getter
@Setter
public class CouncilDialog extends AbstractDialog<CouncilDialog> {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ListItemRepository listItemRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PhysicianRepository physicianRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private CouncilService councilService;

	private CouncilRepository councilRepository;
	
	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PDFService pdfService;

	/**
	 * Root node for Tree
	 */
	private TreeNode root;

	/**
	 * Selected Node
	 */
	private TreeNode selectedNode;

	/**
	 * Selected council from councilList
	 */
	private Council selectedCouncil;

	/**
	 * Council nodes
	 */
	private List<TreeNode> councilNodes;

	/**
	 * List of all councils of this tasks
	 */
	private List<Council> councilList;

	/**
	 * List of physician to address a council
	 */
	private List<Physician> physicianCouncilList;

	/**
	 * Transformer for phyisicianCouncilList
	 */
	private DefaultTransformer<Physician> physicianCouncilTransformer;

	/**
	 * List of physicians to sign the request
	 */
	private List<Physician> physicianSigantureList;

	/**
	 * Transformer for physicianSiangotureList
	 */
	private DefaultTransformer<Physician> physicianSigantureListTransformer;

	/**
	 * Contains all available attachments
	 */
	private List<ListItem> attachmentList;

	/**
	 * True if editable
	 */
	private boolean editable;

	private boolean admendSelectedRequestState;

	/**
	 * Initializes the bean and shows the council dialog
	 * 
	 * @param task
	 */
	public CouncilDialog initAndPrepareBean(Task task) {
		if (initBean(task))
			prepareDialog();

		return this;
	}

	/**
	 * Initializes the bean and calles updatePhysicianLists at the end.
	 * 
	 * @param task
	 */
	public boolean initBean(Task task) {

		super.initBean(task, Dialog.COUNCIL);

		// reload task in order to load councils
		update(true);

		setCouncilList(new ArrayList<Council>(getTask().getCouncils()));

		// setting council as default
		if (getCouncilNodes().size() != 0) {
			setSelectedNode(getCouncilNodes().get(0).getChildren().get(0));
			setSelectedCouncil((Council) getCouncilNodes().get(0).getData());
		} else {
			setSelectedNode(null);
			setSelectedCouncil(null);
		}

		updatePhysicianLists();

		setAttachmentList(listItemRepository
				.findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.COUNCIL_ATTACHMENT, false));

		setEditable(task.getTaskStatus().isEditable());

		return true;
	}

	/**
	 * Updates the tree menu und reloads the patient's data
	 * 
	 * @param reloadPatient
	 */
	private void update(boolean reloadTask) {
		if (reloadTask) {
			setTask(taskRepository.findOptionalByIdAndInitialize(task.getId(), true, true, true, true, true).get());
			getTask().generateTaskStatus();
		}

		setRoot(generateTree(task));

		if (getSelectedNode() != null) {
			for (TreeNode node : getCouncilNodes()) {
				if (((Council) node.getData()).equals((getSelectedNode()).getData())) {
					setSelectedNode(node);
					setSelectedCouncil((Council) node.getData());
					return;
				}
			}
		}

		setSelectedNode(null);
		setSelectedCouncil(null);
	}

	private TreeNode generateTree(Task task) {
		TreeNode root = new DefaultTreeNode("Root", null);

		TreeNode taskNode = new DefaultTreeNode("task", task, root);
		taskNode.setExpanded(true);
		taskNode.setSelectable(false);

		councilNodes = new ArrayList<TreeNode>();

		for (Council council : task.getCouncils()) {
			TreeNode councilNode = new DefaultTreeNode("council", council, taskNode);
			councilNode.setExpanded(true);
			councilNode.setSelectable(false);

			councilNodes.add(councilNode);

			TreeNode councilRequestNode = new DefaultTreeNode("council_request", council, councilNode);
			councilRequestNode.setExpanded(true);
			councilRequestNode.setSelectable(true);

			if (council.isSampleShipped()) {
				TreeNode councilshipNode = new DefaultTreeNode("council_ship", council, councilNode);
				councilshipNode.setExpanded(true);
				councilshipNode.setSelectable(true);
			}

			TreeNode councilReturnNode = new DefaultTreeNode("council_reply", council, councilNode);
			councilReturnNode.setExpanded(true);
			councilReturnNode.setSelectable(true);

			TreeNode councilDataNode = new DefaultTreeNode("data_node", council, councilNode);
			councilDataNode.setExpanded(true);
			councilDataNode.setSelectable(true);

			for (PDFContainer container : council.getAttachedPdfs()) {
				TreeNode councilFileNode = new DefaultTreeNode("file_node", container, councilNode);
				councilFileNode.setExpanded(false);
				councilDataNode.setSelectable(true);
			}
		}

		return root;
	}

	public void onNodeSelect() {
		if (getSelectedNode() != null) {
			setSelectedCouncil((Council) getSelectedNode().getData());
		}
	}

	public String getCenterInclude() {
		if (getSelectedNode() != null) {

			switch (getSelectedNode().getType()) {
			case "council_request":
				return "inculde/request.xhtml";
			case "council_ship":
				return "inculde/ship.xhtml";
			case "council_reply":
				return "inculde/reply.xhtml";
			default:
				return "inculde/empty.xhtml";
			}
		}

		return "inculde/empty.xhtml";
	}

	/**
	 * Renews the physician lists
	 */
	public void updatePhysicianLists() {
		// list of physicians which are the counselors
		setPhysicianCouncilList(physicianRepository.findAllByRole(new ContactRole[] { ContactRole.CASE_CONFERENCE },
				true, SortOrder.PRIORITY));
		setPhysicianCouncilTransformer(new DefaultTransformer<Physician>(getPhysicianCouncilList()));

		// list of physicians to sign the request
		setPhysicianCouncilList(physicianRepository.findAllByRole(new ContactRole[] { ContactRole.SIGNATURE }, true,
				SortOrder.PRIORITY));
		setPhysicianSigantureListTransformer(new DefaultTransformer<Physician>(getPhysicianSigantureList()));
	}

	/**
	 * Creates a new council and saves it
	 */
	public void createCouncil() {
		logger.info("Adding new council");
		councilService.createCouncil(getTask());
		update(true);
	}

	public void admendRequestState() {
		setAdmendSelectedRequestState(true);
	}

	public void endRequestState(Council council) {
		logger.debug("Ending request phase");
		councilService.endCouncilRequest(council);
		setAdmendSelectedRequestState(false);
		update(true);
	}

	/**
	 * Handels file upload
	 * 
	 * @param event
	 */
	public void handleFileUpload(FileUploadEvent event) {
		UploadedFile file = event.getFile();
		try {
			logger.debug("Uploadgin to Council: " + getSelectedCouncil().getId());
			PDFReturn res = pdfService.createAndAttachPDF(getSelectedCouncil(), file, DocumentType.COUNCIL_REPLY, "",
					"", true, new File(PathoConfig.FileSettings.FILE_REPOSITORY_PATH_TOKEN
							+ String.valueOf(getSelectedCouncil().getTask().getPatient().getId())));

			MessageHandler.sendGrowlMessagesAsResource("growl.upload.success");
		} catch (IllegalAccessError e) {
			MessageHandler.sendGrowlMessagesAsResource("growl.upload.failed", FacesMessage.SEVERITY_ERROR);
		}

		update(true);
	}

	public void onCouncilStateChange() {
		try {

			save();

			switch (getSelectedCouncil().getCouncilState()) {
			case EditState:
			case ValidetedState:
				logger.debug("EditState selected");
				// removing all fav lists
				removeListFromTask(PredefinedFavouriteList.CouncilLendingMTA,
						PredefinedFavouriteList.CouncilLendingSecretary, PredefinedFavouriteList.CouncilPending,
						PredefinedFavouriteList.CouncilCompleted);
				break;
			case LendingStateMTA:
			case LendingStateSecretary:
				logger.debug("LendingState selected");
				// removing pending and completed state
				removeListFromTask(PredefinedFavouriteList.CouncilPending, PredefinedFavouriteList.CouncilCompleted);
				favouriteListDAO.addReattachedTaskToList(getTask(),
						getSelectedCouncil().getCouncilState() == CouncilState.LendingStateMTA
								? PredefinedFavouriteList.CouncilLendingMTA
								: PredefinedFavouriteList.CouncilLendingSecretary);
				break;
			case PendingState:
				logger.debug("PendingState selected");
				// removing pending and completed state
				removeListFromTask(PredefinedFavouriteList.CouncilLendingMTA,
						PredefinedFavouriteList.CouncilLendingSecretary, PredefinedFavouriteList.CouncilCompleted);
				favouriteListDAO.addReattachedTaskToList(getTask(), PredefinedFavouriteList.CouncilPending);
				break;
			case CompletedState:
				logger.debug("CompletedState selected");
				// removing pending and completed state
				removeListFromTask(PredefinedFavouriteList.CouncilLendingMTA,
						PredefinedFavouriteList.CouncilLendingSecretary, PredefinedFavouriteList.CouncilPending);
				favouriteListDAO.addReattachedTaskToList(getTask(), PredefinedFavouriteList.CouncilCompleted);
				break;
			default:
				break;
			}
		} catch (HistoDatabaseInconsistentVersionException e) {
			onCouncilStateChange();
		}

	}

	public void removeListFromTask(PredefinedFavouriteList... predefinedFavouriteLists)
			throws HistoDatabaseInconsistentVersionException {

		for (PredefinedFavouriteList predefinedFavouriteList : predefinedFavouriteLists) {
			switch (predefinedFavouriteList) {
			case CouncilCompleted:
				if (!getTask().getCouncils().stream().anyMatch(p -> p.getCouncilState() == CouncilState.CompletedState))
					favouriteListDAO.removeReattachedTaskFromList(getTask(), predefinedFavouriteList);
				else
					logger.debug("Not removing from CouncilCompleted list, other councils are in this state");
				break;
			case CouncilPending:
				if (!getTask().getCouncils().stream().anyMatch(p -> p.getCouncilState() == CouncilState.PendingState))
					favouriteListDAO.removeReattachedTaskFromList(getTask(), predefinedFavouriteList);
				else
					logger.debug("Not removing from CouncilPending list, other councils are in this state");
				break;
			case CouncilLendingMTA:
			case CouncilLendingSecretary:
				if (!getTask().getCouncils().stream().anyMatch(p -> p.getCouncilState() == CouncilState.LendingStateMTA
						|| p.getCouncilState() == CouncilState.LendingStateSecretary))
					favouriteListDAO.removeReattachedTaskFromList(getTask(), predefinedFavouriteList);
				else
					logger.debug("Not removing from CouncilLendingMTA list, other councils are in this state");
				break;
			default:
				break;
			}
		}
	}

	public void onNameChange() {
		getSelectedCouncil().setName(generateName());
		saveCouncilData();
	}

	public String generateName() {
		StringBuffer str = new StringBuffer();

		// name
		if (getSelectedCouncil().getCouncilPhysician() != null)
			str.append(getSelectedCouncil().getCouncilPhysician().getPerson().getFullName());
		else
			str.append(resourceBundle.get("dialog.council.data.newCouncil"));

		str.append(" ");

		LocalDateTime ldt = LocalDateTime.ofInstant(selectedCouncil.getDateOfRequest().toInstant(),
				ZoneId.systemDefault());

		// adding date
		str.append(ldt.format(DateTimeFormatter.ofPattern(DateFormat.GERMAN_DATE.getDateFormat())));

		return str.toString();
	}

	public void saveCouncilData() {
		if (getSelectedCouncil() != null)
			save();
	}

	/**
	 * Saves a council. If id=0, the council is new and is added to the task, if
	 * id!=0 the council will only be saved.
	 * 
	 * @throws HistoDatabaseInconsistentVersionException
	 */
	private boolean save() throws HistoDatabaseInconsistentVersionException {
			logger.debug("Council Dialog: Saving council");
			genericDAO.savePatientData(getSelectedCouncil(), getTask(), "log.patient.task.council.update",
					String.valueOf(getSelectedCouncil().getId()));
			
	}

	/**
	 * hideDialog should be called first. This method opens a printer dialog, an let
	 * the gui click the button for opening the dialog. This is a workaround for
	 * opening other dialogs after closing the current dialog.
	 */
	public void printCouncilReport() {
		try {
			save();

			List<DocumentTemplate> templates = DocumentTemplate.getTemplates(DocumentType.COUNCIL_REQUEST);
			List<AbstractDocumentUi<?>> subSelectUIs = templates.stream().map(p -> p.getDocumentUi())
					.collect(Collectors.toList());

			for (AbstractDocumentUi<?> documentUi : subSelectUIs) {
				((CouncilReportUi) documentUi).initialize(task, getSelectedCouncil());
				((CouncilReportUi) documentUi).setRenderSelectedContact(true);
				((CouncilReportUi) documentUi).setUpdatePdfOnEverySettingChange(true);
				((CouncilReportUi) documentUi).setSingleSelect(true);
			}

			dialogHandlerAction.getPrintDialog().initBeanForPrinting(task, subSelectUIs, DocumentType.COUNCIL_REQUEST);
			dialogHandlerAction.getPrintDialog().prepareDialog();

			// workaround for showing and hiding two dialogues
		} catch (HistoDatabaseInconsistentVersionException e) {
			onDatabaseVersionConflict();
		}
	}

	public void showMediaSelectDialog() {
		showMediaSelectDialog(null);
	}

	public void showMediaSelectDialog(PDFContainer pdf) {
		try {

			// init dialog for patient and task
			dialogHandlerAction.getMediaDialog().initBean(getTask().getPatient(),
					new DataList[] { getTask(), getTask().getPatient() }, pdf, true);

			// setting advance copy mode with move as true and target to task
			// and biobank
			dialogHandlerAction.getMediaDialog().enableAutoCopyMode(new DataList[] { getTask(), getSelectedCouncil() },
					true, true);

			// enabeling upload to task
			dialogHandlerAction.getMediaDialog().enableUpload(new DataList[] { getTask() },
					new DocumentType[] { DocumentType.COUNCIL_REPLY });

			// setting info text
			dialogHandlerAction.getMediaDialog().setActionDescription(
					resourceBundle.get("dialog.pdfOrganizer.headline.info.council", getTask().getTaskID()));

			// show dialog
			dialogHandlerAction.getMediaDialog().prepareDialog();
		} catch (HistoDatabaseInconsistentVersionException e) {
			// do nothing
			// TODO: infom user
		}
	}

	public void showMediaViewDialog(PDFContainer pdfContainer) {
		// init dialog for patient and task
		dialogHandlerAction.getMediaDialog().initBean(getTask().getPatient(), getSelectedCouncil(), pdfContainer,
				false);

		// setting info text
		dialogHandlerAction.getMediaDialog().setActionDescription(
				resourceBundle.get("dialog.pdfOrganizer.headline.info.council", getTask().getTaskID()));

		// show dialog
		dialogHandlerAction.getMediaDialog().prepareDialog();
	}

}
