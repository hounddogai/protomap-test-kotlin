package com.acme.transport.crewing.dutyperiod.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period rollup is in its life cycle. */
enum class DutyPeriodRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A duty period rollup as the duty period team keeps it. */
data class DutyPeriodRollup(
    var code: String,
    var label: String,
    var minimum: Double,
    var depot: String,
    var lane: String,
    var stopCount: Int,
    var windowDays: Int,
    var updatedAt: Int,
    var pinned: Boolean,
    var status: DutyPeriodRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodRollupStatus): Boolean {
        when (status) {
            DutyPeriodRollupStatus.PLANNED -> {
                when (next) {
                    DutyPeriodRollupStatus.SCHEDULED -> return true
                }
                return false
            }
            DutyPeriodRollupStatus.SCHEDULED -> {
                when (next) {
                    DutyPeriodRollupStatus.IN_PROGRESS -> return true
                }
                return false
            }
            DutyPeriodRollupStatus.IN_PROGRESS -> {
                when (next) {
                    DutyPeriodRollupStatus.COMPLETED, DutyPeriodRollupStatus.CANCELLED -> return true
                }
                return false
            }
            DutyPeriodRollupStatus.COMPLETED -> {
                when (next) {
                    DutyPeriodRollupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodRollupStatus) {
        if (!canMoveTo(next)) {
            error("duty period rollup $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"duty period rollup"} $code"

    fun weightedVolume(): Double = round((updatedAt.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): DutyPeriodRollup {
        return DutyPeriodRollup(
            code = code,
            label = label,
            minimum = minimum,
            depot = depot,
            lane = lane,
            stopCount = stopCount,
            windowDays = windowDays,
            updatedAt = updatedAt,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a duty period rollup in status planned. */
fun newDutyPeriodRollup(code: String, label: String): DutyPeriodRollup {
    return DutyPeriodRollup(
        code = code,
        label = label,
        minimum = 0.0,
        depot = "",
        lane = "",
        stopCount = 0,
        windowDays = 0,
        updatedAt = 0,
        pinned = false,
        status = DutyPeriodRollupStatus.PLANNED,
    )
}
