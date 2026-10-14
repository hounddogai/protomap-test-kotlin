package com.acme.transport.lastmile.parcel.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal parcel ledger is in its life cycle. */
enum class SeasonalParcelLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A seasonal parcel ledger. Its code is unique within a tenant. */
data class SeasonalParcelLedger(
    var code: String,
    var name: String,
    var timezone: String,
    var pinned: Boolean,
    var title: String,
    var threshold: Double,
    var amount: Double,
    var lane: String,
    var status: SeasonalParcelLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalParcelLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalParcelLedgerStatus): Boolean {
        when (status) {
            SeasonalParcelLedgerStatus.IDLE -> {
                when (next) {
                    SeasonalParcelLedgerStatus.WARMING -> return true
                }
                return false
            }
            SeasonalParcelLedgerStatus.WARMING -> {
                when (next) {
                    SeasonalParcelLedgerStatus.DRAINING -> return true
                }
                return false
            }
            SeasonalParcelLedgerStatus.READY -> return next == SeasonalParcelLedgerStatus.DRAINING
            SeasonalParcelLedgerStatus.DRAINING -> return next == SeasonalParcelLedgerStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: SeasonalParcelLedgerStatus) {
        if (!canMoveTo(next)) {
            error("seasonal parcel ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedVolume(): Double = round((amount - threshold) * 100) / 100

    fun snapshot(): SeasonalParcelLedger {
        return SeasonalParcelLedger(
            code = code,
            name = name,
            timezone = timezone,
            pinned = pinned,
            title = title,
            threshold = threshold,
            amount = amount,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a seasonal parcel ledger in status idle. */
fun newSeasonalParcelLedger(code: String, name: String): SeasonalParcelLedger {
    return SeasonalParcelLedger(
        code = code,
        name = name,
        timezone = "",
        pinned = false,
        title = "",
        threshold = 0.0,
        amount = 0.0,
        lane = "",
        status = SeasonalParcelLedgerStatus.IDLE,
    )
}
