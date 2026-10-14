package com.acme.transport.crewing.dutyperiod.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal duty period assignment is in its life cycle. */
enum class SeasonalDutyPeriodAssignmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One seasonal duty period assignment, with what the planners need to act on it. */
data class SeasonalDutyPeriodAssignment(
    var code: String,
    var name: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var etaMinutes: Int,
    var fuelLiters: Double,
    var score: Double,
    var notes: String,
    var status: SeasonalDutyPeriodAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDutyPeriodAssignmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDutyPeriodAssignmentStatus): Boolean {
        when (status) {
            SeasonalDutyPeriodAssignmentStatus.OPEN -> {
                when (next) {
                    SeasonalDutyPeriodAssignmentStatus.REOPENED, SeasonalDutyPeriodAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalDutyPeriodAssignmentStatus.IN_REVIEW -> return next == SeasonalDutyPeriodAssignmentStatus.ARCHIVED
            SeasonalDutyPeriodAssignmentStatus.RESOLVED -> {
                when (next) {
                    SeasonalDutyPeriodAssignmentStatus.REOPENED, SeasonalDutyPeriodAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalDutyPeriodAssignmentStatus.REOPENED -> return next == SeasonalDutyPeriodAssignmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: SeasonalDutyPeriodAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal duty period assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedLoad(): Double = round((etaMinutes.toDouble() - score) * 100) / 100

    fun snapshot(): SeasonalDutyPeriodAssignment {
        return SeasonalDutyPeriodAssignment(
            code = code,
            name = name,
            title = title,
            attributes = attributes,
            etaMinutes = etaMinutes,
            fuelLiters = fuelLiters,
            score = score,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal duty period assignment in status open. */
fun newSeasonalDutyPeriodAssignment(code: String, name: String): SeasonalDutyPeriodAssignment {
    return SeasonalDutyPeriodAssignment(
        code = code,
        name = name,
        title = "",
        attributes = mutableMapOf<String, String>(),
        etaMinutes = 0,
        fuelLiters = 0.0,
        score = 0.0,
        notes = "",
        status = SeasonalDutyPeriodAssignmentStatus.OPEN,
    )
}
