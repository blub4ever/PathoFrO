package com.patho.main.dialog.print

import com.patho.main.action.handler.CurrentUserHandler
import com.patho.main.action.handler.MessageHandler
import com.patho.main.common.ContactRole
import com.patho.main.common.Dialog
import com.patho.main.config.PathoConfig
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.dialog.print.documentUi.AbstractDocumentUi
import com.patho.main.dialog.print.documentUi.AbstractTaskDocumentUi
import com.patho.main.model.PDFContainer
import com.patho.main.model.patient.Task
import com.patho.main.repository.miscellaneous.PrintDocumentRepository
import com.patho.main.service.PrintService
import com.patho.main.service.UserService
import com.patho.main.template.PrintDocument
import com.patho.main.template.PrintDocumentType
import com.patho.main.ui.transformer.DefaultTransformer
import com.patho.main.util.pdf.LazyPDFGuiManager
import com.patho.main.util.pdf.creator.PDFCreator
import com.patho.main.util.pdf.PrintOrder
import com.patho.main.util.print.PrintPDFBearer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.io.FileNotFoundException

@Component()
@Scope(value = "session")
class PrintDialog @Autowired constructor(
        private val pathoConfig: PathoConfig,
        private val printService: PrintService,
        private val currentUserHandler: CurrentUserHandler,
        private val userService: UserService,
        private val printDocumentRepository: PrintDocumentRepository) : AbstractTaskDialog(Dialog.PRINT) {

    /**
     * Manager for rendering the pdf lazy style
     */
    val guiManager = LazyPDFGuiManager()

    /**
     * List of all templates for printing
     */
    var templates: List<AbstractDocumentUi<out PrintDocument, out AbstractDocumentUi.SharedData>> = listOf()
        set(value) {
            field = value
            templateTransformer = DefaultTransformer(value)
        }

    /**
     * The TemplateListtransformer for selecting a template
     */
    var templateTransformer: DefaultTransformer<AbstractDocumentUi<out PrintDocument, out AbstractDocumentUi.SharedData>> = DefaultTransformer(templates)

    /**
     * Ui object for template
     */
    var selectedTemplate: AbstractDocumentUi<out PrintDocument, out AbstractDocumentUi.SharedData>? = null

    /**
     * Can be set to true, if so the generated pdf will be saved
     */
    var savePDF: Boolean = false

    /**
     * if true no print button, but instead a select button will be display
     */
    var selectMode: Boolean = false

    /**
     * If True the template will be returned as well
     */
    var selectWithTemplate: Boolean = false

    /**
     * If true only on address can be selected
     */
    var singleAddressSelectMode: Boolean = false

    /**
     * If true a fax button will be displayed
     */
    var faxMode: Boolean = false

    /**
     * if true duplex printing will be used
     */
    var duplexPrinting: Boolean = false

    /**
     * Only in use if duplexPrinting is true. IF printEvenPageCounts is true a blank
     * page will be added if there is an odd number of pages to print.
     */
    var printEvenPageCounts: Boolean = false

    override fun initAndPrepareBean(task: Task): PrintDialog {
        if (initBean(task))
            prepareDialog()
        return this
    }

    fun initAndPrepareBean(task: Task, templateUI: List<AbstractDocumentUi<out PrintDocument, out AbstractDocumentUi.SharedData>>,
                           selectedTemplateUi: AbstractDocumentUi<out PrintDocument, out AbstractDocumentUi.SharedData>): PrintDialog {
        if (initBean(task, templateUI, selectedTemplateUi))
            prepareDialog();
        return this;
    }

    override fun initBean(task: Task): Boolean {
        val templates = printDocumentRepository.findAllByTypes(PrintDocumentType.DIAGNOSIS_REPORT,
                PrintDocumentType.U_REPORT, PrintDocumentType.U_REPORT_EMTY, PrintDocumentType.DIAGNOSIS_REPORT_EXTERN)

        return initBean(task, templates, if (templates.size > 0) templates[0] else null)
    }

    fun initBean(task: Task, templates: List<PrintDocument>,
                 selectTemplate: PrintDocument? = null): Boolean {

        // getting ui objects
        val printDocumentUIs = PrintDocumentRepository.factory(templates)

        // init templates
        printDocumentUIs.forEach { p ->
            if (p is AbstractTaskDocumentUi) {
                logger.debug("Initializing $p")
                p.initialize(task)
            } else
                logger.debug("No TaskReportUI: $p")
        }

        val selectedPrintDocument = selectedTemplate?.let { printDocumentUIs.firstOrNull { p -> p.printDocument == it } }

        return initBean(task, printDocumentUIs, selectedPrintDocument)
    }

    fun initBean(task: Task, templateUI: List<AbstractDocumentUi<*, *>>,
                 selectTemplateUi: AbstractDocumentUi<*, *>? = null): Boolean {

        if (templateUI != null) {

            // setting template list to choose from

            this.templates = templateUI

            if (selectTemplateUi != null)
                this.selectedTemplate = selectTemplateUi
            else
                this.selectedTemplate = templateUI[0]

            guiManager.renderComponent = true
        } else {
            guiManager.renderComponent = false
        }

        guiManager.reset()

        selectMode = false
        faxMode = false
        savePDF = false
        singleAddressSelectMode = false

        super.initBean(task)

        // rendering the template
        onChangePrintTemplate()

        return true
    }

    fun printMode(): PrintDialog {
        return this
    }

    fun selectMode(): PrintDialog {
        return selectMode(false)
    }

    fun selectMode(selectWithTemplate: Boolean): PrintDialog {
        selectMode = true
        return this
    }

    fun onChangePrintTemplate() {
        guiManager.reset()

        val template = selectedTemplate
        if (template != null) {
            guiManager.startRendering(template.getDefaultTemplateConfiguration().documentTemplate,
                    pathoConfig.fileSettings.printDirectory)

            duplexPrinting = template.printDocument.duplexPrinting
            printEvenPageCounts = template.printDocument.printEvenPageCount
        }
    }

    fun onPrintNewPdf() {

        logger.debug("Printing PDF")

        val template = selectedTemplate ?: return

        template.beginNextTemplateIteration()

        var printedDocuments = 0

        while (template.hasNextTemplateConfiguration()) {
            val container = template
                    .getNextTemplateConfiguration()

            var pdf: PDFContainer? = null

            try {
                pdf = PDFCreator().create(container!!.documentTemplate)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                MessageHandler.sendGrowlErrorAsResource("growl.error.critical", "growl.print.error.creatingPDF")
                continue
            }

            val printOrder = PrintOrder(pdf, container.copies, duplexPrinting,
                    container.documentTemplate.attributes)

            currentUserHandler.printer?.print(printOrder)

            // only save if person is associated
            if (container.contact != null && container.contact?.role != ContactRole.NONE) {
                //				reportIntentService.addHistoryEntryAndReportIntentNotification(task,container.getContact(),NotificationTyp.PRINT, get);
                // TODO save print request

                //				associatedContactService.addNotificationByType(container.getContact(), NotificationTyp.PRINT, false,
                //						true, false, Instant.now(), container.getAddress(), false);
            }

            printedDocuments += container.copies
            logger.debug("Printing next order ")
        }

        MessageHandler.sendGrowlMessagesAsResource("growl.print.printing", "growl.print.success.amount",
                arrayOf<Any>(printedDocuments))

        logger.debug("Printing completed")
    }

    /**
     * Hides the dialog. If a rendered pdf is available and a template was selected a LoadedPrintPDFBearer is returned.
     */
    fun hideAndSelect() {
        val pdf = guiManager.displayPDF
        val template = selectedTemplate?.getDefaultTemplateConfiguration()?.documentTemplate

        if (pdf != null && template != null) {
            hideDialog(PrintPDFBearer(pdf, template))
        } else
            hideDialog()
    }
}