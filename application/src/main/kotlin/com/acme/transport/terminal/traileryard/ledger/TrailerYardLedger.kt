package com.acme.transport.terminal.traileryard.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The trailer yard ledger records of terminal's trailer yard work, and the rules, stores, and calculations
// around them.

/** Where a trailer yard ledger is in its life cycle. */
enum class TrailerYardLedgerStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A trailer yard ledger as the trailer yard team keeps it. */
data class TrailerYardLedger(
    var code: String,
    var name: String,
    var endsAt: Int,
    var pallets: Int,
    var segment: String,
    var threshold: Double,
    var ratio: Double,
    var etaMinutes: Int,
    var status: TrailerYardLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardLedgerStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardLedgerStatus): Boolean {
        when (status) {
            TrailerYardLedgerStatus.PLANNED -> return next == TrailerYardLedgerStatus.IN_PROGRESS || next == TrailerYardLedgerStatus.CANCELLED
            TrailerYardLedgerStatus.SCHEDULED -> {
                when (next) {
                    TrailerYardLedgerStatus.COMPLETED -> return true
                }
                return false
            }
            TrailerYardLedgerStatus.IN_PROGRESS -> {
                when (next) {
                    TrailerYardLedgerStatus.CANCELLED -> return true
                }
                return false
            }
            TrailerYardLedgerStatus.COMPLETED -> return next == TrailerYardLedgerStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TrailerYardLedgerStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard ledger $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun weightedValue(): Double = round((endsAt.toDouble() + ratio) * 100) / 100

    fun snapshot(): TrailerYardLedger {
        return TrailerYardLedger(
            code = code,
            name = name,
            endsAt = endsAt,
            pallets = pallets,
            segment = segment,
            threshold = threshold,
            ratio = ratio,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a trailer yard ledger in status planned. */
fun newTrailerYardLedger(code: String, name: String): TrailerYardLedger {
    return TrailerYardLedger(
        code = code,
        name = name,
        endsAt = 0,
        pallets = 0,
        segment = "",
        threshold = 0.0,
        ratio = 0.0,
        etaMinutes = 0,
        status = TrailerYardLedgerStatus.PLANNED,
    )
}
