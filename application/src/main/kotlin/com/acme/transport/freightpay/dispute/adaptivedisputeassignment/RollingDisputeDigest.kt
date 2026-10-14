package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling dispute digest is in its life cycle. */
enum class RollingDisputeDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a rolling dispute digest and its current status. */
data class RollingDisputeDigest(
    var code: String,
    var title: String,
    var budget: Double,
    var phone: String,
    var trailerId: String,
    var distanceKm: Double,
    var status: RollingDisputeDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDisputeDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDisputeDigestStatus): Boolean {
        when (status) {
            RollingDisputeDigestStatus.REQUESTED -> {
                when (next) {
                    RollingDisputeDigestStatus.REVOKED -> return true
                }
                return false
            }
            RollingDisputeDigestStatus.GRANTED -> return next == RollingDisputeDigestStatus.REVOKED || next == RollingDisputeDigestStatus.EXPIRED
            RollingDisputeDigestStatus.REVOKED -> return next == RollingDisputeDigestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RollingDisputeDigestStatus) {
        if (!canMoveTo(next)) {
            error("rolling dispute digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedLoad(): Double = round((budget - distanceKm) * 100) / 100

    fun snapshot(): RollingDisputeDigest {
        return RollingDisputeDigest(
            code = code,
            title = title,
            budget = budget,
            phone = phone,
            trailerId = trailerId,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling dispute digest in status requested. */
fun newRollingDisputeDigest(code: String, title: String): RollingDisputeDigest {
    return RollingDisputeDigest(
        code = code,
        title = title,
        budget = 0.0,
        phone = "",
        trailerId = "",
        distanceKm = 0.0,
        status = RollingDisputeDigestStatus.REQUESTED,
    )
}
