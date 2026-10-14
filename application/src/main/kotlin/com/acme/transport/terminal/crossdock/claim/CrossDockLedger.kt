package com.acme.transport.terminal.crossdock.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock ledger is in its life cycle. */
enum class CrossDockLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A cross dock ledger as the cross dock team keeps it. */
data class CrossDockLedger(
    var code: String,
    var title: String,
    var currency: String,
    var ceiling: Double,
    var region: String,
    var country: String,
    var tags: MutableList<String>,
    var ratio: Double,
    var notes: String,
    var contactEmail: String,
    var source: String,
    var status: CrossDockLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockLedgerStatus): Boolean {
        when (status) {
            CrossDockLedgerStatus.IDLE -> {
                when (next) {
                    CrossDockLedgerStatus.WARMING, CrossDockLedgerStatus.STOPPED -> return true
                }
                return false
            }
            CrossDockLedgerStatus.WARMING -> return next == CrossDockLedgerStatus.STOPPED
            CrossDockLedgerStatus.READY -> {
                when (next) {
                    CrossDockLedgerStatus.DRAINING, CrossDockLedgerStatus.STOPPED -> return true
                }
                return false
            }
            CrossDockLedgerStatus.DRAINING -> {
                when (next) {
                    CrossDockLedgerStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CrossDockLedgerStatus) {
        if (!canMoveTo(next)) {
            error("cross dock ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedTotal(): Double = round(ceiling * ratio * 100) / 100

    fun snapshot(): CrossDockLedger {
        return CrossDockLedger(
            code = code,
            title = title,
            currency = currency,
            ceiling = ceiling,
            region = region,
            country = country,
            tags = tags,
            ratio = ratio,
            notes = notes,
            contactEmail = contactEmail,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a cross dock ledger in status idle. */
fun newCrossDockLedger(code: String, title: String): CrossDockLedger {
    return CrossDockLedger(
        code = code,
        title = title,
        currency = "",
        ceiling = 0.0,
        region = "",
        country = "",
        tags = mutableListOf<String>(),
        ratio = 0.0,
        notes = "",
        contactEmail = "",
        source = "",
        status = CrossDockLedgerStatus.IDLE,
    )
}
