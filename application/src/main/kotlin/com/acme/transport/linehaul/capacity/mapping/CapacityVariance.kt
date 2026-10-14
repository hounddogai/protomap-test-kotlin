package com.acme.transport.linehaul.capacity.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity variance is in its life cycle. */
enum class CapacityVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a capacity variance and its current status. */
data class CapacityVariance(
    var code: String,
    var label: String,
    var cost: Double,
    var vehicleId: String,
    var quantity: Int,
    var durationMinutes: Int,
    var threshold: Double,
    var status: CapacityVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityVarianceStatus): Boolean {
        when (status) {
            CapacityVarianceStatus.PROPOSED -> return next == CapacityVarianceStatus.REJECTED || next == CapacityVarianceStatus.WITHDRAWN
            CapacityVarianceStatus.APPROVED -> {
                when (next) {
                    CapacityVarianceStatus.REJECTED, CapacityVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
            CapacityVarianceStatus.REJECTED -> return next == CapacityVarianceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CapacityVarianceStatus) {
        if (!canMoveTo(next)) {
            error("capacity variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"capacity variance"} $code"

    fun effectiveValue(): Double = round((threshold + quantity.toDouble()) * 100) / 100

    fun snapshot(): CapacityVariance {
        return CapacityVariance(
            code = code,
            label = label,
            cost = cost,
            vehicleId = vehicleId,
            quantity = quantity,
            durationMinutes = durationMinutes,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capacity variance in status proposed. */
fun newCapacityVariance(code: String, label: String): CapacityVariance {
    return CapacityVariance(
        code = code,
        label = label,
        cost = 0.0,
        vehicleId = "",
        quantity = 0,
        durationMinutes = 0,
        threshold = 0.0,
        status = CapacityVarianceStatus.PROPOSED,
    )
}
