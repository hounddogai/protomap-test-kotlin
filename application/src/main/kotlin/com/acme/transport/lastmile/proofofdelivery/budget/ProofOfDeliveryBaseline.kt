package com.acme.transport.lastmile.proofofdelivery.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery baseline is in its life cycle. */
enum class ProofOfDeliveryBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A proof of delivery baseline. Its code is unique within a tenant. */
data class ProofOfDeliveryBaseline(
    var code: String,
    var label: String,
    var weight: Double,
    var windowDays: Int,
    var carrier: String,
    var status: ProofOfDeliveryBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryBaselineStatus): Boolean {
        when (status) {
            ProofOfDeliveryBaselineStatus.DRAFT -> {
                when (next) {
                    ProofOfDeliveryBaselineStatus.CLOSED -> return true
                }
                return false
            }
            ProofOfDeliveryBaselineStatus.PENDING -> {
                when (next) {
                    ProofOfDeliveryBaselineStatus.CLOSED -> return true
                }
                return false
            }
            ProofOfDeliveryBaselineStatus.ACTIVE -> return next == ProofOfDeliveryBaselineStatus.SUSPENDED
            ProofOfDeliveryBaselineStatus.SUSPENDED -> return next == ProofOfDeliveryBaselineStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryBaselineStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedExposure(): Double = round(windowDays.toDouble() * weight * 100) / 100

    fun snapshot(): ProofOfDeliveryBaseline {
        return ProofOfDeliveryBaseline(
            code = code,
            label = label,
            weight = weight,
            windowDays = windowDays,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a proof of delivery baseline in status draft. */
fun newProofOfDeliveryBaseline(code: String, label: String): ProofOfDeliveryBaseline {
    return ProofOfDeliveryBaseline(
        code = code,
        label = label,
        weight = 0.0,
        windowDays = 0,
        carrier = "",
        status = ProofOfDeliveryBaselineStatus.DRAFT,
    )
}
