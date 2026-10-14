package com.acme.transport.lastmile.proofofdelivery.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The proof of delivery audit records of lastmile's proof of delivery work, and the rules, stores, and
// calculations around them.

/** Where a proof of delivery audit is in its life cycle. */
enum class ProofOfDeliveryAuditStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One proof of delivery audit, with what the planners need to act on it. */
data class ProofOfDeliveryAudit(
    var code: String,
    var label: String,
    var priority: Int,
    var approved: Boolean,
    var batchSize: Int,
    var margin: Double,
    var endsAt: Int,
    var distanceKm: Double,
    var score: Double,
    var status: ProofOfDeliveryAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryAuditStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryAuditStatus): Boolean {
        when (status) {
            ProofOfDeliveryAuditStatus.REQUESTED -> return next == ProofOfDeliveryAuditStatus.REVOKED
            ProofOfDeliveryAuditStatus.GRANTED -> {
                when (next) {
                    ProofOfDeliveryAuditStatus.EXPIRED -> return true
                }
                return false
            }
            ProofOfDeliveryAuditStatus.REVOKED -> return next == ProofOfDeliveryAuditStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryAuditStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery audit $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveValue(): Double = round(priority.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): ProofOfDeliveryAudit {
        return ProofOfDeliveryAudit(
            code = code,
            label = label,
            priority = priority,
            approved = approved,
            batchSize = batchSize,
            margin = margin,
            endsAt = endsAt,
            distanceKm = distanceKm,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a proof of delivery audit in status requested. */
fun newProofOfDeliveryAudit(code: String, label: String): ProofOfDeliveryAudit {
    return ProofOfDeliveryAudit(
        code = code,
        label = label,
        priority = 0,
        approved = false,
        batchSize = 0,
        margin = 0.0,
        endsAt = 0,
        distanceKm = 0.0,
        score = 0.0,
        status = ProofOfDeliveryAuditStatus.REQUESTED,
    )
}
