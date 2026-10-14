package com.acme.transport.crewing.dutyperiod.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict duty period target records of crewing's duty period work, and the rules, stores, and
// calculations around them.

/** Where a strict duty period target is in its life cycle. */
enum class StrictDutyPeriodTargetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a strict duty period target and its current status. */
data class StrictDutyPeriodTarget(
    var code: String,
    var name: String,
    var retries: Int,
    var createdAt: Int,
    var driverId: String,
    var status: StrictDutyPeriodTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDutyPeriodTargetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDutyPeriodTargetStatus): Boolean {
        when (status) {
            StrictDutyPeriodTargetStatus.IDLE -> {
                when (next) {
                    StrictDutyPeriodTargetStatus.READY, StrictDutyPeriodTargetStatus.STOPPED -> return true
                }
                return false
            }
            StrictDutyPeriodTargetStatus.WARMING -> return next == StrictDutyPeriodTargetStatus.DRAINING
            StrictDutyPeriodTargetStatus.READY -> {
                when (next) {
                    StrictDutyPeriodTargetStatus.DRAINING, StrictDutyPeriodTargetStatus.STOPPED -> return true
                }
                return false
            }
            StrictDutyPeriodTargetStatus.DRAINING -> {
                when (next) {
                    StrictDutyPeriodTargetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictDutyPeriodTargetStatus) {
        if (!canMoveTo(next)) {
            error("strict duty period target $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"strict duty period target"} $code"

    fun adjustedVolume(): Double = round((createdAt.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): StrictDutyPeriodTarget {
        return StrictDutyPeriodTarget(
            code = code,
            name = name,
            retries = retries,
            createdAt = createdAt,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict duty period target in status idle. */
fun newStrictDutyPeriodTarget(code: String, name: String): StrictDutyPeriodTarget {
    return StrictDutyPeriodTarget(
        code = code,
        name = name,
        retries = 0,
        createdAt = 0,
        driverId = "",
        status = StrictDutyPeriodTargetStatus.IDLE,
    )
}
