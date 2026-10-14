package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted parcel policy is in its life cycle. */
enum class WeightedParcelPolicyStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a weighted parcel policy and its current status. */
data class WeightedParcelPolicy(
    var code: String,
    var label: String,
    var depot: String,
    var maximum: Double,
    var title: String,
    var windowDays: Int,
    var description: String,
    var routeCode: String,
    var status: WeightedParcelPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedParcelPolicyStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedParcelPolicyStatus): Boolean {
        when (status) {
            WeightedParcelPolicyStatus.PROPOSED -> return next == WeightedParcelPolicyStatus.REJECTED || next == WeightedParcelPolicyStatus.WITHDRAWN
            WeightedParcelPolicyStatus.APPROVED -> {
                when (next) {
                    WeightedParcelPolicyStatus.REJECTED -> return true
                }
                return false
            }
            WeightedParcelPolicyStatus.REJECTED -> return next == WeightedParcelPolicyStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: WeightedParcelPolicyStatus) {
        if (!canMoveTo(next)) {
            error("weighted parcel policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveVolume(): Double = round((windowDays.toDouble() + maximum) * 100) / 100

    fun snapshot(): WeightedParcelPolicy {
        return WeightedParcelPolicy(
            code = code,
            label = label,
            depot = depot,
            maximum = maximum,
            title = title,
            windowDays = windowDays,
            description = description,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a weighted parcel policy in status proposed. */
fun newWeightedParcelPolicy(code: String, label: String): WeightedParcelPolicy {
    return WeightedParcelPolicy(
        code = code,
        label = label,
        depot = "",
        maximum = 0.0,
        title = "",
        windowDays = 0,
        description = "",
        routeCode = "",
        status = WeightedParcelPolicyStatus.PROPOSED,
    )
}
