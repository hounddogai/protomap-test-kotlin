package com.acme.transport.emissions.idling.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling ledger is in its life cycle. */
enum class IdlingLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One idling ledger, with what the planners need to act on it. */
data class IdlingLedger(
    var code: String,
    var title: String,
    var region: String,
    var retries: Int,
    var quantity: Int,
    var dock: String,
    var reviewer: String,
    var status: IdlingLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingLedgerStatus): Boolean {
        when (status) {
            IdlingLedgerStatus.IDLE -> return next == IdlingLedgerStatus.STOPPED
            IdlingLedgerStatus.WARMING -> {
                when (next) {
                    IdlingLedgerStatus.READY, IdlingLedgerStatus.STOPPED -> return true
                }
                return false
            }
            IdlingLedgerStatus.READY -> return next == IdlingLedgerStatus.DRAINING || next == IdlingLedgerStatus.STOPPED
            IdlingLedgerStatus.DRAINING -> {
                when (next) {
                    IdlingLedgerStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingLedgerStatus) {
        if (!canMoveTo(next)) {
            error("idling ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedValue(): Double = round((quantity.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): IdlingLedger {
        return IdlingLedger(
            code = code,
            title = title,
            region = region,
            retries = retries,
            quantity = quantity,
            dock = dock,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a idling ledger in status idle. */
fun newIdlingLedger(code: String, title: String): IdlingLedger {
    return IdlingLedger(
        code = code,
        title = title,
        region = "",
        retries = 0,
        quantity = 0,
        dock = "",
        reviewer = "",
        status = IdlingLedgerStatus.IDLE,
    )
}
