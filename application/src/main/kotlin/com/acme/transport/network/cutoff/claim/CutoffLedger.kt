package com.acme.transport.network.cutoff.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff ledger is in its life cycle. */
enum class CutoffLedgerStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One cutoff ledger, with what the planners need to act on it. */
data class CutoffLedger(
    var code: String,
    var name: String,
    var rate: Double,
    var priority: Int,
    var ratio: Double,
    var status: CutoffLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffLedgerStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffLedgerStatus): Boolean {
        when (status) {
            CutoffLedgerStatus.NEW -> {
                when (next) {
                    CutoffLedgerStatus.QUEUED -> return true
                }
                return false
            }
            CutoffLedgerStatus.QUEUED -> return next == CutoffLedgerStatus.DONE || next == CutoffLedgerStatus.FAILED
            CutoffLedgerStatus.RUNNING -> return next == CutoffLedgerStatus.DONE || next == CutoffLedgerStatus.FAILED
            CutoffLedgerStatus.DONE -> return next == CutoffLedgerStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CutoffLedgerStatus) {
        if (!canMoveTo(next)) {
            error("cutoff ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun netLoad(): Double = round(ratio * rate * 100) / 100

    fun snapshot(): CutoffLedger {
        return CutoffLedger(
            code = code,
            name = name,
            rate = rate,
            priority = priority,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a cutoff ledger in status new. */
fun newCutoffLedger(code: String, name: String): CutoffLedger {
    return CutoffLedger(
        code = code,
        name = name,
        rate = 0.0,
        priority = 0,
        ratio = 0.0,
        status = CutoffLedgerStatus.NEW,
    )
}
