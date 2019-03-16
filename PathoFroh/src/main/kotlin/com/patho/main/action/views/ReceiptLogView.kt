package com.patho.main.action.views

import com.patho.main.action.handler.MessageHandler
import com.patho.main.action.handler.WorkPhaseHandler
import com.patho.main.model.interfaces.IdManuallyAltered
import com.patho.main.model.patient.Block
import com.patho.main.model.patient.Sample
import com.patho.main.model.patient.Slide
import com.patho.main.model.patient.Task
import com.patho.main.repository.TaskRepository
import com.patho.main.service.PrintExecutorService
import com.patho.main.service.SlideService
import com.patho.main.util.print.UnknownPrintingException
import com.patho.main.util.status.ReportIntentStatusByUser
import freemarker.template.TemplateNotFoundException
import org.springframework.beans.factory.annotation.Autowired

class ReceiptLogView @Autowired constructor(
        private val slideService: SlideService,
        private val workPhaseHandler: WorkPhaseHandler,
        private val taskRepository: TaskRepository,
        private val printExecutorService: PrintExecutorService) : AbstractTaskView() {

    /**
     * Currently selected task entity in table form, transient, used for gui
     */
    var rows = mutableListOf<TaskEntityRow<out IdManuallyAltered>>()

    /**
     * Is used for selecting a chooser from the generated list (generated by task).
     * It is used to edit the names of the entities by an overlaypannel
     */
    var selectedRow: TaskEntityRow<IdManuallyAltered>? = null

    /**
     * This variable is used to save the selected action, which should be executed
     * upon all selected slides
     */
    var actionOnMany: StainingListAction = StainingListAction.NONE

    /**
     * Status for notification list
     */
    var reportIntentStatusByUser: ReportIntentStatusByUser = ReportIntentStatusByUser()

    override fun loadView(task: Task) {
        logger.debug("Loading receipt log view")
        actionOnMany = StainingListAction.NONE
        rows = TaskEntityRow.factory(task, false)
        reportIntentStatusByUser = ReportIntentStatusByUser(task)
    }

    /**
     * Sets the element an all children as selected
     */
    fun selectChildren(entity: TaskEntityRow<out IdManuallyAltered>, selected: Boolean) {
        entity.selected = selected
        entity.children.forEach { p -> selectChildren(p, selected) }
    }

    /**
     * Sets all elemtns of the list to selectedF
     */
    fun selectListChildren(lists: List<TaskEntityRow<out IdManuallyAltered>>, selected: Boolean) {
        lists.forEach { p -> selectListChildren(p, selected) }
    }

    /**
     * Toggles the selection of an entity
     */
    fun toggleSelectoin(entity: TaskEntityRow<out IdManuallyAltered>) {
        selectChildren(entity, !entity.selected)
    }

    fun performActionOnSelected(action: StainingListAction) {
        // actions ca only be performed on slides
        val slideRows = rows.filter { p -> p.selected && p.isStainingType }

        if (slideRows.isEmpty()) run {
            logger.debug("Nothing selected, do not perform any action")
            return
        }

        when (action) {
            // set slided to performed
            StainingListAction.PERFORMED, StainingListAction.NOT_PERFORMED -> {
                logger.debug("Setting staining status of selected slides")
                slideRows.forEach { p -> slideService.completedStaining(p as Slide, action == StainingListAction.PERFORMED) }
                workPhaseHandler.updateStainingPhase(taskRepository.save(task, resourceBundle.get("log.task.slide.completed", task), task.patient))
            }
            // archive slides
            StainingListAction.ARCHIVE -> {
                //TODO implement this feature
                println("TODO implement")
            }
            StainingListAction.PRINT -> {
                slideRows.filter { p -> p.selected && p.isStainingType }
            }
        }
    }

    fun printLabels(vararg slides: Slide?) {
        if (slides == null || slides.isEmpty()) {
            MessageHandler.sendGrowlErrorAsResource("growl.error.critical", "growl.print.slide.noTemplate")
            return
        }
        try {
            printExecutorService.printLabel(slides as Slide)
            MessageHandler.sendGrowlMessagesAsResource("growl.print", "growl.print.slide.print")
        } catch (e: UnknownPrintingException) {
            MessageHandler.sendGrowlErrorAsResource("growl.error.critical", "growl.print.slide.noTemplate")
            return
        } catch (e: TemplateNotFoundException) {
            MessageHandler.sendGrowlErrorAsResource("growl.error.critical", "growl.print.slide.noTemplate")
            return
        }
    }

    class TaskEntityRow<T : IdManuallyAltered>(var entity: T, val even: Boolean = false) {
        var selected: Boolean = false

        private var idChanged: Boolean = false

        /**
         * Children of this enitity
         */
        val children = mutableListOf<TaskEntityRow<out IdManuallyAltered>>()


        val isSampleType
            get() = entity is Sample

        val isBlockType
            get() = entity is Block

        val isStainingType
            get() = entity is Slide

        fun setIDText(text: String?) {
            when (entity) {
                is Sample -> {
                    if (text != (entity as Sample).sampleID) idChanged = true
                    (entity as Sample).sampleID = text ?: ""
                }
                is Block -> {
                    if (text != (entity as Block).blockID) idChanged = true
                    (entity as Block).blockID = text ?: ""
                }
                is Slide -> {
                    if (text != (entity as Slide).slideID) idChanged = true
                    (entity as Slide).slideID = text ?: ""
                }
            }
        }

        fun getIDText(): String {
            return when (entity) {
                is Sample -> (entity as Sample).sampleID
                is Block -> (entity as Block).blockID
                is Slide -> (entity as Slide).slideID
                else -> ""
            }
        }

        companion object {
            /**
             * Creates linear list of all slides of the given task. The StainingTableChosser
             * is used as holder class in order to offer an option to select the slides by
             * clicking on a checkbox. Archived elements will not be shown if showArchived
             * is false.
             *
             * @param showArchived
             */
            @JvmStatic
            fun factory(task: Task, showArchived: Boolean): MutableList<TaskEntityRow<out IdManuallyAltered>> {

                val result = mutableListOf<TaskEntityRow<out IdManuallyAltered>>()

                var even = false

                for (sample in task.samples) {
                    // skips archived tasks

                    val sampleChooser = TaskEntityRow(sample, even)
                    result.add(sampleChooser)

                    for (block in sample.blocks) {
                        // skips archived blocks

                        val blockChooser = TaskEntityRow(block, even)
                        result.add(blockChooser)
                        sampleChooser.children.add(blockChooser)

                        for (staining in block.slides) {
                            // skips archived sliedes

                            val stainingChooser = TaskEntityRow(staining, even)
                            result.add(stainingChooser)
                            blockChooser.children.add(stainingChooser)
                        }
                    }

                    even = !even

                }
                return result
            }
        }
    }

    /**
     * Action commands for selecting action on many items
     */
    enum class StainingListAction {
        NONE, PERFORMED, NOT_PERFORMED, PRINT, ARCHIVE
    }
}