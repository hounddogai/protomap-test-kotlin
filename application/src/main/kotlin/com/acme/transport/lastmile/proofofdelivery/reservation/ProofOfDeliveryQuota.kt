package com.acme.transport.lastmile.proofofdelivery.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery quota is in its life cycle. */
enum class ProofOfDeliveryQuotaStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a proof of delivery quota and its current status. */
data class ProofOfDeliveryQuota(
    var code: String,
    var name: String,
    var maximum: Double,
    var distanceKm: Double,
    var windowDays: Int,
    var status: ProofOfDeliveryQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryQuotaStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryQuotaStatus): Boolean {
        when (status) {
            ProofOfDeliveryQuotaStatus.REQUESTED -> return next == ProofOfDeliveryQuotaStatus.GRANTED || next == ProofOfDeliveryQuotaStatus.REVOKED
            ProofOfDeliveryQuotaStatus.GRANTED -> {
                when (next) {
                    ProofOfDeliveryQuotaStatus.REVOKED, ProofOfDeliveryQuotaStatus.EXPIRED -> return true
                }
                return false
            }
            ProofOfDeliveryQuotaStatus.REVOKED -> return next == ProofOfDeliveryQuotaStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryQuotaStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedLoad(): Double = round((windowDays.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): ProofOfDeliveryQuota {
        return ProofOfDeliveryQuota(
            code = code,
            name = name,
            maximum = maximum,
            distanceKm = distanceKm,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a proof of delivery quota in status requested. */
fun newProofOfDeliveryQuota(code: String, name: String): ProofOfDeliveryQuota {
    return ProofOfDeliveryQuota(
        code = code,
        name = name,
        maximum = 0.0,
        distanceKm = 0.0,
        windowDays = 0,
        status = ProofOfDeliveryQuotaStatus.REQUESTED,
    )
}
