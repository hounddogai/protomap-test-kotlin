package com.acme.transport.freightpay.dispute.review

import com.acme.transport.kit.text.normalize

/** Where a pending dispute baseline is in its life cycle. */
enum class PendingDisputeBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One pending dispute baseline, with what the planners need to act on it. */
data class PendingDisputeBaseline(
    var code: String,
    var label: String,
    var depot: String,
    var capacity: Int,
    var description: String,
    var status: PendingDisputeBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDisputeBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDisputeBaselineStatus): Boolean {
        when (status) {
            PendingDisputeBaselineStatus.DRAFT -> return next == PendingDisputeBaselineStatus.PENDING
            PendingDisputeBaselineStatus.PENDING -> return next == PendingDisputeBaselineStatus.ACTIVE || next == PendingDisputeBaselineStatus.SUSPENDED
            PendingDisputeBaselineStatus.ACTIVE -> return next == PendingDisputeBaselineStatus.SUSPENDED || next == PendingDisputeBaselineStatus.CLOSED
            PendingDisputeBaselineStatus.SUSPENDED -> {
                when (next) {
                    PendingDisputeBaselineStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingDisputeBaselineStatus) {
        if (!canMoveTo(next)) {
            error("pending dispute baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = capacity.toDouble()

    fun snapshot(): PendingDisputeBaseline {
        return PendingDisputeBaseline(
            code = code,
            label = label,
            depot = depot,
            capacity = capacity,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a pending dispute baseline in status draft. */
fun newPendingDisputeBaseline(code: String, label: String): PendingDisputeBaseline {
    return PendingDisputeBaseline(
        code = code,
        label = label,
        depot = "",
        capacity = 0,
        description = "",
        status = PendingDisputeBaselineStatus.DRAFT,
    )
}
