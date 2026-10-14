package com.acme.transport.emissions.fuelburn.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fuel burn ledger records of emissions's fuel burn work, and the rules, stores, and calculations around
// them.

/** Where a fuel burn ledger is in its life cycle. */
enum class FuelBurnLedgerStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One fuel burn ledger, with what the planners need to act on it. */
data class FuelBurnLedger(
    var code: String,
    var name: String,
    var notes: String,
    var createdAt: Int,
    var stopCount: Int,
    var unit: String,
    var etaMinutes: Int,
    var pallets: Int,
    var status: FuelBurnLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnLedgerStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnLedgerStatus): Boolean {
        when (status) {
            FuelBurnLedgerStatus.REQUESTED -> {
                when (next) {
                    FuelBurnLedgerStatus.GRANTED -> return true
                }
                return false
            }
            FuelBurnLedgerStatus.GRANTED -> {
                when (next) {
                    FuelBurnLedgerStatus.REVOKED -> return true
                }
                return false
            }
            FuelBurnLedgerStatus.REVOKED -> return next == FuelBurnLedgerStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FuelBurnLedgerStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn ledger $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fuel burn ledger"} $code"

    fun weightedValue(): Double = round((createdAt.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): FuelBurnLedger {
        return FuelBurnLedger(
            code = code,
            name = name,
            notes = notes,
            createdAt = createdAt,
            stopCount = stopCount,
            unit = unit,
            etaMinutes = etaMinutes,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fuel burn ledger in status requested. */
fun newFuelBurnLedger(code: String, name: String): FuelBurnLedger {
    return FuelBurnLedger(
        code = code,
        name = name,
        notes = "",
        createdAt = 0,
        stopCount = 0,
        unit = "",
        etaMinutes = 0,
        pallets = 0,
        status = FuelBurnLedgerStatus.REQUESTED,
    )
}
