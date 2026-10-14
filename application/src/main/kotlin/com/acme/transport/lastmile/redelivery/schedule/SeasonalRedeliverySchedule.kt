package com.acme.transport.lastmile.redelivery.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal redelivery schedule records of lastmile's redelivery work, and the rules, stores, and
// calculations around them.

/** Where a seasonal redelivery schedule is in its life cycle. */
enum class SeasonalRedeliveryScheduleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a seasonal redelivery schedule and its current status. */
data class SeasonalRedeliverySchedule(
    var code: String,
    var name: String,
    var ratio: Double,
    var createdAt: Int,
    var approved: Boolean,
    var status: SeasonalRedeliveryScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRedeliveryScheduleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRedeliveryScheduleStatus): Boolean {
        when (status) {
            SeasonalRedeliveryScheduleStatus.PLANNED -> {
                when (next) {
                    SeasonalRedeliveryScheduleStatus.IN_PROGRESS, SeasonalRedeliveryScheduleStatus.COMPLETED -> return true
                }
                return false
            }
            SeasonalRedeliveryScheduleStatus.SCHEDULED -> return next == SeasonalRedeliveryScheduleStatus.CANCELLED
            SeasonalRedeliveryScheduleStatus.IN_PROGRESS -> {
                when (next) {
                    SeasonalRedeliveryScheduleStatus.COMPLETED, SeasonalRedeliveryScheduleStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalRedeliveryScheduleStatus.COMPLETED -> {
                when (next) {
                    SeasonalRedeliveryScheduleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalRedeliveryScheduleStatus) {
        if (!canMoveTo(next)) {
            error("seasonal redelivery schedule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"seasonal redelivery schedule"} $code"

    fun effectiveExposure(): Double = round(ratio * createdAt.toDouble() * 100) / 100

    fun snapshot(): SeasonalRedeliverySchedule {
        return SeasonalRedeliverySchedule(
            code = code,
            name = name,
            ratio = ratio,
            createdAt = createdAt,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal redelivery schedule in status planned. */
fun newSeasonalRedeliverySchedule(code: String, name: String): SeasonalRedeliverySchedule {
    return SeasonalRedeliverySchedule(
        code = code,
        name = name,
        ratio = 0.0,
        createdAt = 0,
        approved = false,
        status = SeasonalRedeliveryScheduleStatus.PLANNED,
    )
}
