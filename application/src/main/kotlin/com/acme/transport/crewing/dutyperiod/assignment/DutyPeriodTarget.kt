package com.acme.transport.crewing.dutyperiod.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period target is in its life cycle. */
enum class DutyPeriodTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A duty period target as the duty period team keeps it. */
data class DutyPeriodTarget(
    var code: String,
    var label: String,
    var shiftCode: String,
    var score: Double,
    var graceDays: Int,
    var status: DutyPeriodTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodTargetStatus): Boolean {
        when (status) {
            DutyPeriodTargetStatus.PLANNED -> return next == DutyPeriodTargetStatus.CANCELLED
            DutyPeriodTargetStatus.SCHEDULED -> {
                when (next) {
                    DutyPeriodTargetStatus.COMPLETED -> return true
                }
                return false
            }
            DutyPeriodTargetStatus.IN_PROGRESS -> {
                when (next) {
                    DutyPeriodTargetStatus.COMPLETED, DutyPeriodTargetStatus.CANCELLED -> return true
                }
                return false
            }
            DutyPeriodTargetStatus.COMPLETED -> return next == DutyPeriodTargetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DutyPeriodTargetStatus) {
        if (!canMoveTo(next)) {
            error("duty period target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedVolume(): Double = round((graceDays.toDouble() + score) * 100) / 100

    fun snapshot(): DutyPeriodTarget {
        return DutyPeriodTarget(
            code = code,
            label = label,
            shiftCode = shiftCode,
            score = score,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a duty period target in status planned. */
fun newDutyPeriodTarget(code: String, label: String): DutyPeriodTarget {
    return DutyPeriodTarget(
        code = code,
        label = label,
        shiftCode = "",
        score = 0.0,
        graceDays = 0,
        status = DutyPeriodTargetStatus.PLANNED,
    )
}
