package com.patho.main.common

enum class Dialog {

    WORKLIST_SEARCH("/pages/dialog/worklist/worklistSearch/worklistSearch", null, 1280, 720, false, false, true),                                                            // OK -> extended search missing, disable on load
    WORKLIST_EXPORT("/pages/dialog/export/exportDialog", null, 1024, 600, false, false, true), // 16:9
    PATIENT_ADD("/pages/dialog/patient/searchAndAddPatient/searchAndAddPatient", null, 1024, 600, false, false, true),                                                                    // OK
    PATIENT_DATA_CONFIRM("/pages/dialog/patient/confirmExternalPatientData", null, 640, 360, false, false, true),                                // OK
    PATIENT_MERGE("/pages/dialog/patient/merge/mergePatient", null, 858, 484, false, false, true), // 16:9
    PATIENT_EDIT("/pages/dialog/patient/editPatient", null, 1024, 600, false, false, true), // 16:9
    PATIENT_REMOVE("/pages/dialog/patient/removePatient", null, 480, 272, false, false, true), // 16:9
    WORKLIST_ORDER("/pages/dialog/worklist/worklistOrder", null, 480, 272, false, false, true),                                                                            // OK
    WORKLIST_SETTINGS("/pages/dialog/worklist/worklistSettings", null, 480, 272, false, false, true),                                                                        // OK
    PATIENT_LOG("/pages/dialog/history/patientLog", null, 1024, 600, false, false, true),                                                                                    // OK
    /**
     * Create task (Kotlin)
     */
    TASK_CREATE("/pages/dialog/task/createTask", null, 1024, 600, false, false, true),                                                                                        // OK
    TASK_DELETE("/pages/dialog/task/delete/deleteTask", null, 480, 272, false, false, true), // 16:9
    TASK_CHANGE_ID("/pages/dialog/task/chagneTaskID", null, 480, 272, false, false, true),                                                                                    // OK
    SAMPLE_CREATE("/pages/dialog/task/sample/createSample", null, 480, 272, false, false, true),                                                                            // OK				// TEST OK
    BIO_BANK("/pages/dialog/biobank/biobank", null, 858, 484, false, false, true),                                                                                            // OK
    SLIDE_OVERVIEW("/pages/dialog/slide/slideOverview", null, 1024, 600, false, false, true),

    /**
     * Dialog for displaying slides
     */
    SLIDE_SHOW_SCANNED("/pages/dialog/slide/slideShowScanned", null, "90vw", "90vh", false, false, true),        // OK 				// TEST OK

    SLIDE_NAMING("/pages/dialog/slide/slideNaming", null, 430, 270, false, false, true),                                                                            // OK				// TEST OK
    SLIDE_CREATE("/pages/dialog/slide/addSlide", null, 858, 484, false, false, true),                                                                                // OK				// TEST OK
    DIAGNOSIS_RECORD_OVERWRITE("/pages/dialog/diagnosis/diagnosisRecordOverwrite", null, 480, 272, false, false, true),                                                // OK				// TEST OK
    CONTACTS("/pages/dialog/contact/contacts", null, 1024, 600, false, false, true),                                                                                        // OK				// TEST OK
    CONTACTS_NOTIFICATION("/pages/dialog/contact/contactNotification", null, 858, 484, false, false, true),                                                                // OK				// TEST OK
    CONTACTS_SELECT("/pages/dialog/contact/contactSelect", null, 858, 484, false, false, true),                                                                            // OK				// TEST OK
    SETTINGS("/pages/dialog/globalSettings/settings", null, 1024, 600, false, false, true),                                                                                    // OK
    SETTINGS_PHYSICIAN_SEARCH("/pages/dialog/settings/physician/physicianSearch/physicianSearch", null, 1280, 720, false, false, true),                                    // OK
    SETTINGS_PHYSICIAN_EDIT("/pages/dialog/settings/physician/physicianEdit", null, 1280, 740, false, false, true),                                                        // OK
    /**
     * Edit staining prototypes (kotlin)
     */
    SETTINGS_STAINING_EDIT("/pages/dialog/settings/staining/stainingEdit", null, 1024, 600, false, false, true),                                                            // OK
    /**
     * Sorting staining list (Kotlin)
     */
    SETTINGS_STAINING_SORT("/pages/dialog/settings/staining/stainingSort", null, 1024, 600, false, false, true),                                                            // OK
    SETTINGS_MATERIAL_EDIT("/pages/dialog/settings/material/materialEdit", null, 1024, 600, false, false, true),                                                            // OK
    /**
     * Sorting material list (Kotlin)
     */
    SETTINGS_MATERIAL_SORT("/pages/dialog/settings/material/materialSort", null, 1024, 600, false, false, true),                                                            // OK
    SETTINGS_DIAGNOSIS_EDIT("/pages/dialog/settings/reportIntent/diagnosisEdit", null, 1024, 600, false, false, true),                                                        // OK
    SETTINGS_GROUP_LIST("/pages/dialog/settings/groups/groupList", null, 1024, 600, false, false, true),                                                                    // OK
    SETTINGS_GROUP_EDIT("/pages/dialog/settings/groups/groupEdit", null, 1024, 600, false, false, true),                                                                    // OK
    SETTINGS_USERS_LIST("/pages/dialog/settings/users/usersList", null, 1024, 600, false, false, true),                                                                    // OK
    SETTINGS_USERS_EDIT("/pages/dialog/settings/users/editUser/userEdit", null, 1280, 740, false, false, true),                                                            // OK
    SETTINGS_USERS_DELETE("/pages/dialog/settings/users/userDelete", null, 480, 272, false, false, true),                                                                    // OK
    SETTINGS_ORGANIZATION_EDIT("/pages/dialog/settings/organization/organizationEdit", null, 1280, 720, false, false, true),                                                // OK
    SETTINGS_ORGANIZATION_LIST("/pages/dialog/settings/organization/organizationList", null, 858, 484, false, false, true),                                                // OK
    SETTINGS_FAVOURITE_LIST_EDIT("/pages/dialog/settings/favouriteList/favouriteListEdit", null, 1024, 600, false, false, true),                                            // OK
    /**
     * Edit list items (Kotlin)
     */
    SETTINGS_LISTITEM_EDIT("/pages/dialog/settings/listItems/listItemEdit", null, 480, 272, false, false, true),                                                            // OK
    FAVOURITE_LIST_ITEM_REMOVE("/pages/dialog/settings/favouriteList/favouriteListItemRemove", null, 480, 272, false, false, true), // 16:9
    PRINT("/pages/dialog/print/print", null, 1280, 720, false, false, true), // 16:9
    PRINT_ADDRESS("/pages/dialog/print/address", null, 480, 272, false, false, true),                                                                                        // OK
    PRINT_FAX("/pages/dialog/print/fax", null, 480, 272, false, false, true),                                                                                                // OK
    CONSULTATION("/pages/dialog/consultation/consultationDialog", null, 1280, 720, false, false, true),                                                                                        // OK
    USER_SETTINGS("/pages/dialog/userSettings/userSettings", null, 1024, 600, false, false, true),                                                                        // OK
    USER_SETTINGS_SAVE("/pages/dialog/userSettings/confirmSave", null, 480, 272, false, false, true),                                                                        // OK
    MEDICAL_FINDINGS("/pages/dialog/medicalFindings/medicalFindings", null, 1024, 600, false, false, true),  // 16:9
    NOTIFICATION_ALREADY_PERFORMED("/pages/dialog/notification/notification_already_performed", null, 480, 272, false, false, true), //  16:9
    NOTIFICATION("/pages/dialog/notification/notification/notification", null, 1024, 600, false, false, true),  // 16:9
    NOTIFICATION_PERFORM("/pages/dialog/notification/performNotification", null, 640, 360, false, false, true),  // 16:9
    NOTIFICATION_PREVIEW("/pages/dialog/notification/notification_preview", null, 1024, 600, false, false, true), //  16:9
    INFO("/pages/dialog/info/info", null, 1024, 600, false, false, true),                                                                                                    // OK
    PDF_ORGANIZER("/pages/dialog/media/pdfOrganizer", null, 1280, 720, false, false, true),                                                                                // OK
    PDF_UPLOAD("/pages/dialog/media/media", null, 640, 360, false, false, true),                                                                                            // OK
    PDF_EDIT("/pages/dialog/media/editPDF", null, 640, 360, false, false, true),                                                                                            // OK
    /**
     * Delete PDF Dialog (Kotlin)
     */
    PDF_DELETE("/pages/dialog/media/deletePDF", null, 480, 272, false, false, true),                                                                                        // OK
    STAINING_PHASE_EXIT("/pages/dialog/phase/stainingPhaseExit", null, 480, 272, false, false, true), // 16:9
    DIAGNOSIS_PHASE_EXIT("/pages/dialog/phase/diagnosisPhaseExit", null, 640, 360, false, false, true), // 16:9
    NOTIFICATION_PHASE_EXIT("/pages/dialog/phase/notificationPhaseExit", null, 640, 370, false, false, true), // 16:9

    /**
     * Archive Task Dialog (Kotlin)
     */
    TASK_ARCHIVE("/pages/dialog/task/archiveTask/archiveTask", null, 1280, 720, false, false, true), // 16:9
    /**
     * Dearchive Task Dialog (Kotlin)
     */
    TASK_DEARCHIVE("/pages/dialog/task/dearchiveTask", null, 480, 272, false, false, true), // 16:9
    DIAGNOSIS_REVISION_CREATE("/pages/dialog/task/diagnosisRevision/diagnosisRevisionsCreate", null, 640, 360, false, false, true),                                            // OK
    DIAGNOSIS_REVISION_EDIT("/pages/dialog/task/diagnosisRevision/diagnosisRevisionsEdit", null, 480, 272, false, false, true),                                                // OK
    DIAGNOSIS_REVISION_DELETE("/pages/dialog/task/diagnosisRevision/diagnosisRevisionDelete", null, 480, 272, false, false, true),                                            // OK
    /**
     * Diagnosis revision add, for re diagnosis (quick add diagnosis) (Kotlin)
     */
    DIAGNOSIS_REVISION_ADD("/pages/dialog/task/diagnosisRevision/diagnosisRevisionDialogQuickAdd", null, 480, 272, false, false, true),                                    // OK		// TEST OK
    PRINT_SELECT_PRINTER("/pages/dialog/selectPrinter", null, 640, 360, false, false, true),
    ACCOUNTING_DATA("/pages/dialog/miscellaneous/accounting", null, 1024, 600, false, false, true),                                                                        // OK
    CONFIRM_CHANGE("/pages/dialog/miscellaneous/confirm", null, 320, 180, false, false, true),                                                                                // OK
    DELETE_ID_OBJECT("/pages/dialog/task/deleteTaskEntity", null, 320, 180, false, false, true);                                                                                // OK

    val path: String
    val useOptions: Boolean
    val header: String?

    val width: String
    val height: String
    val resizeable: Boolean
    val draggable: Boolean
    val modal: Boolean

    constructor(path: String) : this(path, null, 0, 0, false, false, false)

    constructor(path: String, width: String, height: String) : this(path, null, width, height, true, true, false)

    constructor(path: String, header: String?, width: Int, height: Int, resizeable: Boolean, draggable: Boolean, modal: Boolean) : this(path, header, width.toString(), height.toString(), resizeable, draggable, modal)

    constructor(path: String, header: String?, width: String, height: String, resizeable: Boolean, draggable: Boolean, modal: Boolean) {
        this.path = path
        this.width = width
        this.height = height
        this.resizeable = resizeable
        this.draggable = draggable
        this.modal = modal
        this.useOptions = true
        this.header = header
    }
}