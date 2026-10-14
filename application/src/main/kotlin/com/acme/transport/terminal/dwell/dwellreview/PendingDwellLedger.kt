package com.acme.transport.terminal.dwell.dwellreview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending dwell ledger is in its life cycle. */
enum class PendingDwellLedgerStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One pending dwell ledger, with what the planners need to act on it. */
data class PendingDwellLedger(
    var code: String,
    var name: String,
    var timezone: String,
    var threshold: Double,
    var externalRef: String,
    var margin: Double,
    var budget: Double,
    var active: Boolean,
    var pallets: Int,
    var retries: Int,
    var status: PendingDwellLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDwellLedgerStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDwellLedgerStatus): Boolean {
        when (status) {
            PendingDwellLedgerStatus.REQUESTED -> {
                when (next) {
                    PendingDwellLedgerStatus.REVOKED, PendingDwellLedgerStatus.EXPIRED -> return true
                }
                return false
            }
            PendingDwellLedgerStatus.GRANTED -> {
                when (next) {
                    PendingDwellLedgerStatus.EXPIRED -> return true
                }
                return false
            }
            PendingDwellLedgerStatus.REVOKED -> return next == PendingDwellLedgerStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingDwellLedgerStatus) {
        if (!canMoveTo(next)) {
            error("pending dwell ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedVolume(): Double = round(pallets.toDouble() * margin * 100) / 100

    fun snapshot(): PendingDwellLedger {
        return PendingDwellLedger(
            code = code,
            name = name,
            timezone = timezone,
            threshold = threshold,
            externalRef = externalRef,
            margin = margin,
            budget = budget,
            active = active,
            pallets = pallets,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending dwell ledger in status requested. */
fun newPendingDwellLedger(code: String, name: String): PendingDwellLedger {
    return PendingDwellLedger(
        code = code,
        name = name,
        timezone = "",
        threshold = 0.0,
        externalRef = "",
        margin = 0.0,
        budget = 0.0,
        active = false,
        pallets = 0,
        retries = 0,
        status = PendingDwellLedgerStatus.REQUESTED,
    )
}
