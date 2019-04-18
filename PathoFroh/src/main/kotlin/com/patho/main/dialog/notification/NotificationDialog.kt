package com.patho.main.dialog.notification

import com.patho.main.common.ContactRole
import com.patho.main.common.Dialog
import com.patho.main.config.PathoConfig
import com.patho.main.dialog.AbstractTabTaskDialog
import com.patho.main.dialog.contact.ContactAddDialog
import com.patho.main.model.patient.NotificationStatus
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.NotificationTyp
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.person.Contact
import com.patho.main.model.person.Person
import com.patho.main.repository.MailRepository
import com.patho.main.repository.PrintDocumentRepository
import com.patho.main.template.InitializeToken
import com.patho.main.template.MailTemplate
import com.patho.main.template.PrintDocument
import com.patho.main.template.print.ui.document.AbstractDocumentUi
import com.patho.main.template.print.ui.document.report.DiagnosisReportUi
import com.patho.main.ui.selectors.ContactSelector
import com.patho.main.ui.transformer.DefaultTransformer
import com.patho.main.util.status.ReportIntentStatusByDiagnosis
import com.patho.main.util.status.ReportIntentStatusByType
import org.primefaces.event.SelectEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.*

@Component()
@Scope(value = "session")
class NotificationDialog @Autowired constructor(
        private val printDocumentRepository: PrintDocumentRepository,
        private val pathoConfig: PathoConfig,
        private val mailRepository: MailRepository) : AbstractTabTaskDialog(Dialog.NOTIFICATION) {

    val generalTab: GeneralTab = GeneralTab()
    val mailTab: MailTab = MailTab()
    val faxTab: FaxTab = FaxTab()
    val letterTab: LetterTab = LetterTab()
    val phoneTab: PhoneTab = PhoneTab()
    val sendTab: SendTab = SendTab()

    init {
        tabs = arrayOf(generalTab, mailTab, faxTab, letterTab, phoneTab, sendTab)
    }

    fun initBean(task: Task): Boolean {
        return super.initBean(task, true, generalTab)
    }

    /**
     * Sets the disable status of all tabs
     */
    private fun disableTabs(generalTab: Boolean, mailTab: Boolean, faxTab: Boolean, letterTab: Boolean, phoneTab: Boolean,
                            sendTab: Boolean) {
        this.generalTab.disabled = generalTab
        this.mailTab.disabled = mailTab
        this.faxTab.disabled = faxTab
        this.letterTab.disabled = letterTab
        this.phoneTab.disabled = phoneTab
        this.sendTab.disabled = sendTab
    }

    /**
     * Open the print dialog in select mode in order to select a report for the given contact
     */
    fun openSelectPDFDialog(task: Task, contact: ReportIntent) {

        val printDocuments = printDocumentRepository.findAllByTypes(PrintDocument.DocumentType.DIAGNOSIS_REPORT,
                PrintDocument.DocumentType.DIAGNOSIS_REPORT_EXTERN)

        val selectors = ArrayList<ContactSelector>()

        selectors.add(ContactSelector(contact))
        selectors.add(ContactSelector(task,
                Person(resourceBundle.get("dialog.print.individualAddress"), Contact()), ContactRole.NONE))
        selectors[0].isSelected = true

        // getting ui objects
        val printDocumentUIs = AbstractDocumentUi.factory(printDocuments)

        for (documentUi in printDocumentUIs) {
            (documentUi as DiagnosisReportUi).initialize(task, selectors)
            documentUi.sharedData.isRenderSelectedContact = true
            documentUi.sharedData.isUpdatePdfOnEverySettingChange = true
            documentUi.sharedData.isSingleSelect = true
        }

//        dialogHandler.getPrintDialog().initAndPrepareBean(task, printDocumentUIs, printDocumentUIs[0])
//                .selectMode(true)
    }

    /**
     * Default return function, handel the pdf select return
     */
    override fun onSubDialogReturn(event: SelectEvent) {
        val container = event.component.attributes["container"]

        if (event.`object` is ContactAddDialog.SelectPhysicianReturnEvent) {
            logger.debug("On custom dialog return ${event.getObject()}  $container")
//            if (event.getObject() != null && event.getObject() instanceof PDFContainer && container != null
//                    && container instanceof NotificationContainer) {
//                logger.debug("Settign custom pdf for container "
//                        + ((NotificationContainer) container).getContact().getPerson().getFirstName());
//                ((NotificationContainer) container).setPdf((TemplatePDFContainer) event . getObject ());
//            }
            return;
        }

        super.onSubDialogReturn(event)
    }

    /**
     * Class for notification tabs
     */
    abstract inner class NotificationTab(tabName: String,
                                         name: String,
                                         viewID: String,
                                         centerInclude: String) : AbstractTab(tabName, name, viewID, centerInclude) {

        /**
         * True if notification method should be used
         */
        open val useNotification: Boolean = false

        /**
         * List of templates to select from
         */
        open var templates: List<PrintDocument> = listOf()
            set(value) {
                field = value
                templatesTransformer = DefaultTransformer(value)
            }

        /**
         * Transformer for gui
         */
        open var templatesTransformer: DefaultTransformer<PrintDocument> = DefaultTransformer(templates)

        /**
         * The selected template
         */
        open var selectedTemplate: PrintDocument? = null
    }

    /**
     *
     */
    abstract inner class ContactTab(tabName: String,
                                    name: String,
                                    viewID: String,
                                    centerInclude: String,
                                    val notificationTyp: NotificationTyp) : NotificationTab(tabName, name, viewID, centerInclude) {
        /**
         * If true individual addresses will be used in the reports
         */
        open var individualAddresses: Boolean = false

        /**
         * Contais a list of contacts with the specified notification type
         */
        open lateinit var reportIntentStatus: ReportIntentStatusByType

        /**
         * Returns true if notification intents are set in the notification status
         */
        override val useNotification: Boolean
            get() = reportIntentStatus.size > 0

        override fun updateData() {
            reportIntentStatus.update(task)
        }
    }

    /**
     * General Tab
     */
    open inner class GeneralTab : NotificationTab(
            "GeneralTab",
            "dialog.notification.tab.general",
            "generalTab",
            "include/general.xhtml") {

        /**
         * List of all diagnosis revisions with their status
         */
        open var diagnosisRevisions: List<ReportIntentStatusByDiagnosis.DiagnosisBearer> = mutableListOf()

        /**
         * Selected diagnosis for that the notification will be performed
         */
        open var selectDiagnosisRevision: ReportIntentStatusByDiagnosis.DiagnosisBearer? = null

        /**
         * Diagnosis bearer for displaying details in the datatable overlay panel
         */
        open var viewDiagnosisRevisionDetails: ReportIntentStatusByDiagnosis.DiagnosisBearer? = null

        /**
         * Count of additional prints
         */
        open var printCount: Int = 0

        /**
         * Selected diagnosis is not approved
         */
        open var selectedDiagnosisNotApproved: Boolean = false

        /**
         * This tab should always be used
         */
        override var useNotification: Boolean = true

        override fun initTab(force: Boolean): Boolean {
            logger.debug("Initializing general data...")
            diagnosisRevisions = ReportIntentStatusByDiagnosis(task).diagnosisBearer
            selectDiagnosisRevision = diagnosisRevisions.firstOrNull { p -> p.diagnosisRevision.notificationStatus == NotificationStatus.NOTIFICATION_PENDING }

            // setting templates + transformer
            templates = printDocumentRepository.findAllByTypes(PrintDocument.DocumentType.DIAGNOSIS_REPORT,
                    PrintDocument.DocumentType.DIAGNOSIS_REPORT_EXTERN)

            selectedTemplate = printDocumentRepository.findByID(pathoConfig.defaultDocuments.notificationDefaultPrintDocument).orElse(null)
            printCount = 2

            onDiagnosisSelect();

            return super.initTab(force)
        }

        override fun updateData() {}

        /**
         * Method is called on diagnosis change
         */
        open fun onDiagnosisSelect() {
            // sets a not approved warning
            selectedDiagnosisNotApproved = selectDiagnosisRevision?.diagnosisRevision?.notificationStatus != NotificationStatus.NOTIFICATION_PENDING

            // disable tabs if no diagnosis is selected.
            if (selectDiagnosisRevision == null)
                disableTabs(false, true, true, true, true, true)
            else
                disableTabs(false, false, false, false, false, false)
        }

    }

    open inner class MailTab : ContactTab(
            "MailTab",
            "dialog.notification.tab.mail",
            "mailTab",
            "include/mail.xhtml",
            NotificationTyp.EMAIL) {

        /**
         * Template of the email which is send to the receivers
         */
        open var mailTemplate: MailTemplate? = null

        override fun initTab(force: Boolean): Boolean {
            logger.debug("Initializing mail data...")
            individualAddresses = false
            // setting templates + transformer
            templates = printDocumentRepository.findAllByTypes(PrintDocument.DocumentType.DIAGNOSIS_REPORT,
                    PrintDocument.DocumentType.DIAGNOSIS_REPORT_EXTERN)

            selectedTemplate = printDocumentRepository.findByID(pathoConfig.defaultDocuments.notificationDefaultEmail).orElse(null)

            mailTemplate = mailRepository.findByID(pathoConfig.defaultDocuments.notificationDefaultEmail).orElse(null)

            mailTemplate?.initilize(
                    InitializeToken("patient", task.patient),
                    InitializeToken("task", task),
                    InitializeToken("contact", null))

            reportIntentStatus = ReportIntentStatusByType(task, notificationTyp)

            return super.initTab(force)
        }
    }

    open inner class FaxTab : ContactTab(
            "FaxTab",
            "dialog.notification.tab.fax",
            "faxTab",
            "include/fax.xhtml",
            NotificationTyp.FAX) {

        /**
         * Sending faxes via pathofroh
         */
        open var sendFax = true

        /**
         * Printin faxes
         */
        open var printFax = false

        override fun initTab(force: Boolean): Boolean {
            logger.debug("Initializing fax data...")

            individualAddresses = true
            // setting templates + transformer
            templates = printDocumentRepository.findAllByTypes(PrintDocument.DocumentType.DIAGNOSIS_REPORT,
                    PrintDocument.DocumentType.DIAGNOSIS_REPORT_EXTERN)

            selectedTemplate = printDocumentRepository.findByID(pathoConfig.defaultDocuments.notificationDefaultFaxDocument).orElse(null)

            sendFax = true

            printFax = false

            reportIntentStatus = ReportIntentStatusByType(task, notificationTyp)

            return super.initTab(force)
        }
    }

    open inner class LetterTab : ContactTab(
            "LetterTab",
            "dialog.notification.tab.letter",
            "letterTab",
            "include/letter.xhtml",
            NotificationTyp.LETTER) {

        /**
         * If true the letters will be pritned
         */
        open var printLetter = true

        override fun initTab(force: Boolean): Boolean {
            logger.debug("Initializing letter data...")

            individualAddresses = true
            // setting templates + transformer
            templates = printDocumentRepository.findAllByTypes(PrintDocument.DocumentType.DIAGNOSIS_REPORT,
                    PrintDocument.DocumentType.DIAGNOSIS_REPORT_EXTERN)

            selectedTemplate = printDocumentRepository.findByID(pathoConfig.defaultDocuments.notificationDefaultLetterDocument).orElse(null)

            printLetter = true

            reportIntentStatus = ReportIntentStatusByType(task, notificationTyp)

            return super.initTab(force)
        }
    }

    open inner class PhoneTab : ContactTab(
            "PhoneTab",
            "dialog.notification.tab.phone",
            "phoneTab",
            "include/phone.xhtml",
            NotificationTyp.PHONE) {

        override fun initTab(force: Boolean): Boolean {
            logger.debug("Initializing phone data...")

            reportIntentStatus = ReportIntentStatusByType(task, notificationTyp)

            return super.initTab(force)
        }
    }

    open inner class SendTab : NotificationTab(
            "SendTab",
            "dialog.notification.tab.send",
            "sendTab",
            "include/send.xhtml") {

    }


}