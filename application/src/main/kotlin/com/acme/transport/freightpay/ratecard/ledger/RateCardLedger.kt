package com.acme.transport.freightpay.ratecard.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rate card ledger records of freightpay's rate card work, and the rules, stores, and calculations around
// them.

/** Where a rate card ledger is in its life cycle. */
enum class RateCardLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a rate card ledger and its current status. */
data class RateCardLedger(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var limit: Int,
    var depot: String,
    var status: RateCardLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardLedgerStatus): Boolean {
        when (status) {
            RateCardLedgerStatus.CREATED -> return next == RateCardLedgerStatus.VALIDATED
            RateCardLedgerStatus.VALIDATED -> {
                when (next) {
                    RateCardLedgerStatus.PUBLISHED, RateCardLedgerStatus.RETIRED -> return true
                }
                return false
            }
            RateCardLedgerStatus.PUBLISHED -> return next == RateCardLedgerStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RateCardLedgerStatus) {
        if (!canMoveTo(next)) {
            error("rate card ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedExposure(): Double = round((durationMinutes.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): RateCardLedger {
        return RateCardLedger(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            limit = limit,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rate card ledger in status created. */
fun newRateCardLedger(code: String, title: String): RateCardLedger {
    return RateCardLedger(
        code = code,
        title = title,
        durationMinutes = 0,
        limit = 0,
        depot = "",
        status = RateCardLedgerStatus.CREATED,
    )
}
