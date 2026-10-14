package com.acme.transport.emissions.emissionfactor.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The emission factor schedule records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a emission factor schedule is in its life cycle. */
enum class EmissionFactorScheduleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One emission factor schedule, with what the planners need to act on it. */
data class EmissionFactorSchedule(
    var code: String,
    var title: String,
    var budget: Double,
    var priority: Int,
    var currency: String,
    var endsAt: Int,
    var quantity: Int,
    var status: EmissionFactorScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorScheduleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorScheduleStatus): Boolean {
        when (status) {
            EmissionFactorScheduleStatus.OPEN -> {
                when (next) {
                    EmissionFactorScheduleStatus.IN_REVIEW -> return true
                }
                return false
            }
            EmissionFactorScheduleStatus.IN_REVIEW -> {
                when (next) {
                    EmissionFactorScheduleStatus.RESOLVED, EmissionFactorScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
            EmissionFactorScheduleStatus.RESOLVED -> {
                when (next) {
                    EmissionFactorScheduleStatus.REOPENED -> return true
                }
                return false
            }
            EmissionFactorScheduleStatus.REOPENED -> {
                when (next) {
                    EmissionFactorScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorScheduleStatus) {
        if (!canMoveTo(next)) {
            error("emission factor schedule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun weightedLoad(): Double = round((endsAt.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): EmissionFactorSchedule {
        return EmissionFactorSchedule(
            code = code,
            title = title,
            budget = budget,
            priority = priority,
            currency = currency,
            endsAt = endsAt,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a emission factor schedule in status open. */
fun newEmissionFactorSchedule(code: String, title: String): EmissionFactorSchedule {
    return EmissionFactorSchedule(
        code = code,
        title = title,
        budget = 0.0,
        priority = 0,
        currency = "",
        endsAt = 0,
        quantity = 0,
        status = EmissionFactorScheduleStatus.OPEN,
    )
}
