package com.patho.main.util.search.settings

import com.patho.main.common.Eye
import com.patho.main.model.Physician
import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task
import com.patho.main.model.person.Person
import com.patho.main.service.impl.SpringContextBridge
import java.time.LocalDate

class ExtendedSearch : SearchSettings() {

    var isUsePatientName = false
    var patientName: String = ""

    var isUsePatientSurname = false
    var patientSurname: String = ""

    var isUsePatientBirthdayFrom = false
    var patientBirthdayFrom: LocalDate? = null

    var isUsePatientBirthdayTo = false
    var patientBirthdayTo: LocalDate? = null

    var isUsePatientGender = false
    var patientGender: Person.Gender? = Person.Gender.UNKNOWN

    var isUseMaterial = false
    var material: ArrayList<String>? = ArrayList<String>()

    var isUseCaseHistory = false
    var caseHistory: ArrayList<String>? = ArrayList<String>()

    var isUsePhysicians = false
    var physicians: Array<Physician>? = null

    var isUseSignatures = false
    var signatures: Array<Physician>? = null

    var isUseEye = false
    var eye: Eye? = Eye.UNKNOWN

    var isUseWard = false
    var ward: String = ""

    var malign: String = "0"

    var isUseDiagnosis = false
    var diagnosis: ArrayList<String>? = ArrayList<String>()

    var isUseDiagnosisText = false
    var diagnosisText: String = ""

    var isUseDate = false
    var from: LocalDate = LocalDate.now()
    var to: LocalDate = LocalDate.now()

    override fun getPatients(): List<Patient> {
        val p: HashSet<Patient> = hashSetOf()
        val tasks = getTasks()

        tasks.forEach {
            if (p.contains(it.parent)) {
                it.parent?.tasks?.filter { t -> t == it }?.forEach { t -> t.active = true }
            } else {
                p.add(it.parent as Patient)
                it.active = true
            }
        }

        return p.toList()
    }

    override fun getTasks(): List<Task> {
        val taskRepository = SpringContextBridge.services().taskRepository
        return taskRepository.findByExtendedSearchSettings(this, true, true, false, false, false)
    }
}