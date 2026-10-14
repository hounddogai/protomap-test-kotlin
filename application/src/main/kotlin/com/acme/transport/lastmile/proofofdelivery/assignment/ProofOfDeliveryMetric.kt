package com.acme.transport.lastmile.proofofdelivery.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery metric is in its life cycle. */
enum class ProofOfDeliveryMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a proof of delivery metric and its current status. */
data class ProofOfDeliveryMetric(
    var code: String,
    var name: String,
    var summary: String,
    var shiftCode: String,
    var maximum: Double,
    var active: Boolean,
    var graceDays: Int,
    var status: ProofOfDeliveryMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryMetricStatus): Boolean {
        when (status) {
            ProofOfDeliveryMetricStatus.REQUESTED -> return next == ProofOfDeliveryMetricStatus.GRANTED || next == ProofOfDeliveryMetricStatus.EXPIRED
            ProofOfDeliveryMetricStatus.GRANTED -> {
                when (next) {
                    ProofOfDeliveryMetricStatus.EXPIRED -> return true
                }
                return false
            }
            ProofOfDeliveryMetricStatus.REVOKED -> {
                when (next) {
                    ProofOfDeliveryMetricStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryMetricStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedExposure(): Double = round(graceDays.toDouble() * maximum * 100) / 100

    fun snapshot(): ProofOfDeliveryMetric {
        return ProofOfDeliveryMetric(
            code = code,
            name = name,
            summary = summary,
            shiftCode = shiftCode,
            maximum = maximum,
            active = active,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a proof of delivery metric in status requested. */
fun newProofOfDeliveryMetric(code: String, name: String): ProofOfDeliveryMetric {
    return ProofOfDeliveryMetric(
        code = code,
        name = name,
        summary = "",
        shiftCode = "",
        maximum = 0.0,
        active = false,
        graceDays = 0,
        status = ProofOfDeliveryMetricStatus.REQUESTED,
    )
}
