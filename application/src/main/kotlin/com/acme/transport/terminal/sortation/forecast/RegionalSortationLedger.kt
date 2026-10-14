package com.acme.transport.terminal.sortation.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional sortation ledger is in its life cycle. */
enum class RegionalSortationLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A regional sortation ledger. Its code is unique within a tenant. */
data class RegionalSortationLedger(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var ceiling: Double,
    var limit: Int,
    var status: RegionalSortationLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalSortationLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalSortationLedgerStatus): Boolean {
        when (status) {
            RegionalSortationLedgerStatus.IDLE -> {
                when (next) {
                    RegionalSortationLedgerStatus.READY -> return true
                }
                return false
            }
            RegionalSortationLedgerStatus.WARMING -> return next == RegionalSortationLedgerStatus.STOPPED
            RegionalSortationLedgerStatus.READY -> return next == RegionalSortationLedgerStatus.DRAINING || next == RegionalSortationLedgerStatus.STOPPED
            RegionalSortationLedgerStatus.DRAINING -> return next == RegionalSortationLedgerStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RegionalSortationLedgerStatus) {
        if (!canMoveTo(next)) {
            error("regional sortation ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedLoad(): Double = round(limit.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): RegionalSortationLedger {
        return RegionalSortationLedger(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            ceiling = ceiling,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional sortation ledger in status idle. */
fun newRegionalSortationLedger(code: String, name: String): RegionalSortationLedger {
    return RegionalSortationLedger(
        code = code,
        name = name,
        fuelLiters = 0.0,
        ceiling = 0.0,
        limit = 0,
        status = RegionalSortationLedgerStatus.IDLE,
    )
}
