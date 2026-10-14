package com.acme.transport.safety.nearmiss.missclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default near miss ledger is in its life cycle. */
enum class DefaultNearMissLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a default near miss ledger and its current status. */
data class DefaultNearMissLedger(
    var code: String,
    var title: String,
    var driverId: String,
    var sequence: Int,
    var threshold: Double,
    var graceDays: Int,
    var approved: Boolean,
    var attempts: Int,
    var etaMinutes: Int,
    var status: DefaultNearMissLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultNearMissLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultNearMissLedgerStatus): Boolean {
        when (status) {
            DefaultNearMissLedgerStatus.CREATED -> return next == DefaultNearMissLedgerStatus.VALIDATED || next == DefaultNearMissLedgerStatus.PUBLISHED
            DefaultNearMissLedgerStatus.VALIDATED -> {
                when (next) {
                    DefaultNearMissLedgerStatus.PUBLISHED -> return true
                }
                return false
            }
            DefaultNearMissLedgerStatus.PUBLISHED -> {
                when (next) {
                    DefaultNearMissLedgerStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultNearMissLedgerStatus) {
        if (!canMoveTo(next)) {
            error("default near miss ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default near miss ledger"} $code"

    fun adjustedValue(): Double = round((sequence.toDouble() + attempts.toDouble()) * 100) / 100

    fun snapshot(): DefaultNearMissLedger {
        return DefaultNearMissLedger(
            code = code,
            title = title,
            driverId = driverId,
            sequence = sequence,
            threshold = threshold,
            graceDays = graceDays,
            approved = approved,
            attempts = attempts,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default near miss ledger in status created. */
fun newDefaultNearMissLedger(code: String, title: String): DefaultNearMissLedger {
    return DefaultNearMissLedger(
        code = code,
        title = title,
        driverId = "",
        sequence = 0,
        threshold = 0.0,
        graceDays = 0,
        approved = false,
        attempts = 0,
        etaMinutes = 0,
        status = DefaultNearMissLedgerStatus.CREATED,
    )
}
