package com.acme.transport.safety.violation.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The violation ledger records of safety's violation work, and the rules, stores, and calculations around
// them.

/** Where a violation ledger is in its life cycle. */
enum class ViolationLedgerStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a violation ledger and its current status. */
data class ViolationLedger(
    var code: String,
    var label: String,
    var country: String,
    var priority: Int,
    var quantity: Int,
    var fuelLiters: Double,
    var minimum: Double,
    var shiftCode: String,
    var status: ViolationLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationLedgerStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationLedgerStatus): Boolean {
        when (status) {
            ViolationLedgerStatus.OPEN -> {
                when (next) {
                    ViolationLedgerStatus.IN_REVIEW -> return true
                }
                return false
            }
            ViolationLedgerStatus.IN_REVIEW -> return next == ViolationLedgerStatus.REOPENED || next == ViolationLedgerStatus.ARCHIVED
            ViolationLedgerStatus.RESOLVED -> return next == ViolationLedgerStatus.REOPENED
            ViolationLedgerStatus.REOPENED -> return next == ViolationLedgerStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ViolationLedgerStatus) {
        if (!canMoveTo(next)) {
            error("violation ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedExposure(): Double = round((quantity.toDouble() + minimum) * 100) / 100

    fun snapshot(): ViolationLedger {
        return ViolationLedger(
            code = code,
            label = label,
            country = country,
            priority = priority,
            quantity = quantity,
            fuelLiters = fuelLiters,
            minimum = minimum,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a violation ledger in status open. */
fun newViolationLedger(code: String, label: String): ViolationLedger {
    return ViolationLedger(
        code = code,
        label = label,
        country = "",
        priority = 0,
        quantity = 0,
        fuelLiters = 0.0,
        minimum = 0.0,
        shiftCode = "",
        status = ViolationLedgerStatus.OPEN,
    )
}
