package com.acme.transport.safety.coaching.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching ledger is in its life cycle. */
enum class CoachingLedgerStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One coaching ledger, with what the planners need to act on it. */
data class CoachingLedger(
    var code: String,
    var title: String,
    var startsAt: Int,
    var summary: String,
    var minimum: Double,
    var status: CoachingLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingLedgerStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingLedgerStatus): Boolean {
        when (status) {
            CoachingLedgerStatus.REQUESTED -> return next == CoachingLedgerStatus.GRANTED || next == CoachingLedgerStatus.REVOKED
            CoachingLedgerStatus.GRANTED -> {
                when (next) {
                    CoachingLedgerStatus.EXPIRED -> return true
                }
                return false
            }
            CoachingLedgerStatus.REVOKED -> return next == CoachingLedgerStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CoachingLedgerStatus) {
        if (!canMoveTo(next)) {
            error("coaching ledger $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun weightedExposure(): Double = round((minimum + startsAt.toDouble()) * 100) / 100

    fun snapshot(): CoachingLedger {
        return CoachingLedger(
            code = code,
            title = title,
            startsAt = startsAt,
            summary = summary,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a coaching ledger in status requested. */
fun newCoachingLedger(code: String, title: String): CoachingLedger {
    return CoachingLedger(
        code = code,
        title = title,
        startsAt = 0,
        summary = "",
        minimum = 0.0,
        status = CoachingLedgerStatus.REQUESTED,
    )
}
