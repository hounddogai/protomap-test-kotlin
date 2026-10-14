package com.acme.transport.linehaul.tender.tenderrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict tender rollup records of linehaul's tender work, and the rules, stores, and calculations around
// them.

/** Where a strict tender rollup is in its life cycle. */
enum class StrictTenderRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One strict tender rollup, with what the planners need to act on it. */
data class StrictTenderRollup(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var name: String,
    var expiresAt: Int,
    var amount: Double,
    var status: StrictTenderRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTenderRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTenderRollupStatus): Boolean {
        when (status) {
            StrictTenderRollupStatus.OPEN -> return next == StrictTenderRollupStatus.REOPENED || next == StrictTenderRollupStatus.ARCHIVED
            StrictTenderRollupStatus.IN_REVIEW -> {
                when (next) {
                    StrictTenderRollupStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictTenderRollupStatus.RESOLVED -> return next == StrictTenderRollupStatus.REOPENED || next == StrictTenderRollupStatus.ARCHIVED
            StrictTenderRollupStatus.REOPENED -> return next == StrictTenderRollupStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: StrictTenderRollupStatus) {
        if (!canMoveTo(next)) {
            error("strict tender rollup $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedTotal(): Double = round((updatedAt.toDouble() - amount) * 100) / 100

    fun snapshot(): StrictTenderRollup {
        return StrictTenderRollup(
            code = code,
            label = label,
            updatedAt = updatedAt,
            name = name,
            expiresAt = expiresAt,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict tender rollup in status open. */
fun newStrictTenderRollup(code: String, label: String): StrictTenderRollup {
    return StrictTenderRollup(
        code = code,
        label = label,
        updatedAt = 0,
        name = "",
        expiresAt = 0,
        amount = 0.0,
        status = StrictTenderRollupStatus.OPEN,
    )
}
