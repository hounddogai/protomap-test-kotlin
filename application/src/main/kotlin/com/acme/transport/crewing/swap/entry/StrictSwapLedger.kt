package com.acme.transport.crewing.swap.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict swap ledger is in its life cycle. */
enum class StrictSwapLedgerStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a strict swap ledger and its current status. */
data class StrictSwapLedger(
    var code: String,
    var name: String,
    var maximum: Double,
    var batchSize: Int,
    var shiftCode: String,
    var capacity: Int,
    var distanceKm: Double,
    var status: StrictSwapLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictSwapLedgerStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictSwapLedgerStatus): Boolean {
        when (status) {
            StrictSwapLedgerStatus.PROPOSED -> return next == StrictSwapLedgerStatus.REJECTED || next == StrictSwapLedgerStatus.WITHDRAWN
            StrictSwapLedgerStatus.APPROVED -> return next == StrictSwapLedgerStatus.REJECTED || next == StrictSwapLedgerStatus.WITHDRAWN
            StrictSwapLedgerStatus.REJECTED -> return next == StrictSwapLedgerStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: StrictSwapLedgerStatus) {
        if (!canMoveTo(next)) {
            error("strict swap ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedExposure(): Double = round((capacity.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): StrictSwapLedger {
        return StrictSwapLedger(
            code = code,
            name = name,
            maximum = maximum,
            batchSize = batchSize,
            shiftCode = shiftCode,
            capacity = capacity,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict swap ledger in status proposed. */
fun newStrictSwapLedger(code: String, name: String): StrictSwapLedger {
    return StrictSwapLedger(
        code = code,
        name = name,
        maximum = 0.0,
        batchSize = 0,
        shiftCode = "",
        capacity = 0,
        distanceKm = 0.0,
        status = StrictSwapLedgerStatus.PROPOSED,
    )
}
