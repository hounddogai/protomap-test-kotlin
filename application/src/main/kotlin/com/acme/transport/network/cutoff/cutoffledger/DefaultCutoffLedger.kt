package com.acme.transport.network.cutoff.cutoffledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default cutoff ledger records of network's cutoff work, and the rules, stores, and calculations around
// them.

/** Where a default cutoff ledger is in its life cycle. */
enum class DefaultCutoffLedgerStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A default cutoff ledger as the cutoff team keeps it. */
data class DefaultCutoffLedger(
    var code: String,
    var label: String,
    var lane: String,
    var batchSize: Int,
    var phone: String,
    var routeCode: String,
    var cost: Double,
    var status: DefaultCutoffLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCutoffLedgerStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCutoffLedgerStatus): Boolean {
        when (status) {
            DefaultCutoffLedgerStatus.NEW -> {
                when (next) {
                    DefaultCutoffLedgerStatus.RUNNING -> return true
                }
                return false
            }
            DefaultCutoffLedgerStatus.QUEUED -> return next == DefaultCutoffLedgerStatus.RUNNING || next == DefaultCutoffLedgerStatus.FAILED
            DefaultCutoffLedgerStatus.RUNNING -> return next == DefaultCutoffLedgerStatus.FAILED
            DefaultCutoffLedgerStatus.DONE -> {
                when (next) {
                    DefaultCutoffLedgerStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultCutoffLedgerStatus) {
        if (!canMoveTo(next)) {
            error("default cutoff ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedVolume(): Double = round(cost * batchSize.toDouble() * 100) / 100

    fun snapshot(): DefaultCutoffLedger {
        return DefaultCutoffLedger(
            code = code,
            label = label,
            lane = lane,
            batchSize = batchSize,
            phone = phone,
            routeCode = routeCode,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default cutoff ledger in status new. */
fun newDefaultCutoffLedger(code: String, label: String): DefaultCutoffLedger {
    return DefaultCutoffLedger(
        code = code,
        label = label,
        lane = "",
        batchSize = 0,
        phone = "",
        routeCode = "",
        cost = 0.0,
        status = DefaultCutoffLedgerStatus.NEW,
    )
}
