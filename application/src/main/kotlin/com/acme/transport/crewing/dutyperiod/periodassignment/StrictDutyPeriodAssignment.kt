package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict duty period assignment records of crewing's duty period work, and the rules, stores, and
// calculations around them.

/** Where a strict duty period assignment is in its life cycle. */
enum class StrictDutyPeriodAssignmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a strict duty period assignment and its current status. */
data class StrictDutyPeriodAssignment(
    var code: String,
    var title: String,
    var segment: String,
    var sequence: Int,
    var odometer: Int,
    var attributes: MutableMap<String, String>,
    var driverId: String,
    var attempts: Int,
    var name: String,
    var status: StrictDutyPeriodAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDutyPeriodAssignmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDutyPeriodAssignmentStatus): Boolean {
        when (status) {
            StrictDutyPeriodAssignmentStatus.DRAFT -> {
                when (next) {
                    StrictDutyPeriodAssignmentStatus.ACTIVE -> return true
                }
                return false
            }
            StrictDutyPeriodAssignmentStatus.PENDING -> {
                when (next) {
                    StrictDutyPeriodAssignmentStatus.SUSPENDED -> return true
                }
                return false
            }
            StrictDutyPeriodAssignmentStatus.ACTIVE -> {
                when (next) {
                    StrictDutyPeriodAssignmentStatus.SUSPENDED, StrictDutyPeriodAssignmentStatus.CLOSED -> return true
                }
                return false
            }
            StrictDutyPeriodAssignmentStatus.SUSPENDED -> {
                when (next) {
                    StrictDutyPeriodAssignmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictDutyPeriodAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("strict duty period assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedVolume(): Double = round(odometer.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): StrictDutyPeriodAssignment {
        return StrictDutyPeriodAssignment(
            code = code,
            title = title,
            segment = segment,
            sequence = sequence,
            odometer = odometer,
            attributes = attributes,
            driverId = driverId,
            attempts = attempts,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a strict duty period assignment in status draft. */
fun newStrictDutyPeriodAssignment(code: String, title: String): StrictDutyPeriodAssignment {
    return StrictDutyPeriodAssignment(
        code = code,
        title = title,
        segment = "",
        sequence = 0,
        odometer = 0,
        attributes = mutableMapOf<String, String>(),
        driverId = "",
        attempts = 0,
        name = "",
        status = StrictDutyPeriodAssignmentStatus.DRAFT,
    )
}
