package com.patho.main.action.handler;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.primefaces.PrimeFaces;
import org.primefaces.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.patho.main.config.util.ResourceBundle;
import com.patho.main.util.exception.CustomUserNotificationExcepetion;

public class MessageHandler extends AbstractHandler {

	protected static final Logger staticLogger = LoggerFactory.getLogger(MessageHandler.class);

	/**
	 * ID of the global info growl
	 */
	private static final String GLOBAL_GROWL_ID = "globalGrowl";

	public static void sendGrowlMessages(String headline, String message) {
		sendGrowlMessages(new FacesMessage(FacesMessage.SEVERITY_INFO, headline, message));
	}

	public static void sendGrowlMessages(String headline, String message, FacesMessage.Severity servertiy) {
		sendGrowlMessages(new FacesMessage(servertiy, headline, message));
	}

	public static void sendGrowlMessages(CustomUserNotificationExcepetion e) {
		sendGrowlMessages(e.getHeadline(), e.getMessage(), FacesMessage.SEVERITY_ERROR);
	}

	public static void sendGrowlMessages(FacesMessage message) {

		PrimeFaces.current().executeScript("updateGlobalGrowl('" + GLOBAL_GROWL_ID + "','" + message.getSummary()
				+ "','" + message.getDetail() + "','" + message.getSeverity().toString().toLowerCase() + "');");

		staticLogger.debug("Growl (" + GLOBAL_GROWL_ID + ") Messagen (" + message.getSeverity() + "): "
				+ message.getSummary() + " " + message.getDetail());
	}

	public static void sendGrowlMessagesAsResource(CustomUserNotificationExcepetion e) {
		sendGrowlMessagesAsResource(e.getHeadline(), e.getMessage(), FacesMessage.SEVERITY_ERROR);
	}
	
	public static void sendGrowlMessagesAsResource(String headline) {
		sendGrowlMessagesAsResource(headline, "growl.empty");
	}

	public static void sendGrowlMessagesAsResource(String headline, FacesMessage.Severity servertiy) {
		sendGrowlMessagesAsResource(headline, "growl.empty", servertiy);
	}

	public static void sendGrowlMessagesAsResource(String headline, String message) {
		sendGrowlMessagesAsResource(headline, message, FacesMessage.SEVERITY_INFO);
	}

	public static void sendGrowlMessagesAsResource(String headline, String message, Object[] params) {
		sendGrowlMessagesAsResource(headline, message, FacesMessage.SEVERITY_INFO, params);
	}

	public static void sendGrowlMessagesAsResource(String headline, String message, FacesMessage.Severity servertiy,
			Object... params) {

		ResourceBundle bundle = ResourceBundle.getResourceBundle();
		sendGrowlMessages(bundle.get(headline), bundle.get(message, params), servertiy);
	}

	public static void executeScript(String script) {
		staticLogger.debug("Execute Script: " + script);
		PrimeFaces.current().executeScript(script);
	}

	public static boolean isDialogContext() {
		return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
				.containsKey(Constants.DIALOG_FRAMEWORK.CONVERSATION_PARAM);
	}

}