package com.acme.transport.fleetops.utilization.utilizationledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary utilization ledger records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a primary utilization ledger is in its life cycle. */
enum class PrimaryUtilizationLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One primary utilization ledger, with what the planners need to act on it. */
data class PrimaryUtilizationLedger(
    var code: String,
    var label: String,
    var summary: String,
    var channel: String,
    var expiresAt: Int,
    var cost: Double,
    var etaMinutes: Int,
    var ownerId: String,
    var status: PrimaryUtilizationLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryUtilizationLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryUtilizationLedgerStatus): Boolean {
        when (status) {
            PrimaryUtilizationLedgerStatus.IDLE -> return next == PrimaryUtilizationLedgerStatus.WARMING
            PrimaryUtilizationLedgerStatus.WARMING -> {
                when (next) {
                    PrimaryUtilizationLedgerStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryUtilizationLedgerStatus.READY -> {
                when (next) {
                    PrimaryUtilizationLedgerStatus.DRAINING, PrimaryUtilizationLedgerStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryUtilizationLedgerStatus.DRAINING -> return next == PrimaryUtilizationLedgerStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PrimaryUtilizationLedgerStatus) {
        if (!canMoveTo(next)) {
            error("primary utilization ledger $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedValue(): Double = round((etaMinutes.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): PrimaryUtilizationLedger {
        return PrimaryUtilizationLedger(
            code = code,
            label = label,
            summary = summary,
            channel = channel,
            expiresAt = expiresAt,
            cost = cost,
            etaMinutes = etaMinutes,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a primary utilization ledger in status idle. */
fun newPrimaryUtilizationLedger(code: String, label: String): PrimaryUtilizationLedger {
    return PrimaryUtilizationLedger(
        code = code,
        label = label,
        summary = "",
        channel = "",
        expiresAt = 0,
        cost = 0.0,
        etaMinutes = 0,
        ownerId = "",
        status = PrimaryUtilizationLedgerStatus.IDLE,
    )
}
