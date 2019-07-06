package com.patho.main.action.dialog.miscellaneous;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.Dialog;
import com.patho.main.model.dto.AccountingData;
import com.patho.main.repository.AccountingDataRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Configurable
public class AccountingDataDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private AccountingDataRepository accountingDataRepository;

	private LocalDate fromDate;
	private LocalDate toDate;

	private List<AccountingData> accountingData;

	private boolean advancedData;

	public AccountingDataDialog initAndPrepareBean() {
		if (initBean())
			prepareDialog();

		this.advancedData = false;

		return this;
	}

	public boolean initBean() {
		super.initBean(task, Dialog.ACCOUNTING_DATA);
		return true;
	}

	public void loadAccountingDate() {

		logger.debug("Loading accounting-data form {} to {}", fromDate, toDate);

		if (fromDate == null || toDate == null) {
			setAccountingData(null);
		} else {
			setAccountingData(accountingDataRepository.findAllBetweenDates(this.fromDate, this.toDate));
			MessageHandler.sendGrowlMessagesAsResource("growl.accounting.listLoaded");
		}
	}

	public String getExportFileName() {
		StringBuilder builder = new StringBuilder();
		builder.append("Export");

		DateFormat df = new SimpleDateFormat("yyy-MM-dd");

		if (fromDate != null) {
			builder.append("_" + df.format(this.fromDate));
		}

		if (fromDate != null) {
			builder.append("_" + df.format(this.toDate));
		}
		builder.append(".xls");

		return builder.toString();
	}
}
