package com.patho.main.action.handler;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.patho.main.action.handler.GlobalEditViewHandler.StainingListAction;
import com.patho.main.common.ContactRole;
import com.patho.main.common.CouncilState;
import com.patho.main.common.DateFormat;
import com.patho.main.common.Display;
import com.patho.main.common.Eye;
import com.patho.main.common.InformedConsentType;
import com.patho.main.common.Month;
import com.patho.main.common.SignatureRole;
import com.patho.main.common.TaskPriority;
import com.patho.main.model.ListItem;
import com.patho.main.model.StainingPrototype;
import com.patho.main.model.user.HistoGroup;


/**
 * <p:importEnum type="org.histo.config.enums.Display" var="display" />
 * 
 * @author glatza
 *
 */
@Component
@Scope(value = "session")
public class EnumProvider {

	/**
	 * Used for select view via p:selectOneMenu, p:importEnum not working in
	 * this context
	 * 
	 * @return
	 */
	public Display[] getDisplays() {
		return Display.values();
	}

	/**
	 * Returns an array containing all available month.
	 * 
	 * @return
	 */
	public Month[] getMonth() {
		return Month.values();
	}

	/**
	 * Returns an array containing all values of the eye enumeration
	 * 
	 * @return
	 */
	public Eye[] getEyes() {
		return Eye.values();
	}

	/**
	 * Returns an array containing all values of the contactRole enumeration.
	 * 
	 * @return
	 */
	public ContactRole[] getContactRoles() {
		return ContactRole.values();
	}

	/**
	 * Returns an array containing all values of the TaskPriority enumeration
	 * 
	 * @return
	 */
	public TaskPriority[] getTaskPriority() {
		return TaskPriority.values();
	}

	/**
	 * Returns an array containing all values of the SigantureRole enumeration.
	 * 
	 * @return
	 */
	public SignatureRole[] getSignatureRoles() {
		return SignatureRole.values();
	}

	/**
	 * Returns an array containing all values of the StainingListAction
	 * enumeration.
	 * 
	 * @return
	 */
	public StainingListAction[] getStainingListActions() {
		return StainingListAction.values();
	}

	/**
	 * Returns an array containing all values of the StaticList enumeration.
	 * 
	 * @return
	 */
	public ListItem.StaticList[] getStaticLists() {
		return ListItem.StaticList.values();
	}

	/**
	 * Returns a dateformat, is used, because in mainHandlerAction the date
	 * method can take a string, an primefaces prefers the string method other
	 * the DateFormat method.
	 * 
	 * @param dateFormat
	 * @return
	 */
	public DateFormat getDateFormat(DateFormat dateFormat) {
		return dateFormat;
	}

	/**
	 * Returns an array with all values of the InformedConsentInterface
	 * 
	 * @return
	 */
	public InformedConsentType[] getInformedConsentTypes() {
		return InformedConsentType.values();
	}

	/**
	 * Returns an array with all values of the {@link CouncilState} enum
	 * 
	 * @return
	 */
	public CouncilState[] getCouncilStates() {
		return CouncilState.values();
	}

	/**
	 * Returns an array with all values of the StainingType enum
	 * @return
	 */
	public StainingPrototype.StainingType[] getStainingTypes(){
		return  StainingPrototype.StainingType.values();
	}
	
	public HistoGroup.AuthRole[] getAuthRoles(){
		return HistoGroup.AuthRole.values();
	}
}