package com.acme.transport.network.hub.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted hub plan is in its life cycle. */
enum class WeightedHubPlanStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted hub plan. Its code is unique within a tenant. */
data class WeightedHubPlan(
    var code: String,
    var label: String,
    var ratio: Double,
    var amount: Double,
    var weight: Double,
    var description: String,
    var margin: Double,
    var status: WeightedHubPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedHubPlanStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedHubPlanStatus): Boolean {
        when (status) {
            WeightedHubPlanStatus.PLANNED -> {
                when (next) {
                    WeightedHubPlanStatus.SCHEDULED -> return true
                }
                return false
            }
            WeightedHubPlanStatus.SCHEDULED -> return next == WeightedHubPlanStatus.IN_PROGRESS || next == WeightedHubPlanStatus.CANCELLED
            WeightedHubPlanStatus.IN_PROGRESS -> {
                when (next) {
                    WeightedHubPlanStatus.COMPLETED -> return true
                }
                return false
            }
            WeightedHubPlanStatus.COMPLETED -> return next == WeightedHubPlanStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: WeightedHubPlanStatus) {
        if (!canMoveTo(next)) {
            error("weighted hub plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"weighted hub plan"} $code"

    fun adjustedLoad(): Double = round(margin * weight * 100) / 100

    fun snapshot(): WeightedHubPlan {
        return WeightedHubPlan(
            code = code,
            label = label,
            ratio = ratio,
            amount = amount,
            weight = weight,
            description = description,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted hub plan in status planned. */
fun newWeightedHubPlan(code: String, label: String): WeightedHubPlan {
    return WeightedHubPlan(
        code = code,
        label = label,
        ratio = 0.0,
        amount = 0.0,
        weight = 0.0,
        description = "",
        margin = 0.0,
        status = WeightedHubPlanStatus.PLANNED,
    )
}
