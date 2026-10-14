package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending proof of delivery baseline is in its life cycle. */
enum class PendingProofOfDeliveryBaselineStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a pending proof of delivery baseline and its current status. */
data class PendingProofOfDeliveryBaseline(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var description: String,
    var updatedAt: Int,
    var budget: Double,
    var status: PendingProofOfDeliveryBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingProofOfDeliveryBaselineStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingProofOfDeliveryBaselineStatus): Boolean {
        when (status) {
            PendingProofOfDeliveryBaselineStatus.IDLE -> {
                when (next) {
                    PendingProofOfDeliveryBaselineStatus.READY, PendingProofOfDeliveryBaselineStatus.STOPPED -> return true
                }
                return false
            }
            PendingProofOfDeliveryBaselineStatus.WARMING -> return next == PendingProofOfDeliveryBaselineStatus.READY || next == PendingProofOfDeliveryBaselineStatus.STOPPED
            PendingProofOfDeliveryBaselineStatus.READY -> {
                when (next) {
                    PendingProofOfDeliveryBaselineStatus.DRAINING, PendingProofOfDeliveryBaselineStatus.STOPPED -> return true
                }
                return false
            }
            PendingProofOfDeliveryBaselineStatus.DRAINING -> return next == PendingProofOfDeliveryBaselineStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PendingProofOfDeliveryBaselineStatus) {
        if (!canMoveTo(next)) {
            error("pending proof of delivery baseline $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun weightedValue(): Double = round((updatedAt.toDouble() + budget) * 100) / 100

    fun snapshot(): PendingProofOfDeliveryBaseline {
        return PendingProofOfDeliveryBaseline(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            description = description,
            updatedAt = updatedAt,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending proof of delivery baseline in status idle. */
fun newPendingProofOfDeliveryBaseline(code: String, title: String): PendingProofOfDeliveryBaseline {
    return PendingProofOfDeliveryBaseline(
        code = code,
        title = title,
        fuelLiters = 0.0,
        description = "",
        updatedAt = 0,
        budget = 0.0,
        status = PendingProofOfDeliveryBaselineStatus.IDLE,
    )
}
