package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery rollup is in its life cycle. */
enum class ProofOfDeliveryRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A proof of delivery rollup as the proof of delivery team keeps it. */
data class ProofOfDeliveryRollup(
    var code: String,
    var title: String,
    var batchSize: Int,
    var attempts: Int,
    var carrier: String,
    var minimum: Double,
    var region: String,
    var status: ProofOfDeliveryRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryRollupStatus): Boolean {
        when (status) {
            ProofOfDeliveryRollupStatus.OPEN -> {
                when (next) {
                    ProofOfDeliveryRollupStatus.RESOLVED, ProofOfDeliveryRollupStatus.ARCHIVED -> return true
                }
                return false
            }
            ProofOfDeliveryRollupStatus.IN_REVIEW -> {
                when (next) {
                    ProofOfDeliveryRollupStatus.RESOLVED, ProofOfDeliveryRollupStatus.ARCHIVED -> return true
                }
                return false
            }
            ProofOfDeliveryRollupStatus.RESOLVED -> return next == ProofOfDeliveryRollupStatus.REOPENED
            ProofOfDeliveryRollupStatus.REOPENED -> {
                when (next) {
                    ProofOfDeliveryRollupStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryRollupStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveValue(): Double = round((batchSize.toDouble() - minimum) * 100) / 100

    fun snapshot(): ProofOfDeliveryRollup {
        return ProofOfDeliveryRollup(
            code = code,
            title = title,
            batchSize = batchSize,
            attempts = attempts,
            carrier = carrier,
            minimum = minimum,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a proof of delivery rollup in status open. */
fun newProofOfDeliveryRollup(code: String, title: String): ProofOfDeliveryRollup {
    return ProofOfDeliveryRollup(
        code = code,
        title = title,
        batchSize = 0,
        attempts = 0,
        carrier = "",
        minimum = 0.0,
        region = "",
        status = ProofOfDeliveryRollupStatus.OPEN,
    )
}
