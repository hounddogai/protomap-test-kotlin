package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul ledger is in its life cycle. */
enum class BackhaulLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One backhaul ledger, with what the planners need to act on it. */
data class BackhaulLedger(
    var code: String,
    var name: String,
    var startsAt: Int,
    var cost: Double,
    var active: Boolean,
    var sequence: Int,
    var checksum: String,
    var attempts: Int,
    var rate: Double,
    var maximum: Double,
    var status: BackhaulLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulLedgerStatus): Boolean {
        when (status) {
            BackhaulLedgerStatus.IDLE -> return next == BackhaulLedgerStatus.STOPPED
            BackhaulLedgerStatus.WARMING -> return next == BackhaulLedgerStatus.READY || next == BackhaulLedgerStatus.DRAINING
            BackhaulLedgerStatus.READY -> {
                when (next) {
                    BackhaulLedgerStatus.DRAINING, BackhaulLedgerStatus.STOPPED -> return true
                }
                return false
            }
            BackhaulLedgerStatus.DRAINING -> return next == BackhaulLedgerStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BackhaulLedgerStatus) {
        if (!canMoveTo(next)) {
            error("backhaul ledger $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"backhaul ledger"} $code"

    fun netVolume(): Double = round((sequence.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): BackhaulLedger {
        return BackhaulLedger(
            code = code,
            name = name,
            startsAt = startsAt,
            cost = cost,
            active = active,
            sequence = sequence,
            checksum = checksum,
            attempts = attempts,
            rate = rate,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a backhaul ledger in status idle. */
fun newBackhaulLedger(code: String, name: String): BackhaulLedger {
    return BackhaulLedger(
        code = code,
        name = name,
        startsAt = 0,
        cost = 0.0,
        active = false,
        sequence = 0,
        checksum = "",
        attempts = 0,
        rate = 0.0,
        maximum = 0.0,
        status = BackhaulLedgerStatus.IDLE,
    )
}
