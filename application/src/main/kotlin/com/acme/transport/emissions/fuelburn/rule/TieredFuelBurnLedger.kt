package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.kit.text.normalize

/** Where a tiered fuel burn ledger is in its life cycle. */
enum class TieredFuelBurnLedgerStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One tiered fuel burn ledger, with what the planners need to act on it. */
data class TieredFuelBurnLedger(
    var code: String,
    var label: String,
    var channel: String,
    var phone: String,
    var attributes: MutableMap<String, String>,
    var odometer: Int,
    var status: TieredFuelBurnLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredFuelBurnLedgerStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredFuelBurnLedgerStatus): Boolean {
        when (status) {
            TieredFuelBurnLedgerStatus.OPEN -> return next == TieredFuelBurnLedgerStatus.RESOLVED || next == TieredFuelBurnLedgerStatus.REOPENED
            TieredFuelBurnLedgerStatus.IN_REVIEW -> return next == TieredFuelBurnLedgerStatus.ARCHIVED
            TieredFuelBurnLedgerStatus.RESOLVED -> return next == TieredFuelBurnLedgerStatus.REOPENED || next == TieredFuelBurnLedgerStatus.ARCHIVED
            TieredFuelBurnLedgerStatus.REOPENED -> return next == TieredFuelBurnLedgerStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TieredFuelBurnLedgerStatus) {
        if (!canMoveTo(next)) {
            error("tiered fuel burn ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): TieredFuelBurnLedger {
        return TieredFuelBurnLedger(
            code = code,
            label = label,
            channel = channel,
            phone = phone,
            attributes = attributes,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a tiered fuel burn ledger in status open. */
fun newTieredFuelBurnLedger(code: String, label: String): TieredFuelBurnLedger {
    return TieredFuelBurnLedger(
        code = code,
        label = label,
        channel = "",
        phone = "",
        attributes = mutableMapOf<String, String>(),
        odometer = 0,
        status = TieredFuelBurnLedgerStatus.OPEN,
    )
}
