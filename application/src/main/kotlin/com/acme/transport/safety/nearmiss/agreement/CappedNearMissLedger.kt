package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.kit.text.normalize

/** Where a capped near miss ledger is in its life cycle. */
enum class CappedNearMissLedgerStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One capped near miss ledger, with what the planners need to act on it. */
data class CappedNearMissLedger(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var visible: Boolean,
    var lane: String,
    var channel: String,
    var status: CappedNearMissLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedNearMissLedgerStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedNearMissLedgerStatus): Boolean {
        when (status) {
            CappedNearMissLedgerStatus.OPEN -> return next == CappedNearMissLedgerStatus.REOPENED || next == CappedNearMissLedgerStatus.ARCHIVED
            CappedNearMissLedgerStatus.IN_REVIEW -> {
                when (next) {
                    CappedNearMissLedgerStatus.ARCHIVED -> return true
                }
                return false
            }
            CappedNearMissLedgerStatus.RESOLVED -> return next == CappedNearMissLedgerStatus.REOPENED || next == CappedNearMissLedgerStatus.ARCHIVED
            CappedNearMissLedgerStatus.REOPENED -> {
                when (next) {
                    CappedNearMissLedgerStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedNearMissLedgerStatus) {
        if (!canMoveTo(next)) {
            error("capped near miss ledger $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): CappedNearMissLedger {
        return CappedNearMissLedger(
            code = code,
            title = title,
            expiresAt = expiresAt,
            visible = visible,
            lane = lane,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a capped near miss ledger in status open. */
fun newCappedNearMissLedger(code: String, title: String): CappedNearMissLedger {
    return CappedNearMissLedger(
        code = code,
        title = title,
        expiresAt = 0,
        visible = false,
        lane = "",
        channel = "",
        status = CappedNearMissLedgerStatus.OPEN,
    )
}
