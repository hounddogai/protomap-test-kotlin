package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback rate card ledger records of freightpay's rate card work, and the rules, stores, and
// calculations around them.

/** Where a fallback rate card ledger is in its life cycle. */
enum class FallbackRateCardLedgerStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A fallback rate card ledger as the rate card team keeps it. */
data class FallbackRateCardLedger(
    var code: String,
    var title: String,
    var ceiling: Double,
    var fuelLiters: Double,
    var version: Int,
    var archived: Boolean,
    var attempts: Int,
    var status: FallbackRateCardLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRateCardLedgerStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRateCardLedgerStatus): Boolean {
        when (status) {
            FallbackRateCardLedgerStatus.PLANNED -> return next == FallbackRateCardLedgerStatus.CANCELLED
            FallbackRateCardLedgerStatus.SCHEDULED -> {
                when (next) {
                    FallbackRateCardLedgerStatus.COMPLETED, FallbackRateCardLedgerStatus.CANCELLED -> return true
                }
                return false
            }
            FallbackRateCardLedgerStatus.IN_PROGRESS -> return next == FallbackRateCardLedgerStatus.COMPLETED || next == FallbackRateCardLedgerStatus.CANCELLED
            FallbackRateCardLedgerStatus.COMPLETED -> return next == FallbackRateCardLedgerStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: FallbackRateCardLedgerStatus) {
        if (!canMoveTo(next)) {
            error("fallback rate card ledger $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun effectiveTotal(): Double = round((version.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): FallbackRateCardLedger {
        return FallbackRateCardLedger(
            code = code,
            title = title,
            ceiling = ceiling,
            fuelLiters = fuelLiters,
            version = version,
            archived = archived,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback rate card ledger in status planned. */
fun newFallbackRateCardLedger(code: String, title: String): FallbackRateCardLedger {
    return FallbackRateCardLedger(
        code = code,
        title = title,
        ceiling = 0.0,
        fuelLiters = 0.0,
        version = 0,
        archived = false,
        attempts = 0,
        status = FallbackRateCardLedgerStatus.PLANNED,
    )
}
