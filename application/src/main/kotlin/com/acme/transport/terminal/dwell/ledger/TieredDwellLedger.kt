package com.acme.transport.terminal.dwell.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered dwell ledger records of terminal's dwell work, and the rules, stores, and calculations around
// them.

/** Where a tiered dwell ledger is in its life cycle. */
enum class TieredDwellLedgerStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a tiered dwell ledger and its current status. */
data class TieredDwellLedger(
    var code: String,
    var title: String,
    var description: String,
    var label: String,
    var loadKg: Double,
    var channel: String,
    var version: Int,
    var graceDays: Int,
    var status: TieredDwellLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDwellLedgerStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDwellLedgerStatus): Boolean {
        when (status) {
            TieredDwellLedgerStatus.PLANNED -> return next == TieredDwellLedgerStatus.IN_PROGRESS || next == TieredDwellLedgerStatus.CANCELLED
            TieredDwellLedgerStatus.SCHEDULED -> return next == TieredDwellLedgerStatus.COMPLETED || next == TieredDwellLedgerStatus.CANCELLED
            TieredDwellLedgerStatus.IN_PROGRESS -> return next == TieredDwellLedgerStatus.COMPLETED || next == TieredDwellLedgerStatus.CANCELLED
            TieredDwellLedgerStatus.COMPLETED -> return next == TieredDwellLedgerStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TieredDwellLedgerStatus) {
        if (!canMoveTo(next)) {
            error("tiered dwell ledger $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code: $title"

    fun projectedLoad(): Double = round(version.toDouble() * loadKg * 100) / 100

    fun snapshot(): TieredDwellLedger {
        return TieredDwellLedger(
            code = code,
            title = title,
            description = description,
            label = label,
            loadKg = loadKg,
            channel = channel,
            version = version,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a tiered dwell ledger in status planned. */
fun newTieredDwellLedger(code: String, title: String): TieredDwellLedger {
    return TieredDwellLedger(
        code = code,
        title = title,
        description = "",
        label = "",
        loadKg = 0.0,
        channel = "",
        version = 0,
        graceDays = 0,
        status = TieredDwellLedgerStatus.PLANNED,
    )
}
