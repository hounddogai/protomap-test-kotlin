package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted hold request is in its life cycle. */
enum class WeightedHoldRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A weighted hold request as the hold team keeps it. */
data class WeightedHoldRequest(
    var code: String,
    var name: String,
    var summary: String,
    var currency: String,
    var budget: Double,
    var capacity: Int,
    var vehicleId: String,
    var startsAt: Int,
    var status: WeightedHoldRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedHoldRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedHoldRequestStatus): Boolean {
        when (status) {
            WeightedHoldRequestStatus.PROPOSED -> {
                when (next) {
                    WeightedHoldRequestStatus.REJECTED -> return true
                }
                return false
            }
            WeightedHoldRequestStatus.APPROVED -> return next == WeightedHoldRequestStatus.REJECTED || next == WeightedHoldRequestStatus.WITHDRAWN
            WeightedHoldRequestStatus.REJECTED -> return next == WeightedHoldRequestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: WeightedHoldRequestStatus) {
        if (!canMoveTo(next)) {
            error("weighted hold request $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveValue(): Double = round((startsAt.toDouble() + budget) * 100) / 100

    fun snapshot(): WeightedHoldRequest {
        return WeightedHoldRequest(
            code = code,
            name = name,
            summary = summary,
            currency = currency,
            budget = budget,
            capacity = capacity,
            vehicleId = vehicleId,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted hold request in status proposed. */
fun newWeightedHoldRequest(code: String, name: String): WeightedHoldRequest {
    return WeightedHoldRequest(
        code = code,
        name = name,
        summary = "",
        currency = "",
        budget = 0.0,
        capacity = 0,
        vehicleId = "",
        startsAt = 0,
        status = WeightedHoldRequestStatus.PROPOSED,
    )
}
