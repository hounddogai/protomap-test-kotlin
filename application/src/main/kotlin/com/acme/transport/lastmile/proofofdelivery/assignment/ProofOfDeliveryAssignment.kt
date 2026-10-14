package com.acme.transport.lastmile.proofofdelivery.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The proof of delivery assignment records of lastmile's proof of delivery work, and the rules, stores, and
// calculations around them.

/** Where a proof of delivery assignment is in its life cycle. */
enum class ProofOfDeliveryAssignmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a proof of delivery assignment and its current status. */
data class ProofOfDeliveryAssignment(
    var code: String,
    var label: String,
    var name: String,
    var reviewer: String,
    var carrier: String,
    var trailerId: String,
    var threshold: Double,
    var priority: Int,
    var revision: Int,
    var status: ProofOfDeliveryAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryAssignmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryAssignmentStatus): Boolean {
        when (status) {
            ProofOfDeliveryAssignmentStatus.PLANNED -> {
                when (next) {
                    ProofOfDeliveryAssignmentStatus.SCHEDULED, ProofOfDeliveryAssignmentStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ProofOfDeliveryAssignmentStatus.SCHEDULED -> {
                when (next) {
                    ProofOfDeliveryAssignmentStatus.IN_PROGRESS, ProofOfDeliveryAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
            ProofOfDeliveryAssignmentStatus.IN_PROGRESS -> return next == ProofOfDeliveryAssignmentStatus.COMPLETED || next == ProofOfDeliveryAssignmentStatus.CANCELLED
            ProofOfDeliveryAssignmentStatus.COMPLETED -> {
                when (next) {
                    ProofOfDeliveryAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery assignment $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedLoad(): Double = round(priority.toDouble() * threshold * 100) / 100

    fun snapshot(): ProofOfDeliveryAssignment {
        return ProofOfDeliveryAssignment(
            code = code,
            label = label,
            name = name,
            reviewer = reviewer,
            carrier = carrier,
            trailerId = trailerId,
            threshold = threshold,
            priority = priority,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a proof of delivery assignment in status planned. */
fun newProofOfDeliveryAssignment(code: String, label: String): ProofOfDeliveryAssignment {
    return ProofOfDeliveryAssignment(
        code = code,
        label = label,
        name = "",
        reviewer = "",
        carrier = "",
        trailerId = "",
        threshold = 0.0,
        priority = 0,
        revision = 0,
        status = ProofOfDeliveryAssignmentStatus.PLANNED,
    )
}
