package com.acme.transport.freightpay.dispute.disputeforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending dispute digest is in its life cycle. */
enum class PendingDisputeDigestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A pending dispute digest as the dispute team keeps it. */
data class PendingDisputeDigest(
    var code: String,
    var title: String,
    var version: Int,
    var reviewer: String,
    var graceDays: Int,
    var depot: String,
    var unit: String,
    var status: PendingDisputeDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDisputeDigestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDisputeDigestStatus): Boolean {
        when (status) {
            PendingDisputeDigestStatus.DRAFT -> {
                when (next) {
                    PendingDisputeDigestStatus.CLOSED -> return true
                }
                return false
            }
            PendingDisputeDigestStatus.PENDING -> return next == PendingDisputeDigestStatus.ACTIVE || next == PendingDisputeDigestStatus.SUSPENDED
            PendingDisputeDigestStatus.ACTIVE -> {
                when (next) {
                    PendingDisputeDigestStatus.SUSPENDED, PendingDisputeDigestStatus.CLOSED -> return true
                }
                return false
            }
            PendingDisputeDigestStatus.SUSPENDED -> return next == PendingDisputeDigestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PendingDisputeDigestStatus) {
        if (!canMoveTo(next)) {
            error("pending dispute digest $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun weightedLoad(): Double = round(graceDays.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): PendingDisputeDigest {
        return PendingDisputeDigest(
            code = code,
            title = title,
            version = version,
            reviewer = reviewer,
            graceDays = graceDays,
            depot = depot,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a pending dispute digest in status draft. */
fun newPendingDisputeDigest(code: String, title: String): PendingDisputeDigest {
    return PendingDisputeDigest(
        code = code,
        title = title,
        version = 0,
        reviewer = "",
        graceDays = 0,
        depot = "",
        unit = "",
        status = PendingDisputeDigestStatus.DRAFT,
    )
}
