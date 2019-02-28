package com.patho.main.action.dialog.settings.organization;

import com.patho.main.service.impl.SpringContextBridge;
import org.primefaces.event.SelectEvent;

import com.patho.main.action.dialog.settings.organization.OrganizationListDialog.OrganizationSelectReturnEvent;
import com.patho.main.model.person.Organization;
import com.patho.main.model.person.Person;
import com.patho.main.ui.transformer.DefaultTransformer;

public interface OrganizationFunctions {

	public Person getPerson();

	public DefaultTransformer<Organization> getOrganizationTransformer();

	public void setOrganizationTransformer(DefaultTransformer<Organization> transformer);

	/**
	 * Adds an organization to the user and updates the default address selection
	 * 
	 * @param event
	 */
	public default void onReturnOrganizationDialog(SelectEvent event) {
		// ReloadEvent should not be thrown by the select dialog
		if (event.getObject() != null && event.getObject() instanceof OrganizationSelectReturnEvent) {
			SpringContextBridge.services().getOrganizationService().addOrganizationToPerson(getPerson(),((OrganizationSelectReturnEvent) event.getObject()).getOrganization());
			updateOrganizationSelection();
		}
	}

	/**
	 * Removes an organization from the user and updates the default address
	 * selection
	 * 
	 * @param organization
	 */
	public default void removeFromOrganization(Organization organization) {
		getPerson().getOrganizsations().remove(organization);
		updateOrganizationSelection();
	}

	/**
	 * Is called after organization change of the user. Upated the transformer for
	 * selecting the default address and checks if the current default address is
	 * also in the organization array. If not the current default address will be
	 * set to null. (The program will use the personal address of the user)
	 */
	public default void updateOrganizationSelection() {
		setOrganizationTransformer(new DefaultTransformer<Organization>(getPerson().getOrganizsations()));

		// checking if organization exists within the organization array
		boolean organizationExsists = false;
		for (Organization organization : getPerson().getOrganizsations()) {
			if (getPerson().getDefaultAddress() != null && getPerson().getDefaultAddress().equals(organization)) {
				organizationExsists = true;
				break;
			}
		}

		if (!organizationExsists)
			getPerson().setDefaultAddress(null);

	}
}
