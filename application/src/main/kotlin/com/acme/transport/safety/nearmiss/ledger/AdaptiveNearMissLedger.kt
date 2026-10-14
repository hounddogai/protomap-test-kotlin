package com.acme.transport.safety.nearmiss.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive near miss ledger records of safety's near miss work, and the rules, stores, and calculations
// around them.

/** Where a adaptive near miss ledger is in its life cycle. */
enum class AdaptiveNearMissLedgerStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a adaptive near miss ledger and its current status. */
data class AdaptiveNearMissLedger(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var expiresAt: Int,
    var graceDays: Int,
    var trailerId: String,
    var visible: Boolean,
    var locale: String,
    var status: AdaptiveNearMissLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveNearMissLedgerStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveNearMissLedgerStatus): Boolean {
        when (status) {
            AdaptiveNearMissLedgerStatus.OPEN -> {
                when (next) {
                    AdaptiveNearMissLedgerStatus.IN_REVIEW -> return true
                }
                return false
            }
            AdaptiveNearMissLedgerStatus.IN_REVIEW -> return next == AdaptiveNearMissLedgerStatus.RESOLVED || next == AdaptiveNearMissLedgerStatus.REOPENED
            AdaptiveNearMissLedgerStatus.RESOLVED -> return next == AdaptiveNearMissLedgerStatus.ARCHIVED
            AdaptiveNearMissLedgerStatus.REOPENED -> return next == AdaptiveNearMissLedgerStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AdaptiveNearMissLedgerStatus) {
        if (!canMoveTo(next)) {
            error("adaptive near miss ledger $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveTotal(): Double = round((expiresAt.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveNearMissLedger {
        return AdaptiveNearMissLedger(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            expiresAt = expiresAt,
            graceDays = graceDays,
            trailerId = trailerId,
            visible = visible,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a adaptive near miss ledger in status open. */
fun newAdaptiveNearMissLedger(code: String, label: String): AdaptiveNearMissLedger {
    return AdaptiveNearMissLedger(
        code = code,
        label = label,
        etaMinutes = 0,
        expiresAt = 0,
        graceDays = 0,
        trailerId = "",
        visible = false,
        locale = "",
        status = AdaptiveNearMissLedgerStatus.OPEN,
    )
}
