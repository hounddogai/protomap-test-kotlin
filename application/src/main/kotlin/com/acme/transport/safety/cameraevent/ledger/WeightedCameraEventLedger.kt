package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted camera event ledger records of safety's camera event work, and the rules, stores, and
// calculations around them.

/** Where a weighted camera event ledger is in its life cycle. */
enum class WeightedCameraEventLedgerStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a weighted camera event ledger and its current status. */
data class WeightedCameraEventLedger(
    var code: String,
    var label: String,
    var reviewer: String,
    var createdAt: Int,
    var quantity: Int,
    var limit: Int,
    var status: WeightedCameraEventLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedCameraEventLedgerStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedCameraEventLedgerStatus): Boolean {
        when (status) {
            WeightedCameraEventLedgerStatus.PROPOSED -> return next == WeightedCameraEventLedgerStatus.APPROVED || next == WeightedCameraEventLedgerStatus.WITHDRAWN
            WeightedCameraEventLedgerStatus.APPROVED -> return next == WeightedCameraEventLedgerStatus.WITHDRAWN
            WeightedCameraEventLedgerStatus.REJECTED -> return next == WeightedCameraEventLedgerStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: WeightedCameraEventLedgerStatus) {
        if (!canMoveTo(next)) {
            error("weighted camera event ledger $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedExposure(): Double = round((quantity.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedCameraEventLedger {
        return WeightedCameraEventLedger(
            code = code,
            label = label,
            reviewer = reviewer,
            createdAt = createdAt,
            quantity = quantity,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted camera event ledger in status proposed. */
fun newWeightedCameraEventLedger(code: String, label: String): WeightedCameraEventLedger {
    return WeightedCameraEventLedger(
        code = code,
        label = label,
        reviewer = "",
        createdAt = 0,
        quantity = 0,
        limit = 0,
        status = WeightedCameraEventLedgerStatus.PROPOSED,
    )
}
