package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dispute variance records of freightpay's dispute work, and the rules, stores, and calculations around
// them.

/** Where a dispute variance is in its life cycle. */
enum class DisputeVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A dispute variance as the dispute team keeps it. */
data class DisputeVariance(
    var code: String,
    var name: String,
    var odometer: Int,
    var attempts: Int,
    var attributes: MutableMap<String, String>,
    var carrier: String,
    var amount: Double,
    var weight: Double,
    var maximum: Double,
    var vehicleId: String,
    var status: DisputeVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeVarianceStatus): Boolean {
        when (status) {
            DisputeVarianceStatus.NEW -> {
                when (next) {
                    DisputeVarianceStatus.RUNNING, DisputeVarianceStatus.DONE -> return true
                }
                return false
            }
            DisputeVarianceStatus.QUEUED -> return next == DisputeVarianceStatus.RUNNING
            DisputeVarianceStatus.RUNNING -> {
                when (next) {
                    DisputeVarianceStatus.DONE, DisputeVarianceStatus.FAILED -> return true
                }
                return false
            }
            DisputeVarianceStatus.DONE -> {
                when (next) {
                    DisputeVarianceStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeVarianceStatus) {
        if (!canMoveTo(next)) {
            error("dispute variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"dispute variance"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedValue(): Double = round(weight * maximum * 100) / 100

    fun snapshot(): DisputeVariance {
        return DisputeVariance(
            code = code,
            name = name,
            odometer = odometer,
            attempts = attempts,
            attributes = attributes,
            carrier = carrier,
            amount = amount,
            weight = weight,
            maximum = maximum,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dispute variance in status new. */
fun newDisputeVariance(code: String, name: String): DisputeVariance {
    return DisputeVariance(
        code = code,
        name = name,
        odometer = 0,
        attempts = 0,
        attributes = mutableMapOf<String, String>(),
        carrier = "",
        amount = 0.0,
        weight = 0.0,
        maximum = 0.0,
        vehicleId = "",
        status = DisputeVarianceStatus.NEW,
    )
}
