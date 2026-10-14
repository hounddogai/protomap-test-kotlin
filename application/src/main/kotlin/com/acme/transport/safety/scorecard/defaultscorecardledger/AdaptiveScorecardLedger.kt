package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive scorecard ledger records of safety's scorecard work, and the rules, stores, and calculations
// around them.

/** Where a adaptive scorecard ledger is in its life cycle. */
enum class AdaptiveScorecardLedgerStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A adaptive scorecard ledger. Its code is unique within a tenant. */
data class AdaptiveScorecardLedger(
    var code: String,
    var name: String,
    var createdAt: Int,
    var reviewer: String,
    var quantity: Int,
    var fuelLiters: Double,
    var ownerId: String,
    var channel: String,
    var label: String,
    var status: AdaptiveScorecardLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveScorecardLedgerStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveScorecardLedgerStatus): Boolean {
        when (status) {
            AdaptiveScorecardLedgerStatus.PROPOSED -> {
                when (next) {
                    AdaptiveScorecardLedgerStatus.APPROVED, AdaptiveScorecardLedgerStatus.REJECTED -> return true
                }
                return false
            }
            AdaptiveScorecardLedgerStatus.APPROVED -> {
                when (next) {
                    AdaptiveScorecardLedgerStatus.WITHDRAWN -> return true
                }
                return false
            }
            AdaptiveScorecardLedgerStatus.REJECTED -> {
                when (next) {
                    AdaptiveScorecardLedgerStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveScorecardLedgerStatus) {
        if (!canMoveTo(next)) {
            error("adaptive scorecard ledger $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$name]"

    fun netExposure(): Double = round((createdAt.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): AdaptiveScorecardLedger {
        return AdaptiveScorecardLedger(
            code = code,
            name = name,
            createdAt = createdAt,
            reviewer = reviewer,
            quantity = quantity,
            fuelLiters = fuelLiters,
            ownerId = ownerId,
            channel = channel,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a adaptive scorecard ledger in status proposed. */
fun newAdaptiveScorecardLedger(code: String, name: String): AdaptiveScorecardLedger {
    return AdaptiveScorecardLedger(
        code = code,
        name = name,
        createdAt = 0,
        reviewer = "",
        quantity = 0,
        fuelLiters = 0.0,
        ownerId = "",
        channel = "",
        label = "",
        status = AdaptiveScorecardLedgerStatus.PROPOSED,
    )
}
