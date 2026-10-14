package com.acme.transport.customs.hold.holdaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary hold ledger is in its life cycle. */
enum class PrimaryHoldLedgerStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One primary hold ledger, with what the planners need to act on it. */
data class PrimaryHoldLedger(
    var code: String,
    var title: String,
    var capacity: Int,
    var limit: Int,
    var durationMinutes: Int,
    var threshold: Double,
    var status: PrimaryHoldLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryHoldLedgerStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryHoldLedgerStatus): Boolean {
        when (status) {
            PrimaryHoldLedgerStatus.PROPOSED -> {
                when (next) {
                    PrimaryHoldLedgerStatus.REJECTED, PrimaryHoldLedgerStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryHoldLedgerStatus.APPROVED -> return next == PrimaryHoldLedgerStatus.REJECTED || next == PrimaryHoldLedgerStatus.WITHDRAWN
            PrimaryHoldLedgerStatus.REJECTED -> return next == PrimaryHoldLedgerStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PrimaryHoldLedgerStatus) {
        if (!canMoveTo(next)) {
            error("primary hold ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary hold ledger"} $code"

    fun adjustedVolume(): Double = round((capacity.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): PrimaryHoldLedger {
        return PrimaryHoldLedger(
            code = code,
            title = title,
            capacity = capacity,
            limit = limit,
            durationMinutes = durationMinutes,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary hold ledger in status proposed. */
fun newPrimaryHoldLedger(code: String, title: String): PrimaryHoldLedger {
    return PrimaryHoldLedger(
        code = code,
        title = title,
        capacity = 0,
        limit = 0,
        durationMinutes = 0,
        threshold = 0.0,
        status = PrimaryHoldLedgerStatus.PROPOSED,
    )
}
