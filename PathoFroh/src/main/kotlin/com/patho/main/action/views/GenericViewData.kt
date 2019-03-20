package com.patho.main.action.views

import com.patho.main.common.ContactRole
import com.patho.main.common.SortOrder
import com.patho.main.model.DiagnosisPreset
import com.patho.main.model.ListItem
import com.patho.main.model.MaterialPreset
import com.patho.main.model.Physician
import com.patho.main.model.patient.Task
import com.patho.main.repository.DiagnosisPresetRepository
import com.patho.main.repository.ListItemRepository
import com.patho.main.repository.MaterialPresetRepository
import com.patho.main.repository.PhysicianRepository
import com.patho.main.ui.transformer.DefaultTransformer
import com.patho.main.util.bearer.SimplePhysicianBearer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(value = "session")
open class GenericViewData @Autowired constructor(
        private val listItemRepository: ListItemRepository,
        private val diagnosisPresetRepository: DiagnosisPresetRepository,
        private val physicianRepository: PhysicianRepository,
        private val materialPresetRepository: MaterialPresetRepository) : AbstractTaskView() {

    /**
     * Contains all available case histories
     */
    open var slideCommentary: MutableList<ListItem> = mutableListOf<ListItem>()

    /**
     * List of all diagnosis presets
     */
    open var diagnosisPresets: MutableList<DiagnosisPreset> = mutableListOf<DiagnosisPreset>()

    /**
     * List of physicians which have the role signature
     */
    open var physiciansToSignList: List<Physician> = listOf<Physician>()

    /**
     * Transfomer for physiciansToSign
     */
    open var physiciansToSignListTransformer: DefaultTransformer<Physician> = DefaultTransformer<Physician>(physiciansToSignList)

    /**
     * List of available materials
     */
    open var materialList: List<MaterialPreset> = mutableListOf<MaterialPreset>()

    /**
     * Contains all available case histories
     */
    open var caseHistoryList: MutableList<ListItem> = mutableListOf<ListItem>()

    /**
     * Contains all available wards
     */
    open var wardList: MutableList<ListItem> = mutableListOf<ListItem>()

    /**
     * List of all surgeons
     */
    open var surgeons: List<SimplePhysicianBearer> = listOf<SimplePhysicianBearer>()

    /**
     * List of all surgeons
     */
    open var privatePhysicians: List<SimplePhysicianBearer> = listOf<SimplePhysicianBearer>()

    /**
     * Loads generic data for all views
     */
    override fun loadView() {
        logger.debug("Loading generic data")

        slideCommentary = listItemRepository
                .findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.SLIDES, false)

        caseHistoryList = listItemRepository
                .findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.CASE_HISTORY, false)

        wardList = listItemRepository.findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.WARDS,
                false)

        diagnosisPresets = diagnosisPresetRepository.findAllByOrderByIndexInListAsc()

        physiciansToSignList = physicianRepository.findAllByRole(ContactRole.SIGNATURE, true)
        physiciansToSignListTransformer = DefaultTransformer(physiciansToSignList)

        materialList = materialPresetRepository.findAll(true)
    }

    /**
     * Loads generic data for all views
     */
    override fun loadView(task: Task) {
        loadView(task, true);
    }

    fun loadView(task: Task, loadGeneric: Boolean) {
        super.loadView(task)

        if (loadGeneric)
            loadView()

        surgeons = physicianRepository.findAllByRole(
                arrayOf(ContactRole.SURGEON, ContactRole.EXTERNAL_SURGEON), true, SortOrder.PRIORITY).map { p -> SimplePhysicianBearer(p.id, p, task) }

        privatePhysicians = physicianRepository.findAllByRole(arrayOf(ContactRole.PRIVATE_PHYSICIAN),
                true, SortOrder.PRIORITY).map { p -> SimplePhysicianBearer(p.id, p, task) }
    }
}