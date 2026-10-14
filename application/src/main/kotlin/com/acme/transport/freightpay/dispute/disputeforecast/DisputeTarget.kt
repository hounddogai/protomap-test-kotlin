package com.acme.transport.freightpay.dispute.disputeforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute target is in its life cycle. */
enum class DisputeTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a dispute target and its current status. */
data class DisputeTarget(
    var code: String,
    var title: String,
    var discount: Double,
    var currency: String,
    var reviewer: String,
    var sequence: Int,
    var endsAt: Int,
    var revision: Int,
    var pallets: Int,
    var distanceKm: Double,
    var status: DisputeTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeTargetStatus): Boolean {
        when (status) {
            DisputeTargetStatus.PLANNED -> return next == DisputeTargetStatus.IN_PROGRESS
            DisputeTargetStatus.SCHEDULED -> return next == DisputeTargetStatus.IN_PROGRESS
            DisputeTargetStatus.IN_PROGRESS -> return next == DisputeTargetStatus.COMPLETED
            DisputeTargetStatus.COMPLETED -> {
                when (next) {
                    DisputeTargetStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeTargetStatus) {
        if (!canMoveTo(next)) {
            error("dispute target $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun adjustedExposure(): Double = round((pallets.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): DisputeTarget {
        return DisputeTarget(
            code = code,
            title = title,
            discount = discount,
            currency = currency,
            reviewer = reviewer,
            sequence = sequence,
            endsAt = endsAt,
            revision = revision,
            pallets = pallets,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a dispute target in status planned. */
fun newDisputeTarget(code: String, title: String): DisputeTarget {
    return DisputeTarget(
        code = code,
        title = title,
        discount = 0.0,
        currency = "",
        reviewer = "",
        sequence = 0,
        endsAt = 0,
        revision = 0,
        pallets = 0,
        distanceKm = 0.0,
        status = DisputeTargetStatus.PLANNED,
    )
}
