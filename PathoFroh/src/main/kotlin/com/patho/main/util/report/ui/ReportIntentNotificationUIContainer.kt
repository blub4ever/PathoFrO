package com.patho.main.util.report.ui

import com.patho.main.model.PDFContainer
import com.patho.main.model.patient.notification.NotificationTyp
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.util.print.PrintPDFBearer

/**
 * Bearer for the notification intent
 */
open class ReportIntentNotificationUIContainer(var reportIntent: ReportIntent, var reportIntentNotification: ReportIntentNotification) {

    /**
     * Conatct address, if reportIntentNotification has no contact address set, a new one will be generated
     */
    var contactAddress: String = if (reportIntentNotification.contactAddress.isEmpty()) getContactAddress(reportIntentNotification) else reportIntentNotification.contactAddress

    /**
     * Status of the reportintentNotification
     */
    val status: ReportIntentUIContainer.ReportIntentBearerStatus
        get() = if (reportIntentNotification.active) {
            // status is active, check if history is present, if so check if the notification was successful
            if (SpringContextBridge.services().reportIntentService.isHistoryPresent(reportIntentNotification)) {
                if (SpringContextBridge.services().reportIntentService.isNotificationPerformed(reportIntentNotification))
                    ReportIntentUIContainer.ReportIntentBearerStatus.SUCCESS
                else
                    ReportIntentUIContainer.ReportIntentBearerStatus.FAILED

            } else
                ReportIntentUIContainer.ReportIntentBearerStatus.ACTIVE
            // is inactive
        } else
            ReportIntentUIContainer.ReportIntentBearerStatus.INACTIVE

    /**
     * If true a notification is performed
     */
    var performNotification: Boolean = status == ReportIntentUIContainer.ReportIntentBearerStatus.ACTIVE

    /**
     * Custom pdf and template for contact
     */
    var printPDF : PrintPDFBearer? = null

    /**
     * Custom pdf for contact
     */
    val pdf: PDFContainer? = printPDF?.pdfContainer

    /**
     * True if the disable button should be rendered
     */
    val renderInactiveButton
        get() = status != ReportIntentUIContainer.ReportIntentBearerStatus.INACTIVE && status != ReportIntentUIContainer.ReportIntentBearerStatus.SUCCESS

    /**
     * True if the enable button should be rendered
     */
    val renderActiveButton
        get() = status == ReportIntentUIContainer.ReportIntentBearerStatus.INACTIVE


    fun update(update: ReportIntentNotificationUIContainer) {
        this.reportIntent = update.reportIntent
        this.reportIntentNotification = update.reportIntentNotification
        this.performNotification = status == ReportIntentUIContainer.ReportIntentBearerStatus.ACTIVE
    }

    /**
     * Returns the matching contact address for the reportIntentNotification
     */
    private fun getContactAddress(reportIntentNotification: ReportIntentNotification): String {
        return when (reportIntentNotification.notificationTyp) {
            NotificationTyp.EMAIL -> reportIntentNotification.contact?.person?.contact?.email ?: ""
            NotificationTyp.FAX -> reportIntentNotification.contact?.person?.contact?.fax ?: ""
            NotificationTyp.PHONE -> reportIntentNotification.contact?.person?.contact?.phone ?: ""
            NotificationTyp.LETTER -> reportIntentNotification.contact?.let { SpringContextBridge.services().reportIntentService.generateAddress(it) }
                    ?: ""
            else -> ""
        }
    }
}