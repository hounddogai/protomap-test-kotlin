package com.acme.transport.freightpay.dispute.disputemanifest

import com.acme.transport.kit.text.normalize

/** Where a tiered dispute target is in its life cycle. */
enum class TieredDisputeTargetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tiered dispute target. Its code is unique within a tenant. */
data class TieredDisputeTarget(
    var code: String,
    var label: String,
    var dock: String,
    var attempts: Int,
    var unit: String,
    var status: TieredDisputeTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDisputeTargetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDisputeTargetStatus): Boolean {
        when (status) {
            TieredDisputeTargetStatus.OPEN -> return next == TieredDisputeTargetStatus.REOPENED
            TieredDisputeTargetStatus.IN_REVIEW -> return next == TieredDisputeTargetStatus.REOPENED || next == TieredDisputeTargetStatus.ARCHIVED
            TieredDisputeTargetStatus.RESOLVED -> return next == TieredDisputeTargetStatus.ARCHIVED
            TieredDisputeTargetStatus.REOPENED -> {
                when (next) {
                    TieredDisputeTargetStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredDisputeTargetStatus) {
        if (!canMoveTo(next)) {
            error("tiered dispute target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): TieredDisputeTarget {
        return TieredDisputeTarget(
            code = code,
            label = label,
            dock = dock,
            attempts = attempts,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a tiered dispute target in status open. */
fun newTieredDisputeTarget(code: String, label: String): TieredDisputeTarget {
    return TieredDisputeTarget(
        code = code,
        label = label,
        dock = "",
        attempts = 0,
        unit = "",
        status = TieredDisputeTargetStatus.OPEN,
    )
}
