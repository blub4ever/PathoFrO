package com.patho.main.action.handler.views;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Lazy;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.handler.GlobalEditViewHandler;
import com.patho.main.action.handler.GlobalEditViewHandler.StainingListAction;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.action.handler.WorkPhaseHandler;
import com.patho.main.model.patient.Slide;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.PrintDocumentRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.SlideService;
import com.patho.main.template.InitializeToken;
import com.patho.main.template.PrintDocument;
import com.patho.main.ui.StainingTableChooser;
import com.patho.main.util.helper.HistoUtil;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Savin receipt log data
 * 
 * @author andi
 *
 */
@Getter
@Setter
@Configurable
public class ReceiptLogView extends AbstractTaskView {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private SlideService slideService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@Lazy
	private WorkPhaseHandler workPhaseHandler;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PrintDocumentRepository printDocumentRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PathoConfig pathoConfig;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserHandlerAction userHandlerAction;

	/**
	 * Currently selected task in table form, transient, used for gui
	 */
	private ArrayList<StainingTableChooser<?>> stainingTableRows;

	/**
	 * Is used for selecting a chooser from the generated list (generated by task).
	 * It is used to edit the names of the entities by an overlaypannel
	 */
	private StainingTableChooser<?> selectedStainingTableChooser;

	/**
	 * This variable is used to save the selected action, which should be executed
	 * upon all selected slides
	 */
	private StainingListAction actionOnMany;

	public ReceiptLogView(GlobalEditViewHandler globalEditViewHandler) {
		super(globalEditViewHandler);
	}

	/**
	 * Toggles the status of a StainingTableChooser object and all chides.
	 * 
	 * @param chooser
	 */
	public void toggleChildrenChoosenFlag(StainingTableChooser<?> chooser) {
		setChildrenAsChoosen(chooser, !chooser.isChoosen());
	}

	/**
	 * Sets all children of a StainingTableChoosers to chosen/unchosen
	 * 
	 * @param chooser
	 * @param choosen
	 */
	public void setChildrenAsChoosen(StainingTableChooser<?> chooser, boolean chosen) {
		chooser.setChoosen(chosen);
		if (chooser.isSampleType() || chooser.isBlockType()) {
			for (StainingTableChooser<?> tmp : chooser.getChildren()) {
				setChildrenAsChoosen(tmp, chosen);
			}
		}

		setActionOnMany(StainingListAction.NONE);
	}

	/**
	 * Sets a lists of StainingTableChoosers to chosen/unchosen Setzt den Status
	 * einer Liste von StainingTableChoosers und ihrer Kinder
	 * 
	 * @param choosers
	 * @param choosen
	 */
	public void setListAsChoosen(List<StainingTableChooser<?>> choosers, boolean chosen) {
		for (StainingTableChooser<?> chooser : choosers) {
			if (chooser.isSampleType()) {
				setChildrenAsChoosen(chooser, chosen);
			}
		}
	}

	/**
	 * Executes an action on all selected slides
	 * 
	 * @param task
	 */
	public void performActionOnMany(Task task) {
		performActionOnMany(task, getActionOnMany());
	}

	/**
	 * Executes an action on all selected slides
	 * 
	 * @param list
	 * @param action
	 */
	public void performActionOnMany(Task task, StainingListAction action) {
		List<StainingTableChooser<?>> list = getStainingTableRows();

		List<Slide> slides = list.stream().filter(p -> p.isChoosen() && p.isStainingType())
				.map(p -> (Slide) p.getEntity()).collect(Collectors.toList());

		if (slides.isEmpty()) {
			logger.debug("Nothing selected, do not performe any action");
			return;
		}

		switch (getActionOnMany()) {
		case PERFORMED:
		case NOT_PERFORMED:
			logger.debug("Setting staining status of selected slides");
			slides.stream().forEachOrdered(p -> slideService.completedStaining(p,
					getActionOnMany() == StainingListAction.PERFORMED ? true : false));
			workPhaseHandler.updateStainingPhase(
					taskRepository.save(task, resourceBundle.get("log.task.slide.completed", task)));
			break;
		case ARCHIVE:
			// TODO implement
			System.out.println("To impliment");
			break;
		case PRINT:

			List<Slide> selectedSlides = new ArrayList<Slide>();

			for (StainingTableChooser<?> stainingTableChooser : list)
				if (stainingTableChooser.isChoosen() && stainingTableChooser.isStainingType())
					selectedSlides.add((Slide) stainingTableChooser.getEntity());

			Slide[] slideArr = new Slide[selectedSlides.size()];
			printLable(selectedSlides.toArray(slideArr));

			break;
		default:
			break;
		}

		setActionOnMany(StainingListAction.NONE);

	}

	@Override
	public void loadView() {
		logger.debug("Loading receiptlog view");
		setActionOnMany(StainingListAction.NONE);
		setStainingTableRows(StainingTableChooser.factory(getTask(), false));
	}

	public void printAllLables() {
		MessageHandler.sendGrowlMessagesAsResource("growl.print", "growl.print.slide.print.all");
		printLable(getStainingTableRows().stream().filter(p -> p.isStainingType()).map(p -> (Slide) p.getEntity())
				.toArray(Slide[]::new));
	}

	public void printLable(Slide... slides) {

		if (slides == null || slides.length == 0)
			return;

		Optional<PrintDocument> printDocument = printDocumentRepository
				.findByID(pathoConfig.getDefaultDocuments().getSlideLabelDocument());

		if (!printDocument.isPresent()) {
			logger.error("New Task: No TemplateUtil for printing label found");
			MessageHandler.sendGrowlErrorAsResource("growl.error.critical", "growl.print.slide.noTemplate");
			return;
		}

		List<String> toPrint = new ArrayList<String>(slides.length);

		for (Slide slide : slides) {
			printDocument.get().initilize(new InitializeToken("slide", slide), new InitializeToken("date", new Date()),
					new InitializeToken("uniqueID",
							slide.getTask().getTaskID() + "" + HistoUtil.fitString(slide.getUniqueIDinTask(), 3, '0')));
			System.out.println(printDocument.get().getFinalContent());
			toPrint.add(printDocument.get().getFinalContent());
		}

		userHandlerAction.getSelectedLabelPrinter().print(toPrint);

		MessageHandler.sendGrowlMessagesAsResource("growl.print", "growl.print.slide.print");

		logger.debug("Printing label..");

	}

}
