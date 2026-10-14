package com.acme.transport.network.servicearea.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal service area ledger is in its life cycle. */
enum class SeasonalServiceAreaLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A seasonal service area ledger. Its code is unique within a tenant. */
data class SeasonalServiceAreaLedger(
    var code: String,
    var title: String,
    var label: String,
    var reviewer: String,
    var carrier: String,
    var locale: String,
    var maximum: Double,
    var fuelLiters: Double,
    var priority: Int,
    var status: SeasonalServiceAreaLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalServiceAreaLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalServiceAreaLedgerStatus): Boolean {
        when (status) {
            SeasonalServiceAreaLedgerStatus.IDLE -> return next == SeasonalServiceAreaLedgerStatus.STOPPED
            SeasonalServiceAreaLedgerStatus.WARMING -> return next == SeasonalServiceAreaLedgerStatus.DRAINING
            SeasonalServiceAreaLedgerStatus.READY -> return next == SeasonalServiceAreaLedgerStatus.DRAINING || next == SeasonalServiceAreaLedgerStatus.STOPPED
            SeasonalServiceAreaLedgerStatus.DRAINING -> {
                when (next) {
                    SeasonalServiceAreaLedgerStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalServiceAreaLedgerStatus) {
        if (!canMoveTo(next)) {
            error("seasonal service area ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun projectedVolume(): Double = round((maximum + fuelLiters) * 100) / 100

    fun snapshot(): SeasonalServiceAreaLedger {
        return SeasonalServiceAreaLedger(
            code = code,
            title = title,
            label = label,
            reviewer = reviewer,
            carrier = carrier,
            locale = locale,
            maximum = maximum,
            fuelLiters = fuelLiters,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a seasonal service area ledger in status idle. */
fun newSeasonalServiceAreaLedger(code: String, title: String): SeasonalServiceAreaLedger {
    return SeasonalServiceAreaLedger(
        code = code,
        title = title,
        label = "",
        reviewer = "",
        carrier = "",
        locale = "",
        maximum = 0.0,
        fuelLiters = 0.0,
        priority = 0,
        status = SeasonalServiceAreaLedgerStatus.IDLE,
    )
}
