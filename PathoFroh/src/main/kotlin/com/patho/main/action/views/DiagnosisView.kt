package com.patho.main.action.views

import com.patho.main.model.DiagnosisPreset
import com.patho.main.model.ListItem
import com.patho.main.model.MaterialPreset
import com.patho.main.model.patient.Diagnosis
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.repository.PhysicianRepository
import com.patho.main.service.DiagnosisService
import com.patho.main.util.bearer.SimplePhysicianBearer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@Scope(value = "session")
open class DiagnosisView @Autowired constructor(
        private val physicianRepository: PhysicianRepository,
        private val diagnosisService: DiagnosisService) : AbstractTaskView() {

    open var diagnosisViewData = listOf<DiagnosisViewData>()

    /**
     * Selected material presets
     */
    open var selectedMaterialPresets: Array<MaterialPreset?> = arrayOf<MaterialPreset?>()

    /**
     * Material preset filter
     */
    var selectedMaterialPresetFilter: Array<String?> = arrayOf<String?>()

    /**
     * Search string for case history
     */
    var caseHistoryFilter: String = ""

    /**
     * Selected List item form caseHistory list
     */
    var selectedCaseHistoryItem: ListItem? = null

    /**
     * Selected surgeon
     */
    var selectedSurgeon: SimplePhysicianBearer? = null

    /**
     * Surgeon filter
     */
    var selectedSurgeonFilter: String = ""

    /**
     * Private physician surgeon
     */
    var selectedPrivatePhysician: SimplePhysicianBearer? = null

    /**
     * Private physician filter
     */
    var selectedPrivatePhysicianFilter: String = ""

    /**
     * Array for diagnoses filter
     */
    var diagnosisFilter: Array<Array<String>> = arrayOf<Array<String>>()

    /**
     * Array for selected diagnosis presets
     */
    var selectedDiagnosisPresets: Array<Array<DiagnosisPreset?>> = arrayOf<Array<DiagnosisPreset?>>()

    override fun loadView(task: Task) {
        logger.debug("Loading diagnosis data")
        super.loadView(task)

        diagnosisViewData = task.diagnosisRevisions.map { p -> DiagnosisViewData(p) }

        // updating signature date and person to sign
        for (revision in task.diagnosisRevisions) {
            if (!revision.completed) {
                revision.signatureDate = LocalDate.now()

                if (revision.signatureOne.physician == null || revision.signatureTwo.physician == null) {
                    // TODO set if physician to the left, if consultant to the right
                }
            }
        }

        selectedMaterialPresets = Array<MaterialPreset?>(task.samples.size) { null }
        selectedMaterialPresetFilter = Array<String?>(task.samples.size) { "" }

        selectedMaterialPresets.forEach { p -> println(p) }

        selectedCaseHistoryItem = null
        caseHistoryFilter = ""

        selectedSurgeon = null
        selectedSurgeonFilter = ""

        selectedPrivatePhysician = null
        selectedPrivatePhysicianFilter = ""

        diagnosisFilter = arrayOf<Array<String>>()
        selectedDiagnosisPresets = arrayOf<Array<DiagnosisPreset?>>()

        for (revision in task.diagnosisRevisions) {
            diagnosisFilter += Array<String>(revision.diagnoses.size) { "" }
            selectedDiagnosisPresets += Array<DiagnosisPreset?>(revision.diagnoses.size) { null }
        }
    }

//    /**
//     * Updates a diagnosis with a preset
//     *
//     * @param diagnosis
//     * @param preset
//     */
//    fun updateDiagnosisPrototype(diagnosis: Diagnosis, preset: DiagnosisPreset) {
//        logger.debug("Updating diagnosis with prototype")
//        val task = diagnosisService.updateDiagnosisWithPrototype(diagnosis.task, diagnosis, preset)
//
//        globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_TASK_STATUS)
//    }
//
//    /**
//     * Updates a diagnosis without a preset. (Removes the previously set preset)
//     */
//    fun updateDiagnosisPrototype(diagnosis: Diagnosis, diagnosisAsText: String) {
//        updateDiagnosisPrototype(diagnosis, diagnosisAsText, "", diagnosis.malign, "")
//    }
//
//    /**
//     * Updates a diagnosis without a preset. (Removes the previously set preset)
//     */
//    fun updateDiagnosisPrototype(diagnosis: Diagnosis, diagnosisAsText: String, extendedDiagnosisText: String,
//                                 malign: Boolean, icd10: String) {
//        logger.debug("Updating diagnosis to $diagnosisAsText")
//        setSelectedTask(diagnosisService.updateDiagnosisWithoutPrototype(diagnosis.task, diagnosis,
//                diagnosisAsText, extendedDiagnosisText, malign, icd10))
//        globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_TASK_STATUS)
//    }


    class DiagnosisViewData(diagnosisRevision: DiagnosisRevision) {
        val diagnosisRevision = diagnosisRevision
    }
}