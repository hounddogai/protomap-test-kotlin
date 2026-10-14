package com.acme.transport.crewing.qualification.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The qualification schedule records of crewing's qualification work, and the rules, stores, and calculations
// around them.

/** Where a qualification schedule is in its life cycle. */
enum class QualificationScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A qualification schedule. Its code is unique within a tenant. */
data class QualificationSchedule(
    var code: String,
    var label: String,
    var loadKg: Double,
    var contactEmail: String,
    var minimum: Double,
    var fuelLiters: Double,
    var summary: String,
    var title: String,
    var revision: Int,
    var status: QualificationScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationScheduleStatus): Boolean {
        when (status) {
            QualificationScheduleStatus.DRAFT -> return next == QualificationScheduleStatus.SUSPENDED || next == QualificationScheduleStatus.CLOSED
            QualificationScheduleStatus.PENDING -> return next == QualificationScheduleStatus.SUSPENDED
            QualificationScheduleStatus.ACTIVE -> return next == QualificationScheduleStatus.CLOSED
            QualificationScheduleStatus.SUSPENDED -> {
                when (next) {
                    QualificationScheduleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: QualificationScheduleStatus) {
        if (!canMoveTo(next)) {
            error("qualification schedule $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveTotal(): Double = round(minimum * revision.toDouble() * 100) / 100

    fun snapshot(): QualificationSchedule {
        return QualificationSchedule(
            code = code,
            label = label,
            loadKg = loadKg,
            contactEmail = contactEmail,
            minimum = minimum,
            fuelLiters = fuelLiters,
            summary = summary,
            title = title,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a qualification schedule in status draft. */
fun newQualificationSchedule(code: String, label: String): QualificationSchedule {
    return QualificationSchedule(
        code = code,
        label = label,
        loadKg = 0.0,
        contactEmail = "",
        minimum = 0.0,
        fuelLiters = 0.0,
        summary = "",
        title = "",
        revision = 0,
        status = QualificationScheduleStatus.DRAFT,
    )
}
