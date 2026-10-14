package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return score is in its life cycle. */
enum class LeaseReturnScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a lease return score and its current status. */
data class LeaseReturnScore(
    var code: String,
    var name: String,
    var checksum: String,
    var unit: String,
    var phone: String,
    var label: String,
    var sequence: Int,
    var distanceKm: Double,
    var segment: String,
    var status: LeaseReturnScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnScoreStatus): Boolean {
        when (status) {
            LeaseReturnScoreStatus.PLANNED -> return next == LeaseReturnScoreStatus.IN_PROGRESS || next == LeaseReturnScoreStatus.CANCELLED
            LeaseReturnScoreStatus.SCHEDULED -> return next == LeaseReturnScoreStatus.IN_PROGRESS || next == LeaseReturnScoreStatus.COMPLETED
            LeaseReturnScoreStatus.IN_PROGRESS -> return next == LeaseReturnScoreStatus.COMPLETED || next == LeaseReturnScoreStatus.CANCELLED
            LeaseReturnScoreStatus.COMPLETED -> return next == LeaseReturnScoreStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LeaseReturnScoreStatus) {
        if (!canMoveTo(next)) {
            error("lease return score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"lease return score"} $code"

    fun adjustedExposure(): Double = round((sequence.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): LeaseReturnScore {
        return LeaseReturnScore(
            code = code,
            name = name,
            checksum = checksum,
            unit = unit,
            phone = phone,
            label = label,
            sequence = sequence,
            distanceKm = distanceKm,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lease return score in status planned. */
fun newLeaseReturnScore(code: String, name: String): LeaseReturnScore {
    return LeaseReturnScore(
        code = code,
        name = name,
        checksum = "",
        unit = "",
        phone = "",
        label = "",
        sequence = 0,
        distanceKm = 0.0,
        segment = "",
        status = LeaseReturnScoreStatus.PLANNED,
    )
}
