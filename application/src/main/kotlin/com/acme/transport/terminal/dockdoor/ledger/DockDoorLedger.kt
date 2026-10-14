package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dock door ledger records of terminal's dock door work, and the rules, stores, and calculations around
// them.

/** Where a dock door ledger is in its life cycle. */
enum class DockDoorLedgerStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One dock door ledger, with what the planners need to act on it. */
data class DockDoorLedger(
    var code: String,
    var label: String,
    var priority: Int,
    var depot: String,
    var capacity: Int,
    var cost: Double,
    var version: Int,
    var minimum: Double,
    var sequence: Int,
    var budget: Double,
    var rate: Double,
    var status: DockDoorLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorLedgerStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorLedgerStatus): Boolean {
        when (status) {
            DockDoorLedgerStatus.PLANNED -> {
                when (next) {
                    DockDoorLedgerStatus.SCHEDULED, DockDoorLedgerStatus.IN_PROGRESS -> return true
                }
                return false
            }
            DockDoorLedgerStatus.SCHEDULED -> {
                when (next) {
                    DockDoorLedgerStatus.COMPLETED -> return true
                }
                return false
            }
            DockDoorLedgerStatus.IN_PROGRESS -> {
                when (next) {
                    DockDoorLedgerStatus.COMPLETED -> return true
                }
                return false
            }
            DockDoorLedgerStatus.COMPLETED -> return next == DockDoorLedgerStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DockDoorLedgerStatus) {
        if (!canMoveTo(next)) {
            error("dock door ledger $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun netExposure(): Double = round((priority.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): DockDoorLedger {
        return DockDoorLedger(
            code = code,
            label = label,
            priority = priority,
            depot = depot,
            capacity = capacity,
            cost = cost,
            version = version,
            minimum = minimum,
            sequence = sequence,
            budget = budget,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a dock door ledger in status planned. */
fun newDockDoorLedger(code: String, label: String): DockDoorLedger {
    return DockDoorLedger(
        code = code,
        label = label,
        priority = 0,
        depot = "",
        capacity = 0,
        cost = 0.0,
        version = 0,
        minimum = 0.0,
        sequence = 0,
        budget = 0.0,
        rate = 0.0,
        status = DockDoorLedgerStatus.PLANNED,
    )
}
