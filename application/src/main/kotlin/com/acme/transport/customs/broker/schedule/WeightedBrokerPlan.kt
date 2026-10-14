package com.acme.transport.customs.broker.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted broker plan is in its life cycle. */
enum class WeightedBrokerPlanStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A weighted broker plan as the broker team keeps it. */
data class WeightedBrokerPlan(
    var code: String,
    var name: String,
    var active: Boolean,
    var discount: Double,
    var quantity: Int,
    var budget: Double,
    var checksum: String,
    var stopCount: Int,
    var status: WeightedBrokerPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedBrokerPlanStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedBrokerPlanStatus): Boolean {
        when (status) {
            WeightedBrokerPlanStatus.DRAFT -> return next == WeightedBrokerPlanStatus.ACTIVE
            WeightedBrokerPlanStatus.PENDING -> {
                when (next) {
                    WeightedBrokerPlanStatus.ACTIVE -> return true
                }
                return false
            }
            WeightedBrokerPlanStatus.ACTIVE -> {
                when (next) {
                    WeightedBrokerPlanStatus.CLOSED -> return true
                }
                return false
            }
            WeightedBrokerPlanStatus.SUSPENDED -> {
                when (next) {
                    WeightedBrokerPlanStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedBrokerPlanStatus) {
        if (!canMoveTo(next)) {
            error("weighted broker plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted broker plan"} $code"

    fun effectiveExposure(): Double = round((quantity.toDouble() + budget) * 100) / 100

    fun snapshot(): WeightedBrokerPlan {
        return WeightedBrokerPlan(
            code = code,
            name = name,
            active = active,
            discount = discount,
            quantity = quantity,
            budget = budget,
            checksum = checksum,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted broker plan in status draft. */
fun newWeightedBrokerPlan(code: String, name: String): WeightedBrokerPlan {
    return WeightedBrokerPlan(
        code = code,
        name = name,
        active = false,
        discount = 0.0,
        quantity = 0,
        budget = 0.0,
        checksum = "",
        stopCount = 0,
        status = WeightedBrokerPlanStatus.DRAFT,
    )
}
