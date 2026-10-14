package com.acme.transport.linehaul.backhaul.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global backhaul ledger records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a global backhaul ledger is in its life cycle. */
enum class GlobalBackhaulLedgerStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a global backhaul ledger and its current status. */
data class GlobalBackhaulLedger(
    var code: String,
    var name: String,
    var summary: String,
    var country: String,
    var maximum: Double,
    var loadKg: Double,
    var category: String,
    var ratio: Double,
    var status: GlobalBackhaulLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalBackhaulLedgerStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalBackhaulLedgerStatus): Boolean {
        when (status) {
            GlobalBackhaulLedgerStatus.DRAFT -> return next == GlobalBackhaulLedgerStatus.PENDING || next == GlobalBackhaulLedgerStatus.ACTIVE
            GlobalBackhaulLedgerStatus.PENDING -> {
                when (next) {
                    GlobalBackhaulLedgerStatus.SUSPENDED, GlobalBackhaulLedgerStatus.CLOSED -> return true
                }
                return false
            }
            GlobalBackhaulLedgerStatus.ACTIVE -> return next == GlobalBackhaulLedgerStatus.SUSPENDED || next == GlobalBackhaulLedgerStatus.CLOSED
            GlobalBackhaulLedgerStatus.SUSPENDED -> {
                when (next) {
                    GlobalBackhaulLedgerStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalBackhaulLedgerStatus) {
        if (!canMoveTo(next)) {
            error("global backhaul ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global backhaul ledger"} $code"

    fun effectiveTotal(): Double = round(ratio * loadKg * 100) / 100

    fun snapshot(): GlobalBackhaulLedger {
        return GlobalBackhaulLedger(
            code = code,
            name = name,
            summary = summary,
            country = country,
            maximum = maximum,
            loadKg = loadKg,
            category = category,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a global backhaul ledger in status draft. */
fun newGlobalBackhaulLedger(code: String, name: String): GlobalBackhaulLedger {
    return GlobalBackhaulLedger(
        code = code,
        name = name,
        summary = "",
        country = "",
        maximum = 0.0,
        loadKg = 0.0,
        category = "",
        ratio = 0.0,
        status = GlobalBackhaulLedgerStatus.DRAFT,
    )
}
