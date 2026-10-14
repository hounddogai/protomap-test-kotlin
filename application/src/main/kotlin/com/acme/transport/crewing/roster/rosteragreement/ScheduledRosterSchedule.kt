package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled roster schedule is in its life cycle. */
enum class ScheduledRosterScheduleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One scheduled roster schedule, with what the planners need to act on it. */
data class ScheduledRosterSchedule(
    var code: String,
    var label: String,
    var weight: Double,
    var stopCount: Int,
    var minimum: Double,
    var unit: String,
    var batchSize: Int,
    var expiresAt: Int,
    var shiftCode: String,
    var discount: Double,
    var status: ScheduledRosterScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRosterScheduleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRosterScheduleStatus): Boolean {
        when (status) {
            ScheduledRosterScheduleStatus.PLANNED -> {
                when (next) {
                    ScheduledRosterScheduleStatus.IN_PROGRESS, ScheduledRosterScheduleStatus.COMPLETED -> return true
                }
                return false
            }
            ScheduledRosterScheduleStatus.SCHEDULED -> return next == ScheduledRosterScheduleStatus.COMPLETED
            ScheduledRosterScheduleStatus.IN_PROGRESS -> return next == ScheduledRosterScheduleStatus.COMPLETED || next == ScheduledRosterScheduleStatus.CANCELLED
            ScheduledRosterScheduleStatus.COMPLETED -> {
                when (next) {
                    ScheduledRosterScheduleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledRosterScheduleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled roster schedule $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"scheduled roster schedule"} $code"

    fun projectedTotal(): Double = round((weight - discount) * 100) / 100

    fun snapshot(): ScheduledRosterSchedule {
        return ScheduledRosterSchedule(
            code = code,
            label = label,
            weight = weight,
            stopCount = stopCount,
            minimum = minimum,
            unit = unit,
            batchSize = batchSize,
            expiresAt = expiresAt,
            shiftCode = shiftCode,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scheduled roster schedule in status planned. */
fun newScheduledRosterSchedule(code: String, label: String): ScheduledRosterSchedule {
    return ScheduledRosterSchedule(
        code = code,
        label = label,
        weight = 0.0,
        stopCount = 0,
        minimum = 0.0,
        unit = "",
        batchSize = 0,
        expiresAt = 0,
        shiftCode = "",
        discount = 0.0,
        status = ScheduledRosterScheduleStatus.PLANNED,
    )
}
