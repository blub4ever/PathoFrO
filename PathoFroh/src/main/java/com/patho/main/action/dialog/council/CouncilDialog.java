package com.patho.main.action.dialog.council;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Lazy;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.DialogHandler;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.common.SortOrder;
import com.patho.main.config.PathoConfig;
import com.patho.main.model.Council;
import com.patho.main.model.Council.CouncilNotificationMethod;
import com.patho.main.model.ListItem;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.Physician;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.CouncilRepository;
import com.patho.main.repository.ListItemRepository;
import com.patho.main.repository.PhysicianRepository;
import com.patho.main.repository.PrintDocumentRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.CouncilService;
import com.patho.main.service.PDFService;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocument.DocumentType;
import com.patho.main.template.print.ui.document.AbstractDocumentUi;
import com.patho.main.template.print.ui.document.report.CouncilReportUi;
import com.patho.main.ui.pdf.PDFStreamContainer;
import com.patho.main.ui.transformer.DefaultTransformer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

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

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private CouncilRepository councilRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PDFService pdfService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PrintDocumentRepository printDocumentRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@Lazy
	private DialogHandler dialogHandler;

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
	private CouncilContainer selectedCouncil;

	/**
	 * Council nodes
	 */
	private List<TreeNode> councilNodes;

	/**
	 * List of all councils of this tasks
	 */
	private List<CouncilContainer> councilList;

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

	private PDFStreamContainer streamContainer;

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

		setStreamContainer(new PDFStreamContainer());

		setSelectedNode(null);
		setSelectedCouncil(null);

		// reload task in order to load councils
		update(true);

		updatePhysicianLists();

		setAttachmentList(listItemRepository
				.findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.COUNCIL_ATTACHMENT, false));

		// setting council as default
		if (getCouncilList().size() != 0) {
			selectNode(getCouncilList().get(0), 0);
		}

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

			if (getCouncilList() != null && getCouncilList().size() > 0) {
				List<Council> coucils = new ArrayList<Council>(getTask().getCouncils());
				List<Council> foundCouncils = new ArrayList<Council>();
				List<CouncilContainer> oldContainers = new ArrayList<CouncilContainer>();

				loop1: for (CouncilContainer container : getCouncilList()) {
					for (Council council : coucils) {
						if (container.getCouncil().equals(council)) {
							container.setCouncil(council);
							foundCouncils.add(council);
							continue loop1;
						}
					}
					oldContainers.add(container);
				}

				// adding new councils
				coucils.removeAll(foundCouncils);

				for (Council council : coucils) {
					getCouncilList().add(new CouncilContainer(council));
				}

				// removing old contaienr
				getCouncilList().removeAll(oldContainers);

				// sorting list
				getCouncilList().stream().sorted((f1, f2) -> f2.getDateOfRequest().compareTo(f1.getDateOfRequest()));

			} else
				setCouncilList(getTask().getCouncils().stream().map(p -> new CouncilContainer(p))
						.collect(Collectors.toList()));
		}

		setRoot(generateTree());

		if (getSelectedNode() != null) {
			logger.debug("Replacing selected node");
			for (TreeNode node : getCouncilNodes()) {
				if (node.getData().equals(getSelectedNode().getData())) {
					setSelectedNode(node);
					logger.debug("Replacing selected node");
					return;
				}
			}
		}
	}

	private TreeNode generateTree() {

		logger.debug("Generating new tree");

		TreeNode root = new DefaultTreeNode("Root", null);

		TreeNode taskNode = new DefaultTreeNode("task", getTask(), root);
		taskNode.setExpanded(true);
		taskNode.setSelectable(false);

		setCouncilNodes(new ArrayList<TreeNode>());

		for (CouncilContainer council : getCouncilList()) {
			logger.debug("Creating tree for {}", council);
			TreeNode councilNode = new DefaultTreeNode("council", council, taskNode);
			councilNode.setExpanded(true);
			councilNode.setSelectable(false);

			getCouncilNodes().add(councilNode);

			TreeNode councilRequestNode = new DefaultTreeNode("council_request", council, councilNode);
			councilRequestNode.setExpanded(true);
			councilRequestNode.setSelectable(true);

			if (council.getNotificationMethod() == CouncilNotificationMethod.MTA
					&& council.isCouncilRequestCompleted()) {
				TreeNode councilshipNode = new DefaultTreeNode("council_ship", council, councilNode);
				councilshipNode.setExpanded(true);
				councilshipNode.setSelectable(true);
			}

			TreeNode councilReturnNode = new DefaultTreeNode("council_reply", council, councilNode);
			councilReturnNode.setExpanded(true);
			councilReturnNode.setSelectable(true);

			TreeNode councilDataNode = new DefaultTreeNode("data_node", council, councilNode);
			councilDataNode.setExpanded(true);
			councilDataNode.setSelectable(false);

			for (PDFContainer container : council.getAttachedPdfs()) {
				TreeNode councilFileNode = new DefaultTreeNode("pdf_node", new CouncilPDFContainer(council, container),
						councilDataNode);
				councilFileNode.setExpanded(false);
				councilFileNode.setSelectable(true);
			}
		}

		return root;
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
			case "pdf_node":
				return "inculde/report.xhtml";
			default:
				return "inculde/empty.xhtml";
			}
		}
		return "inculde/empty.xhtml";
	}

	public void setSelectedNode(TreeNode node) {

		// deselecting old node
		if (selectedNode != null)
			selectedNode.setSelected(false);

		selectedNode = node;

		if (node != null) {
			setSelectedCouncil((CouncilContainer) node.getData());
			node.setSelected(true);

			if (node.getData() instanceof CouncilPDFContainer) {
				logger.debug("Is pdf container, render pdf");
				getStreamContainer().setDisplayPDF(((CouncilPDFContainer) node.getData()).getContainer());
			}

		} else
			setSelectedCouncil(null);

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
		setPhysicianSigantureList(physicianRepository.findAllByRole(new ContactRole[] { ContactRole.SIGNATURE }, true,
				SortOrder.PRIORITY));
		setPhysicianSigantureListTransformer(new DefaultTransformer<Physician>(getPhysicianSigantureList()));
	}

	/**
	 * Creates a new council and saves it
	 */
	public void createCouncil() {
		logger.info("Adding new council");
		Council c = councilService.createCouncil(getTask(), true).getCouncil();
		update(true);
		logger.debug("Council with id {} created", c.getId());

		selectNode(new CouncilContainer(c), 0);
	}

	public void admendRequestState() {
		setAdmendSelectedRequestState(true);
	}

	public void endRequestState(CouncilContainer council) {
		logger.debug("Ending request phase");
		councilService.endCouncilRequest(getTask(), council.getCouncil());
		setAdmendSelectedRequestState(false);
		update(true);
		selectNode(council, 1);
	}

	private void selectNode(CouncilContainer container, int child) {
		for (TreeNode node : getCouncilNodes()) {
			if (((CouncilContainer) node.getData()).getCouncil().equals(container)) {
				if (child < node.getChildCount())
					setSelectedNode(node.getChildren().get(child));
			}
		}
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
			pdfService.createAndAttachPDF(getSelectedCouncil().getCouncil(), file, DocumentType.COUNCIL_REPLY, "", "",
					true, new File(PathoConfig.FileSettings.FILE_REPOSITORY_PATH_TOKEN
							+ String.valueOf(getTask().getPatient().getId())));

			MessageHandler.sendGrowlMessagesAsResource("growl.upload.success");
		} catch (IllegalAccessError e) {
			MessageHandler.sendGrowlMessagesAsResource("growl.upload.failed", FacesMessage.SEVERITY_ERROR);
		}

		update(true);
	}

	/**
	 * Updates the council name
	 */
	public void onNameChange() {
		getSelectedCouncil().setName(councilService.generateCouncilName(getSelectedCouncil()));
		saveSelectedCouncil();
	}

	public void onShipSample() {
		if (getSelectedCouncil().isSampleShipped() && getSelectedCouncil().getSampleShippedDate() == null) {
			getSelectedCouncil().setSampleShippedDate(new Date());
		}
		saveSelectedCouncil();
	}

	public void onReturnSample() {
		if (getSelectedCouncil().isSampleShipped() && getSelectedCouncil().getSampleShippedDate() == null) {
			getSelectedCouncil().setSampleShippedDate(new Date());
		}
		saveSelectedCouncil();
	}

	public void noReplyReceived() {
		if (getSelectedCouncil().isReplyReceived() && getSelectedCouncil().getReplyReceivedDate() == null) {
			getSelectedCouncil().setReplyReceivedDate(new Date());
		}
		saveSelectedCouncil();
	}

	/**
	 * Saves the currently selected coucil and replaces all old objects
	 */
	public void saveSelectedCouncil() {
		logger.debug("Saving council data");
		if (getSelectedCouncil() != null) {
			Council c = councilRepository.save(getSelectedCouncil().getCouncil(),
					resourceBundle.get("log.patient.task.council.update", getTask(), getSelectedCouncil().getName()),
					getTask().getPatient());

			getSelectedCouncil().setCouncil(c);
		}
	}

	/**
	 * hideDialog should be called first. This method opens a printer dialog, an let
	 * the gui click the button for opening the dialog. This is a workaround for
	 * opening other dialogs after closing the current dialog.
	 */
	public void printCouncilReport() {

		List<PrintDocument> printDocuments = printDocumentRepository.findAllByTypes(DocumentType.COUNCIL_REQUEST);

		// getting ui objects
		List<AbstractDocumentUi<?, ?>> printDocumentUIs = AbstractDocumentUi.factory(printDocuments);

		for (AbstractDocumentUi<?, ?> documentUi : printDocumentUIs) {
			((CouncilReportUi) documentUi).initialize(task, getSelectedCouncil());
			((CouncilReportUi) documentUi).setRenderSelectedContact(true);
			((CouncilReportUi) documentUi).setUpdatePdfOnEverySettingChange(true);
			((CouncilReportUi) documentUi).getSharedData().setSingleSelect(true);
		}

		dialogHandler.getPrintDialog().initAndPrepareBean(getTask(), printDocumentUIs, printDocumentUIs.get(0));
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class CouncilContainer extends Council {
		@Delegate
		private Council council;
	}

	@Getter
	@Setter
	public static class CouncilPDFContainer extends CouncilContainer {
		private PDFContainer container;

		public CouncilPDFContainer(Council council, PDFContainer container) {
			super(council);
			this.container = container;
		}
	}
}
