package com.acme.transport.lastmile.drop.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal drop schedule is in its life cycle. */
enum class SeasonalDropScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a seasonal drop schedule and its current status. */
data class SeasonalDropSchedule(
    var code: String,
    var label: String,
    var amount: Double,
    var description: String,
    var durationMinutes: Int,
    var contactEmail: String,
    var approved: Boolean,
    var cost: Double,
    var status: SeasonalDropScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDropScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDropScheduleStatus): Boolean {
        when (status) {
            SeasonalDropScheduleStatus.DRAFT -> return next == SeasonalDropScheduleStatus.CLOSED
            SeasonalDropScheduleStatus.PENDING -> return next == SeasonalDropScheduleStatus.SUSPENDED || next == SeasonalDropScheduleStatus.CLOSED
            SeasonalDropScheduleStatus.ACTIVE -> {
                when (next) {
                    SeasonalDropScheduleStatus.SUSPENDED -> return true
                }
                return false
            }
            SeasonalDropScheduleStatus.SUSPENDED -> return next == SeasonalDropScheduleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: SeasonalDropScheduleStatus) {
        if (!canMoveTo(next)) {
            error("seasonal drop schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal drop schedule"} $code"

    fun effectiveLoad(): Double = round((cost - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): SeasonalDropSchedule {
        return SeasonalDropSchedule(
            code = code,
            label = label,
            amount = amount,
            description = description,
            durationMinutes = durationMinutes,
            contactEmail = contactEmail,
            approved = approved,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal drop schedule in status draft. */
fun newSeasonalDropSchedule(code: String, label: String): SeasonalDropSchedule {
    return SeasonalDropSchedule(
        code = code,
        label = label,
        amount = 0.0,
        description = "",
        durationMinutes = 0,
        contactEmail = "",
        approved = false,
        cost = 0.0,
        status = SeasonalDropScheduleStatus.DRAFT,
    )
}
