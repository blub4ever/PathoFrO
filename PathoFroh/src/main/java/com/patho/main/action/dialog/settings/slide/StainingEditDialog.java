package com.patho.main.action.dialog.settings.slide;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.dao.SettingsDAO;
import com.patho.main.model.StainingPrototype;
import com.patho.main.model.StainingPrototypeDetails;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configurable
@Getter
@Setter
@Slf4j
public class StainingEditDialog extends AbstractDialog {

	@Autowired
	private SettingsDAO settingsDAO;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TransactionTemplate transactionTemplate;

	private boolean newStaining;

	private StainingPrototype stainingPrototype;

	private List<StainingPrototypeDetails> toRemove;

	public void initAndPrepareBean() {
		StainingPrototype staining = new StainingPrototype();

		if (initBean(staining, false))
			prepareDialog();
	}

	public void initAndPrepareBean(StainingPrototype staining) {
		if (initBean(staining, true))
			prepareDialog();
	}

	public boolean initBean(StainingPrototype staining, boolean initialize) {

		if (initialize) {
			try {
				setStainingPrototype(settingsDAO.initializeStainingPrototype(staining, true));
			} catch (HistoDatabaseInconsistentVersionException e) {
				log.debug("Version conflict, updating entity");
				setStainingPrototype(settingsDAO.getStainingPrototype(stainingPrototype.getId(), true));
			}
		} else {
			setStainingPrototype(staining);
		}

		setNewStaining(staining.getId() == 0 ? true : false);

		setToRemove(new ArrayList<StainingPrototypeDetails>());

		if (staining.getBatchDetails() == null)
			staining.setBatchDetails(new ArrayList<StainingPrototypeDetails>());

		super.initBean(task, Dialog.SETTINGS_STAINING_EDIT);
		return true;
	}

	public void addBatch() {
		StainingPrototypeDetails newBatch = new StainingPrototypeDetails(stainingPrototype);
		newBatch.setDeliveryDate(new Date(System.currentTimeMillis()));
		getStainingPrototype().getBatchDetails().add(0, newBatch);
	}

	public void removeBatch(StainingPrototypeDetails stainingPrototypeDetails) {
		stainingPrototype.getBatchDetails().remove(stainingPrototypeDetails);

		// delete if user clicks save
		if (stainingPrototypeDetails.getId() != 0) {
			toRemove.add(stainingPrototypeDetails);
		}
	}

	public void cloneBatch(StainingPrototypeDetails stainingPrototypeDetails) {
		try {
			StainingPrototypeDetails clone = stainingPrototypeDetails.clone();
			clone.setId(0);
			stainingPrototype.getBatchDetails().add(0, clone);
		} catch (CloneNotSupportedException e) {
		}
	}

	public void saveStaining() {
		try {

			transactionTemplate.execute(new TransactionCallbackWithoutResult() {

				public void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

					for (StainingPrototypeDetails stainingPrototypeDetails : toRemove) {
						settingsDAO.delete(stainingPrototypeDetails);
					}

					if (stainingPrototype.getId() == 0) {
						settingsDAO.save(stainingPrototype,
								resourceBundle.get("log.settings.staining.new", stainingPrototype));
					} else {
						settingsDAO.save(stainingPrototype,
								resourceBundle.get("log.settings.staining.update", stainingPrototype));
					}
				}
			});
			// TODO Lock
			// genericDAO.lockParent(task);

		} catch (Exception e) {
			onDatabaseVersionConflict();
		}

	}
}
