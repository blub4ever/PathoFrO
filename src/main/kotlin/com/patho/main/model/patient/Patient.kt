package com.patho.main.model.patient

import com.patho.main.model.AbstractPersistable
import com.patho.main.model.PDFContainer
import com.patho.main.model.audit.AuditAble
import com.patho.main.model.interfaces.DataList
import com.patho.main.model.person.Person
import com.patho.main.model.util.audit.Audit
import com.patho.main.model.util.audit.AuditListener
import org.hibernate.annotations.*
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import javax.persistence.*
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.OrderBy

@Entity
@Audited
@SelectBeforeUpdate(true)
@EntityListeners(AuditListener::class)
open class Patient : AbstractPersistable, AuditAble, DataList {

    @Id
    @GeneratedValue(generator = "patient_sequencegenerator")
    @SequenceGenerator(name = "patient_sequencegenerator", sequenceName = "patient_sequence")
    @Column(unique = true, nullable = false)
    override var id: Long = 0

    @Version
    open var version: Long = 0

    /**
     * PIZ
     */
    @Column
    open var piz = ""

    /**
     * Insurance of the patient
     */
    @Column
    open var insurance = ""

    /**
     * Audit Data
     */
    @Embedded
    override var audit: Audit? = null

    /**
     * Person data
     */
    @OneToOne(cascade = [CascadeType.ALL])
    @NotAudited
    open var person: Person = Person()

    /**
     * Task for this patient
     */
    @OneToMany(mappedBy = "parent")
    @LazyCollection(LazyCollectionOption.TRUE)
    @Fetch(value = FetchMode.SUBSELECT)
    @OrderBy("taskid DESC")
    open var tasks = mutableSetOf<Task>()

    /**
     * True if patient was added as an external patient.
     */
    @Column
    open var externalPatient = false

    /**
     * Pdf attached to this patient, this might be an informed consent
     */
    @OneToMany
    @LazyCollection(LazyCollectionOption.TRUE)
    @Fetch(value = FetchMode.SUBSELECT)
    @OrderBy("audit.createdOn DESC")
    override var attachedPdfs = mutableSetOf<PDFContainer>()

    /**
     * If true the patient is archived. Thus he won'special.pdfOrganizerDialog be displayed.
     */
    @Column
    open var archived = false

    /**
     * True if saved in database, false if only in clinic backend
     */
    @Transient
    open var inDatabase = true

    /**
     * Public name for datalist
     */
    override val publicName: String
        @Transient
        get() = person.getFullName()

    /**
     * Returns this, overwrite from datalist
     */
    override val patient: Patient?
        @Transient
        get() = this

    constructor()

    constructor(id: Long) {
        this.id = id
    }

    constructor(person: Person) {
        this.person = person
    }

    @Transient
    fun getActiveTasks(): List<Task> {
        return getActiveTasks(false)
    }

    @Transient
    fun getTasksOfPatient(activeOnly: Boolean): List<Task>? {
        return tasks.filter { p -> (activeOnly && p.active) || !activeOnly }
    }

    /**
     * Returns a list with all currently active tasks of a Patient
     */
    @Transient
    fun getActiveTasks(activeOnly: Boolean): List<Task> {
        return tasks.filter { p -> p.isActiveOrActionPending(activeOnly) }
    }

    @Transient
    fun hasActiveTasks(): Boolean {
        return hasActiveTasks(false)
    }

    /**
     * Returns true if at least one task is marked as active
     */
    @Transient
    fun hasActiveTasks(activeOnly: Boolean): Boolean {
        return tasks.any { p -> p.isActiveOrActionPending(activeOnly) }
    }

    /**
     * Returns a list with tasks which are not active
     */
    @Transient
    fun getNoneActiveTasks(): List<Task>? {
        return tasks.filter { p -> !p.isActiveOrActionPending() }
    }

    /**
     * Returns true if at least one task is not marked as active
     */
    @Transient
    fun hasNoneActiveTasks(): Boolean {
        return !hasActiveTasks()
    }
}
