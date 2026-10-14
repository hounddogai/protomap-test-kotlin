package com.acme.transport.network.transittime.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The transit time ledger records of network's transit time work, and the rules, stores, and calculations
// around them.

/** Where a transit time ledger is in its life cycle. */
enum class TransitTimeLedgerStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A transit time ledger. Its code is unique within a tenant. */
data class TransitTimeLedger(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var locale: String,
    var attempts: Int,
    var name: String,
    var maximum: Double,
    var priority: Int,
    var status: TransitTimeLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeLedgerStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeLedgerStatus): Boolean {
        when (status) {
            TransitTimeLedgerStatus.PLANNED -> {
                when (next) {
                    TransitTimeLedgerStatus.COMPLETED, TransitTimeLedgerStatus.CANCELLED -> return true
                }
                return false
            }
            TransitTimeLedgerStatus.SCHEDULED -> return next == TransitTimeLedgerStatus.IN_PROGRESS || next == TransitTimeLedgerStatus.COMPLETED
            TransitTimeLedgerStatus.IN_PROGRESS -> {
                when (next) {
                    TransitTimeLedgerStatus.COMPLETED, TransitTimeLedgerStatus.CANCELLED -> return true
                }
                return false
            }
            TransitTimeLedgerStatus.COMPLETED -> return next == TransitTimeLedgerStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TransitTimeLedgerStatus) {
        if (!canMoveTo(next)) {
            error("transit time ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"transit time ledger"} $code"

    fun projectedExposure(): Double = round(attempts.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): TransitTimeLedger {
        return TransitTimeLedger(
            code = code,
            label = label,
            pinned = pinned,
            locale = locale,
            attempts = attempts,
            name = name,
            maximum = maximum,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a transit time ledger in status planned. */
fun newTransitTimeLedger(code: String, label: String): TransitTimeLedger {
    return TransitTimeLedger(
        code = code,
        label = label,
        pinned = false,
        locale = "",
        attempts = 0,
        name = "",
        maximum = 0.0,
        priority = 0,
        status = TransitTimeLedgerStatus.PLANNED,
    )
}
