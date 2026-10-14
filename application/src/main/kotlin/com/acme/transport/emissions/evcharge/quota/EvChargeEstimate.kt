package com.acme.transport.emissions.evcharge.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge estimate is in its life cycle. */
enum class EvChargeEstimateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One ev charge estimate, with what the planners need to act on it. */
data class EvChargeEstimate(
    var code: String,
    var name: String,
    var active: Boolean,
    var quantity: Int,
    var category: String,
    var trailerId: String,
    var ratio: Double,
    var shiftCode: String,
    var status: EvChargeEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeEstimateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeEstimateStatus): Boolean {
        when (status) {
            EvChargeEstimateStatus.PROPOSED -> return next == EvChargeEstimateStatus.WITHDRAWN
            EvChargeEstimateStatus.APPROVED -> {
                when (next) {
                    EvChargeEstimateStatus.REJECTED, EvChargeEstimateStatus.WITHDRAWN -> return true
                }
                return false
            }
            EvChargeEstimateStatus.REJECTED -> {
                when (next) {
                    EvChargeEstimateStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeEstimateStatus) {
        if (!canMoveTo(next)) {
            error("ev charge estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"ev charge estimate"} $code"

    fun adjustedTotal(): Double = round((quantity.toDouble() - ratio) * 100) / 100

    fun snapshot(): EvChargeEstimate {
        return EvChargeEstimate(
            code = code,
            name = name,
            active = active,
            quantity = quantity,
            category = category,
            trailerId = trailerId,
            ratio = ratio,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a ev charge estimate in status proposed. */
fun newEvChargeEstimate(code: String, name: String): EvChargeEstimate {
    return EvChargeEstimate(
        code = code,
        name = name,
        active = false,
        quantity = 0,
        category = "",
        trailerId = "",
        ratio = 0.0,
        shiftCode = "",
        status = EvChargeEstimateStatus.PROPOSED,
    )
}
