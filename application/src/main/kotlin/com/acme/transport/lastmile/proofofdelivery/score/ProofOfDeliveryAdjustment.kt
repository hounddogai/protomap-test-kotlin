package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery adjustment is in its life cycle. */
enum class ProofOfDeliveryAdjustmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a proof of delivery adjustment and its current status. */
data class ProofOfDeliveryAdjustment(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var priority: Int,
    var attempts: Int,
    var windowDays: Int,
    var description: String,
    var status: ProofOfDeliveryAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryAdjustmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryAdjustmentStatus): Boolean {
        when (status) {
            ProofOfDeliveryAdjustmentStatus.REQUESTED -> {
                when (next) {
                    ProofOfDeliveryAdjustmentStatus.REVOKED, ProofOfDeliveryAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
            ProofOfDeliveryAdjustmentStatus.GRANTED -> {
                when (next) {
                    ProofOfDeliveryAdjustmentStatus.REVOKED, ProofOfDeliveryAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
            ProofOfDeliveryAdjustmentStatus.REVOKED -> {
                when (next) {
                    ProofOfDeliveryAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"proof of delivery adjustment"} $code"

    fun projectedTotal(): Double = round((windowDays.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): ProofOfDeliveryAdjustment {
        return ProofOfDeliveryAdjustment(
            code = code,
            title = title,
            pinned = pinned,
            priority = priority,
            attempts = attempts,
            windowDays = windowDays,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a proof of delivery adjustment in status requested. */
fun newProofOfDeliveryAdjustment(code: String, title: String): ProofOfDeliveryAdjustment {
    return ProofOfDeliveryAdjustment(
        code = code,
        title = title,
        pinned = false,
        priority = 0,
        attempts = 0,
        windowDays = 0,
        description = "",
        status = ProofOfDeliveryAdjustmentStatus.REQUESTED,
    )
}
