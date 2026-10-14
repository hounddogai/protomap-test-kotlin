package com.acme.transport.safety.coaching.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending coaching ledger records of safety's coaching work, and the rules, stores, and calculations
// around them.

/** Where a pending coaching ledger is in its life cycle. */
enum class PendingCoachingLedgerStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One pending coaching ledger, with what the planners need to act on it. */
data class PendingCoachingLedger(
    var code: String,
    var title: String,
    var currency: String,
    var name: String,
    var unit: String,
    var discount: Double,
    var endsAt: Int,
    var status: PendingCoachingLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCoachingLedgerStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCoachingLedgerStatus): Boolean {
        when (status) {
            PendingCoachingLedgerStatus.PLANNED -> {
                when (next) {
                    PendingCoachingLedgerStatus.SCHEDULED -> return true
                }
                return false
            }
            PendingCoachingLedgerStatus.SCHEDULED -> return next == PendingCoachingLedgerStatus.COMPLETED || next == PendingCoachingLedgerStatus.CANCELLED
            PendingCoachingLedgerStatus.IN_PROGRESS -> {
                when (next) {
                    PendingCoachingLedgerStatus.COMPLETED, PendingCoachingLedgerStatus.CANCELLED -> return true
                }
                return false
            }
            PendingCoachingLedgerStatus.COMPLETED -> return next == PendingCoachingLedgerStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingCoachingLedgerStatus) {
        if (!canMoveTo(next)) {
            error("pending coaching ledger $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"pending coaching ledger"} $code"

    fun netLoad(): Double = round(discount * endsAt.toDouble() * 100) / 100

    fun snapshot(): PendingCoachingLedger {
        return PendingCoachingLedger(
            code = code,
            title = title,
            currency = currency,
            name = name,
            unit = unit,
            discount = discount,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a pending coaching ledger in status planned. */
fun newPendingCoachingLedger(code: String, title: String): PendingCoachingLedger {
    return PendingCoachingLedger(
        code = code,
        title = title,
        currency = "",
        name = "",
        unit = "",
        discount = 0.0,
        endsAt = 0,
        status = PendingCoachingLedgerStatus.PLANNED,
    )
}
