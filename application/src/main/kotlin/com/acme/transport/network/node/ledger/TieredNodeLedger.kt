package com.acme.transport.network.node.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered node ledger records of network's node work, and the rules, stores, and calculations around them.

/** Where a tiered node ledger is in its life cycle. */
enum class TieredNodeLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One tiered node ledger, with what the planners need to act on it. */
data class TieredNodeLedger(
    var code: String,
    var label: String,
    var depot: String,
    var amount: Double,
    var floorValue: Double,
    var rate: Double,
    var unit: String,
    var contactEmail: String,
    var status: TieredNodeLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredNodeLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredNodeLedgerStatus): Boolean {
        when (status) {
            TieredNodeLedgerStatus.CREATED -> {
                when (next) {
                    TieredNodeLedgerStatus.VALIDATED, TieredNodeLedgerStatus.RETIRED -> return true
                }
                return false
            }
            TieredNodeLedgerStatus.VALIDATED -> {
                when (next) {
                    TieredNodeLedgerStatus.PUBLISHED, TieredNodeLedgerStatus.RETIRED -> return true
                }
                return false
            }
            TieredNodeLedgerStatus.PUBLISHED -> {
                when (next) {
                    TieredNodeLedgerStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredNodeLedgerStatus) {
        if (!canMoveTo(next)) {
            error("tiered node ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedExposure(): Double = round((rate - amount) * 100) / 100

    fun snapshot(): TieredNodeLedger {
        return TieredNodeLedger(
            code = code,
            label = label,
            depot = depot,
            amount = amount,
            floorValue = floorValue,
            rate = rate,
            unit = unit,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a tiered node ledger in status created. */
fun newTieredNodeLedger(code: String, label: String): TieredNodeLedger {
    return TieredNodeLedger(
        code = code,
        label = label,
        depot = "",
        amount = 0.0,
        floorValue = 0.0,
        rate = 0.0,
        unit = "",
        contactEmail = "",
        status = TieredNodeLedgerStatus.CREATED,
    )
}
