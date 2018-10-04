package com.patho.main.config.excepion;

import java.util.Iterator;
import java.util.Map;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.patho.main.action.handler.GlobalEditViewHandler;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.util.dialogReturn.ReloadEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * <!-- <factory> <exception-handler-factory> org.histo.config.exception.
 * CustomExceptionHandlerFactory </exception-handler-factory> </factory>--> can
 * be actived in faces config again if needed
 * 
 * <mvc:interceptors> <bean id="webContentInterceptor" class=
 * "org.springframework.web.servlet.mvc.WebContentInterceptor">
 * <property name= "cacheSeconds" value="0" />
 * <property name="useExpiresHeader" value="true" />
 * <property name="useCacheControlHeader" value="true" />
 * <property name= "useCacheControlNoStore" value="true" /> </bean>
 * </mvc:interceptors>
 * 
 * this is used instead in spring config
 * 
 * @author andi
 *
 */
@Configurable
public class PathoExceptionHandler extends ExceptionHandlerWrapper {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	protected ResourceBundle resourceBundle;
	
	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	protected GlobalEditViewHandler globalEditViewHandler;
	
	private ExceptionHandler wrapped;

	public PathoExceptionHandler(ExceptionHandler exception) {
		this.wrapped = exception;
	}

	@Override
	public ExceptionHandler getWrapped() {
		return wrapped;
	}

	@Override
	public void handle() throws FacesException {

		final Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator();

		while (i.hasNext()) {
			ExceptionQueuedEvent event = i.next();
			ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();

			// get the exception from context
			Throwable cause = context.getException();

			final FacesContext fc = FacesContext.getCurrentInstance();
			final Map<String, Object> requestMap = fc.getExternalContext().getRequestMap();
			final NavigationHandler nav = fc.getApplication().getNavigationHandler();

			logger.debug("Global exeption handler - " + cause);

			// getting root excepetion
			while (cause instanceof FacesException || cause instanceof ELException) {
				if (cause instanceof FacesException)
					cause = ((FacesException) cause).getCause();
				else
					cause = ((ELException) cause).getCause();
			}

			boolean hanled = false;

			logger.debug("Global exeption handler - " + cause);

			if (cause != null) {
				if(cause instanceof ObjectOptimisticLockingFailureException) {
					String c = ((ObjectOptimisticLockingFailureException)cause).getPersistentClassName();
					Object identifier = ((ObjectOptimisticLockingFailureException)cause).getIdentifier();
					
					System.out.println(c + " " + Task.class.toString());
					
					if(c.equals(Task.class.toString())) {
						System.out.println("task");
					}else if(c.equals(Patient.class.toString())) {
						System.out.println("Patient");
					}
						
					closeDialogs();
					globalEditViewHandler.reloadAllData();
					MessageHandler.executeScript("clickButtonFromBean('globalCommandsForm:refreshContentBtn')");
					MessageHandler.sendGrowlMessagesAsResource("growl.error.version", "growl.error.version.text");
					
					hanled = true;
				}else if(cause instanceof InvalidDataAccessApiUsageException) {
					
					closeDialogs();
					globalEditViewHandler.reloadAllData();
					MessageHandler.executeScript("clickButtonFromBean('globalCommandsForm:refreshContentBtn')");
					MessageHandler.sendGrowlMessagesAsResource("growl.error.save", "growl.error.save.text");
					
					hanled = true;
				}
			}
//
//				if (cause instanceof CustomNotUniqueReqest) {
//					logger.debug("Not Unique Reqest Error");
//					PrimeFaces.current().dialog().closeDynamic(null);
//					mainHandlerAction.sendGrowlMessages("Fehler!", "Doppelte Anfrage", FacesMessage.SEVERITY_ERROR);
//				} else if (cause instanceof HistoDatabaseInconsistentVersionException) {
//
//					logger.debug("Database Version Conflict");
//
//					if (((HistoDatabaseInconsistentVersionException) cause).getOldVersion() instanceof Patient) {
//						logger.debug("Version Error, replacing Patient");
//						worklistViewHandlerAction.replacePatientInCurrentWorklist(
//								((Patient) ((HistoDatabaseInconsistentVersionException) cause).getOldVersion()));
//					} else if (((HistoDatabaseInconsistentVersionException) cause).getOldVersion() instanceof Task) {
//						logger.debug("Version Error, replacing task");
//						worklistViewHandlerAction.replaceTaskInCurrentWorklist(
//								((Task) ((HistoDatabaseInconsistentVersionException) cause).getOldVersion()));
//					} else if (((HistoDatabaseInconsistentVersionException) cause)
//							.getOldVersion() instanceof Parent<?>) {
//						logger.debug("Version Error, replacing parent -> task");
//						worklistViewHandlerAction.replaceTaskInCurrentWorklist(
//								((Parent<?>) ((HistoDatabaseInconsistentVersionException) cause).getOldVersion())
//										.getTask());
//					} else {
//						logger.debug("Version Error,"
//								+ ((HistoDatabaseInconsistentVersionException) cause).getOldVersion().getClass());
//					}
//
//					mainHandlerAction.sendGrowlMessagesAsResource("growl.error", "growl.error.version");
//
//					PrimeFaces.current()
//							.executeScript("clickButtonFromBean('#globalCommandsForm\\\\:refreshContentBtn')");
//
//					// TODO implement
//				} else if (cause instanceof AbortProcessingException) {
//					logger.debug("Error aboring all actions!");
//				} else if (cause instanceof BadCredentialsException) {
//					hanled = false;
//				} else if (cause instanceof HibernateException) {
//					System.out.println("datenbank exception");
//					globalEditViewHandler.setDisplayView(View.WORKLIST_DATA_ERROR);
//				} else {
//					logger.debug("Other exception!");
//					cause.printStackTrace();
//				}
//
//				// ErrorMail mail = new ErrorMail();
//				// mail.prepareTemplate(userHandlerAction.getCurrentUser(), "Ehandler " +
//				// cause.getMessage(),
//				// new Date(System.currentTimeMillis()));
//				// mail.fillTemplate();
//				// globalSettings.getMailHandler().sendErrorMail(mail);
//			} else if (event instanceof ExceptionQueuedEvent) {
//
//			}

			if (hanled)
				i.remove();
		}
		// parent hanle
		getWrapped().handle();
	}
	
	private void closeDialogs() {
		if(MessageHandler.isDialogContext()) {
			logger.debug("Closing Dialog");
			PrimeFaces.current().dialog().closeDynamic(new ReloadEvent());
		}
	}
}