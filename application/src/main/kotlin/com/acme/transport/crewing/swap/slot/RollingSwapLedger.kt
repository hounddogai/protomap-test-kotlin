package com.acme.transport.crewing.swap.slot

import com.acme.transport.kit.text.normalize

/** Where a rolling swap ledger is in its life cycle. */
enum class RollingSwapLedgerStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A rolling swap ledger as the swap team keeps it. */
data class RollingSwapLedger(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var region: String,
    var notes: String,
    var updatedAt: Int,
    var trailerId: String,
    var reviewer: String,
    var status: RollingSwapLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingSwapLedgerStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingSwapLedgerStatus): Boolean {
        when (status) {
            RollingSwapLedgerStatus.REQUESTED -> {
                when (next) {
                    RollingSwapLedgerStatus.GRANTED, RollingSwapLedgerStatus.EXPIRED -> return true
                }
                return false
            }
            RollingSwapLedgerStatus.GRANTED -> {
                when (next) {
                    RollingSwapLedgerStatus.REVOKED -> return true
                }
                return false
            }
            RollingSwapLedgerStatus.REVOKED -> return next == RollingSwapLedgerStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RollingSwapLedgerStatus) {
        if (!canMoveTo(next)) {
            error("rolling swap ledger $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"rolling swap ledger"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = updatedAt.toDouble()

    fun snapshot(): RollingSwapLedger {
        return RollingSwapLedger(
            code = code,
            label = label,
            tags = tags,
            region = region,
            notes = notes,
            updatedAt = updatedAt,
            trailerId = trailerId,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rolling swap ledger in status requested. */
fun newRollingSwapLedger(code: String, label: String): RollingSwapLedger {
    return RollingSwapLedger(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        region = "",
        notes = "",
        updatedAt = 0,
        trailerId = "",
        reviewer = "",
        status = RollingSwapLedgerStatus.REQUESTED,
    )
}
