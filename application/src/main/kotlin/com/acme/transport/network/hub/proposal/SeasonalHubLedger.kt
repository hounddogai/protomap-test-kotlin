package com.acme.transport.network.hub.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal hub ledger is in its life cycle. */
enum class SeasonalHubLedgerStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a seasonal hub ledger and its current status. */
data class SeasonalHubLedger(
    var code: String,
    var name: String,
    var notes: String,
    var quantity: Int,
    var lane: String,
    var startsAt: Int,
    var label: String,
    var status: SeasonalHubLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalHubLedgerStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalHubLedgerStatus): Boolean {
        when (status) {
            SeasonalHubLedgerStatus.REQUESTED -> return next == SeasonalHubLedgerStatus.EXPIRED
            SeasonalHubLedgerStatus.GRANTED -> return next == SeasonalHubLedgerStatus.REVOKED
            SeasonalHubLedgerStatus.REVOKED -> {
                when (next) {
                    SeasonalHubLedgerStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalHubLedgerStatus) {
        if (!canMoveTo(next)) {
            error("seasonal hub ledger $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"seasonal hub ledger"} $code"

    fun adjustedTotal(): Double = round((quantity.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): SeasonalHubLedger {
        return SeasonalHubLedger(
            code = code,
            name = name,
            notes = notes,
            quantity = quantity,
            lane = lane,
            startsAt = startsAt,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a seasonal hub ledger in status requested. */
fun newSeasonalHubLedger(code: String, name: String): SeasonalHubLedger {
    return SeasonalHubLedger(
        code = code,
        name = name,
        notes = "",
        quantity = 0,
        lane = "",
        startsAt = 0,
        label = "",
        status = SeasonalHubLedgerStatus.REQUESTED,
    )
}
