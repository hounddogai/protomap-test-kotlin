package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop schedule is in its life cycle. */
enum class DropScheduleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A drop schedule as the drop team keeps it. */
data class DropSchedule(
    var code: String,
    var name: String,
    var notes: String,
    var windowDays: Int,
    var channel: String,
    var retries: Int,
    var status: DropScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropScheduleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropScheduleStatus): Boolean {
        when (status) {
            DropScheduleStatus.OPEN -> {
                when (next) {
                    DropScheduleStatus.IN_REVIEW, DropScheduleStatus.REOPENED -> return true
                }
                return false
            }
            DropScheduleStatus.IN_REVIEW -> {
                when (next) {
                    DropScheduleStatus.REOPENED, DropScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
            DropScheduleStatus.RESOLVED -> {
                when (next) {
                    DropScheduleStatus.REOPENED, DropScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
            DropScheduleStatus.REOPENED -> return next == DropScheduleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DropScheduleStatus) {
        if (!canMoveTo(next)) {
            error("drop schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"drop schedule"} $code"

    fun projectedExposure(): Double = round((windowDays.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): DropSchedule {
        return DropSchedule(
            code = code,
            name = name,
            notes = notes,
            windowDays = windowDays,
            channel = channel,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a drop schedule in status open. */
fun newDropSchedule(code: String, name: String): DropSchedule {
    return DropSchedule(
        code = code,
        name = name,
        notes = "",
        windowDays = 0,
        channel = "",
        retries = 0,
        status = DropScheduleStatus.OPEN,
    )
}
