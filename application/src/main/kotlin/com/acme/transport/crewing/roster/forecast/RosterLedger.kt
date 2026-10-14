package com.acme.transport.crewing.roster.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster ledger is in its life cycle. */
enum class RosterLedgerStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a roster ledger and its current status. */
data class RosterLedger(
    var code: String,
    var label: String,
    var reviewer: String,
    var driverId: String,
    var batchSize: Int,
    var fuelLiters: Double,
    var category: String,
    var minimum: Double,
    var windowDays: Int,
    var ownerId: String,
    var status: RosterLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterLedgerStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterLedgerStatus): Boolean {
        when (status) {
            RosterLedgerStatus.DRAFT -> return next == RosterLedgerStatus.CLOSED
            RosterLedgerStatus.PENDING -> return next == RosterLedgerStatus.ACTIVE || next == RosterLedgerStatus.SUSPENDED
            RosterLedgerStatus.ACTIVE -> return next == RosterLedgerStatus.SUSPENDED || next == RosterLedgerStatus.CLOSED
            RosterLedgerStatus.SUSPENDED -> return next == RosterLedgerStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RosterLedgerStatus) {
        if (!canMoveTo(next)) {
            error("roster ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedLoad(): Double = round((fuelLiters + batchSize.toDouble()) * 100) / 100

    fun snapshot(): RosterLedger {
        return RosterLedger(
            code = code,
            label = label,
            reviewer = reviewer,
            driverId = driverId,
            batchSize = batchSize,
            fuelLiters = fuelLiters,
            category = category,
            minimum = minimum,
            windowDays = windowDays,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a roster ledger in status draft. */
fun newRosterLedger(code: String, label: String): RosterLedger {
    return RosterLedger(
        code = code,
        label = label,
        reviewer = "",
        driverId = "",
        batchSize = 0,
        fuelLiters = 0.0,
        category = "",
        minimum = 0.0,
        windowDays = 0,
        ownerId = "",
        status = RosterLedgerStatus.DRAFT,
    )
}
