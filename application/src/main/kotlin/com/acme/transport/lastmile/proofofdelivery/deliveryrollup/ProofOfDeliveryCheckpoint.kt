package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery checkpoint is in its life cycle. */
enum class ProofOfDeliveryCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a proof of delivery checkpoint and its current status. */
data class ProofOfDeliveryCheckpoint(
    var code: String,
    var title: String,
    var priority: Int,
    var reviewer: String,
    var expiresAt: Int,
    var routeCode: String,
    var distanceKm: Double,
    var status: ProofOfDeliveryCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryCheckpointStatus): Boolean {
        when (status) {
            ProofOfDeliveryCheckpointStatus.PROPOSED -> {
                when (next) {
                    ProofOfDeliveryCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
            ProofOfDeliveryCheckpointStatus.APPROVED -> {
                when (next) {
                    ProofOfDeliveryCheckpointStatus.REJECTED, ProofOfDeliveryCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
            ProofOfDeliveryCheckpointStatus.REJECTED -> return next == ProofOfDeliveryCheckpointStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery checkpoint $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"proof of delivery checkpoint"} $code"

    fun adjustedExposure(): Double = round(priority.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): ProofOfDeliveryCheckpoint {
        return ProofOfDeliveryCheckpoint(
            code = code,
            title = title,
            priority = priority,
            reviewer = reviewer,
            expiresAt = expiresAt,
            routeCode = routeCode,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a proof of delivery checkpoint in status proposed. */
fun newProofOfDeliveryCheckpoint(code: String, title: String): ProofOfDeliveryCheckpoint {
    return ProofOfDeliveryCheckpoint(
        code = code,
        title = title,
        priority = 0,
        reviewer = "",
        expiresAt = 0,
        routeCode = "",
        distanceKm = 0.0,
        status = ProofOfDeliveryCheckpointStatus.PROPOSED,
    )
}
