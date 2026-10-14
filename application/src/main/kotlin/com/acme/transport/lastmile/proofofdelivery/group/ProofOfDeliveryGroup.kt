package com.acme.transport.lastmile.proofofdelivery.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The proof of delivery group records of lastmile's proof of delivery work, and the rules, stores, and
// calculations around them.

/** Where a proof of delivery group is in its life cycle. */
enum class ProofOfDeliveryGroupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a proof of delivery group and its current status. */
data class ProofOfDeliveryGroup(
    var code: String,
    var name: String,
    var score: Double,
    var description: String,
    var routeCode: String,
    var windowDays: Int,
    var lane: String,
    var batchSize: Int,
    var active: Boolean,
    var quantity: Int,
    var revision: Int,
    var status: ProofOfDeliveryGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryGroupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryGroupStatus): Boolean {
        when (status) {
            ProofOfDeliveryGroupStatus.IDLE -> {
                when (next) {
                    ProofOfDeliveryGroupStatus.STOPPED -> return true
                }
                return false
            }
            ProofOfDeliveryGroupStatus.WARMING -> return next == ProofOfDeliveryGroupStatus.READY || next == ProofOfDeliveryGroupStatus.STOPPED
            ProofOfDeliveryGroupStatus.READY -> return next == ProofOfDeliveryGroupStatus.DRAINING || next == ProofOfDeliveryGroupStatus.STOPPED
            ProofOfDeliveryGroupStatus.DRAINING -> return next == ProofOfDeliveryGroupStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryGroupStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery group $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"proof of delivery group"} $code"

    fun adjustedTotal(): Double = round(revision.toDouble() * score * 100) / 100

    fun snapshot(): ProofOfDeliveryGroup {
        return ProofOfDeliveryGroup(
            code = code,
            name = name,
            score = score,
            description = description,
            routeCode = routeCode,
            windowDays = windowDays,
            lane = lane,
            batchSize = batchSize,
            active = active,
            quantity = quantity,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a proof of delivery group in status idle. */
fun newProofOfDeliveryGroup(code: String, name: String): ProofOfDeliveryGroup {
    return ProofOfDeliveryGroup(
        code = code,
        name = name,
        score = 0.0,
        description = "",
        routeCode = "",
        windowDays = 0,
        lane = "",
        batchSize = 0,
        active = false,
        quantity = 0,
        revision = 0,
        status = ProofOfDeliveryGroupStatus.IDLE,
    )
}
