package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal broker plan is in its life cycle. */
enum class SeasonalBrokerPlanStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One seasonal broker plan, with what the planners need to act on it. */
data class SeasonalBrokerPlan(
    var code: String,
    var name: String,
    var cost: Double,
    var archived: Boolean,
    var etaMinutes: Int,
    var status: SeasonalBrokerPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalBrokerPlanStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalBrokerPlanStatus): Boolean {
        when (status) {
            SeasonalBrokerPlanStatus.PLANNED -> {
                when (next) {
                    SeasonalBrokerPlanStatus.SCHEDULED -> return true
                }
                return false
            }
            SeasonalBrokerPlanStatus.SCHEDULED -> {
                when (next) {
                    SeasonalBrokerPlanStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalBrokerPlanStatus.IN_PROGRESS -> {
                when (next) {
                    SeasonalBrokerPlanStatus.COMPLETED, SeasonalBrokerPlanStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalBrokerPlanStatus.COMPLETED -> return next == SeasonalBrokerPlanStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SeasonalBrokerPlanStatus) {
        if (!canMoveTo(next)) {
            error("seasonal broker plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveValue(): Double = round(etaMinutes.toDouble() * cost * 100) / 100

    fun snapshot(): SeasonalBrokerPlan {
        return SeasonalBrokerPlan(
            code = code,
            name = name,
            cost = cost,
            archived = archived,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal broker plan in status planned. */
fun newSeasonalBrokerPlan(code: String, name: String): SeasonalBrokerPlan {
    return SeasonalBrokerPlan(
        code = code,
        name = name,
        cost = 0.0,
        archived = false,
        etaMinutes = 0,
        status = SeasonalBrokerPlanStatus.PLANNED,
    )
}
