package com.acme.transport.lastmile.proofofdelivery.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery milestone is in its life cycle. */
enum class ProofOfDeliveryMilestoneStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A proof of delivery milestone as the proof of delivery team keeps it. */
data class ProofOfDeliveryMilestone(
    var code: String,
    var label: String,
    var cost: Double,
    var ownerId: String,
    var currency: String,
    var margin: Double,
    var ceiling: Double,
    var segment: String,
    var windowDays: Int,
    var endsAt: Int,
    var startsAt: Int,
    var status: ProofOfDeliveryMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryMilestoneStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryMilestoneStatus): Boolean {
        when (status) {
            ProofOfDeliveryMilestoneStatus.DRAFT -> return next == ProofOfDeliveryMilestoneStatus.SUSPENDED
            ProofOfDeliveryMilestoneStatus.PENDING -> {
                when (next) {
                    ProofOfDeliveryMilestoneStatus.CLOSED -> return true
                }
                return false
            }
            ProofOfDeliveryMilestoneStatus.ACTIVE -> {
                when (next) {
                    ProofOfDeliveryMilestoneStatus.SUSPENDED -> return true
                }
                return false
            }
            ProofOfDeliveryMilestoneStatus.SUSPENDED -> {
                when (next) {
                    ProofOfDeliveryMilestoneStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery milestone $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"proof of delivery milestone"} $code"

    fun weightedLoad(): Double = round((margin - startsAt.toDouble()) * 100) / 100

    fun snapshot(): ProofOfDeliveryMilestone {
        return ProofOfDeliveryMilestone(
            code = code,
            label = label,
            cost = cost,
            ownerId = ownerId,
            currency = currency,
            margin = margin,
            ceiling = ceiling,
            segment = segment,
            windowDays = windowDays,
            endsAt = endsAt,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a proof of delivery milestone in status draft. */
fun newProofOfDeliveryMilestone(code: String, label: String): ProofOfDeliveryMilestone {
    return ProofOfDeliveryMilestone(
        code = code,
        label = label,
        cost = 0.0,
        ownerId = "",
        currency = "",
        margin = 0.0,
        ceiling = 0.0,
        segment = "",
        windowDays = 0,
        endsAt = 0,
        startsAt = 0,
        status = ProofOfDeliveryMilestoneStatus.DRAFT,
    )
}
