package com.acme.transport.network.cutoff.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual cutoff ledger records of network's cutoff work, and the rules, stores, and calculations around
// them.

/** Where a manual cutoff ledger is in its life cycle. */
enum class ManualCutoffLedgerStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A manual cutoff ledger as the cutoff team keeps it. */
data class ManualCutoffLedger(
    var code: String,
    var label: String,
    var limit: Int,
    var durationMinutes: Int,
    var quantity: Int,
    var pallets: Int,
    var checksum: String,
    var status: ManualCutoffLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCutoffLedgerStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCutoffLedgerStatus): Boolean {
        when (status) {
            ManualCutoffLedgerStatus.DRAFT -> {
                when (next) {
                    ManualCutoffLedgerStatus.PENDING -> return true
                }
                return false
            }
            ManualCutoffLedgerStatus.PENDING -> {
                when (next) {
                    ManualCutoffLedgerStatus.ACTIVE -> return true
                }
                return false
            }
            ManualCutoffLedgerStatus.ACTIVE -> return next == ManualCutoffLedgerStatus.SUSPENDED || next == ManualCutoffLedgerStatus.CLOSED
            ManualCutoffLedgerStatus.SUSPENDED -> {
                when (next) {
                    ManualCutoffLedgerStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualCutoffLedgerStatus) {
        if (!canMoveTo(next)) {
            error("manual cutoff ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedLoad(): Double = round((limit.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): ManualCutoffLedger {
        return ManualCutoffLedger(
            code = code,
            label = label,
            limit = limit,
            durationMinutes = durationMinutes,
            quantity = quantity,
            pallets = pallets,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual cutoff ledger in status draft. */
fun newManualCutoffLedger(code: String, label: String): ManualCutoffLedger {
    return ManualCutoffLedger(
        code = code,
        label = label,
        limit = 0,
        durationMinutes = 0,
        quantity = 0,
        pallets = 0,
        checksum = "",
        status = ManualCutoffLedgerStatus.DRAFT,
    )
}
