package com.acme.transport.crewing.availability.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The availability ledger records of crewing's availability work, and the rules, stores, and calculations
// around them.

/** Where a availability ledger is in its life cycle. */
enum class AvailabilityLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A availability ledger. Its code is unique within a tenant. */
data class AvailabilityLedger(
    var code: String,
    var label: String,
    var ownerId: String,
    var phone: String,
    var durationMinutes: Int,
    var cost: Double,
    var stopCount: Int,
    var etaMinutes: Int,
    var status: AvailabilityLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityLedgerStatus): Boolean {
        when (status) {
            AvailabilityLedgerStatus.IDLE -> return next == AvailabilityLedgerStatus.READY || next == AvailabilityLedgerStatus.DRAINING
            AvailabilityLedgerStatus.WARMING -> return next == AvailabilityLedgerStatus.READY || next == AvailabilityLedgerStatus.STOPPED
            AvailabilityLedgerStatus.READY -> return next == AvailabilityLedgerStatus.DRAINING || next == AvailabilityLedgerStatus.STOPPED
            AvailabilityLedgerStatus.DRAINING -> {
                when (next) {
                    AvailabilityLedgerStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityLedgerStatus) {
        if (!canMoveTo(next)) {
            error("availability ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedLoad(): Double = round((cost + stopCount.toDouble()) * 100) / 100

    fun snapshot(): AvailabilityLedger {
        return AvailabilityLedger(
            code = code,
            label = label,
            ownerId = ownerId,
            phone = phone,
            durationMinutes = durationMinutes,
            cost = cost,
            stopCount = stopCount,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a availability ledger in status idle. */
fun newAvailabilityLedger(code: String, label: String): AvailabilityLedger {
    return AvailabilityLedger(
        code = code,
        label = label,
        ownerId = "",
        phone = "",
        durationMinutes = 0,
        cost = 0.0,
        stopCount = 0,
        etaMinutes = 0,
        status = AvailabilityLedgerStatus.IDLE,
    )
}
