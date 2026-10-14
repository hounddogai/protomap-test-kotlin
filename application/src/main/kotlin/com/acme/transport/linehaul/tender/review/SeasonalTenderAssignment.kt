package com.acme.transport.linehaul.tender.review

import com.acme.transport.kit.text.normalize

/** Where a seasonal tender assignment is in its life cycle. */
enum class SeasonalTenderAssignmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a seasonal tender assignment and its current status. */
data class SeasonalTenderAssignment(
    var code: String,
    var label: String,
    var driverId: String,
    var etaMinutes: Int,
    var contactEmail: String,
    var pinned: Boolean,
    var archived: Boolean,
    var status: SeasonalTenderAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTenderAssignmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTenderAssignmentStatus): Boolean {
        when (status) {
            SeasonalTenderAssignmentStatus.NEW -> return next == SeasonalTenderAssignmentStatus.RUNNING || next == SeasonalTenderAssignmentStatus.DONE
            SeasonalTenderAssignmentStatus.QUEUED -> return next == SeasonalTenderAssignmentStatus.DONE || next == SeasonalTenderAssignmentStatus.FAILED
            SeasonalTenderAssignmentStatus.RUNNING -> {
                when (next) {
                    SeasonalTenderAssignmentStatus.FAILED -> return true
                }
                return false
            }
            SeasonalTenderAssignmentStatus.DONE -> {
                when (next) {
                    SeasonalTenderAssignmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalTenderAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal tender assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal tender assignment"} $code"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): SeasonalTenderAssignment {
        return SeasonalTenderAssignment(
            code = code,
            label = label,
            driverId = driverId,
            etaMinutes = etaMinutes,
            contactEmail = contactEmail,
            pinned = pinned,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal tender assignment in status new. */
fun newSeasonalTenderAssignment(code: String, label: String): SeasonalTenderAssignment {
    return SeasonalTenderAssignment(
        code = code,
        label = label,
        driverId = "",
        etaMinutes = 0,
        contactEmail = "",
        pinned = false,
        archived = false,
        status = SeasonalTenderAssignmentStatus.NEW,
    )
}
