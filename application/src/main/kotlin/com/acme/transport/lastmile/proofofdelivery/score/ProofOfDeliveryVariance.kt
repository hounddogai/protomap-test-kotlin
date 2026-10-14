package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery variance is in its life cycle. */
enum class ProofOfDeliveryVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a proof of delivery variance and its current status. */
data class ProofOfDeliveryVariance(
    var code: String,
    var label: String,
    var quantity: Int,
    var name: String,
    var lane: String,
    var batchSize: Int,
    var trailerId: String,
    var amount: Double,
    var status: ProofOfDeliveryVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryVarianceStatus): Boolean {
        when (status) {
            ProofOfDeliveryVarianceStatus.IDLE -> return next == ProofOfDeliveryVarianceStatus.READY || next == ProofOfDeliveryVarianceStatus.STOPPED
            ProofOfDeliveryVarianceStatus.WARMING -> {
                when (next) {
                    ProofOfDeliveryVarianceStatus.READY, ProofOfDeliveryVarianceStatus.STOPPED -> return true
                }
                return false
            }
            ProofOfDeliveryVarianceStatus.READY -> {
                when (next) {
                    ProofOfDeliveryVarianceStatus.STOPPED -> return true
                }
                return false
            }
            ProofOfDeliveryVarianceStatus.DRAINING -> {
                when (next) {
                    ProofOfDeliveryVarianceStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryVarianceStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netTotal(): Double = round(batchSize.toDouble() * amount * 100) / 100

    fun snapshot(): ProofOfDeliveryVariance {
        return ProofOfDeliveryVariance(
            code = code,
            label = label,
            quantity = quantity,
            name = name,
            lane = lane,
            batchSize = batchSize,
            trailerId = trailerId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a proof of delivery variance in status idle. */
fun newProofOfDeliveryVariance(code: String, label: String): ProofOfDeliveryVariance {
    return ProofOfDeliveryVariance(
        code = code,
        label = label,
        quantity = 0,
        name = "",
        lane = "",
        batchSize = 0,
        trailerId = "",
        amount = 0.0,
        status = ProofOfDeliveryVarianceStatus.IDLE,
    )
}
