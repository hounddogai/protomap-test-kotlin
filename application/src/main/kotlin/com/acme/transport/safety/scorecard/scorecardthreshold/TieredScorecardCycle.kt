package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered scorecard cycle is in its life cycle. */
enum class TieredScorecardCycleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a tiered scorecard cycle and its current status. */
data class TieredScorecardCycle(
    var code: String,
    var title: String,
    var rate: Double,
    var odometer: Int,
    var endsAt: Int,
    var externalRef: String,
    var locale: String,
    var status: TieredScorecardCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredScorecardCycleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredScorecardCycleStatus): Boolean {
        when (status) {
            TieredScorecardCycleStatus.PROPOSED -> return next == TieredScorecardCycleStatus.APPROVED || next == TieredScorecardCycleStatus.REJECTED
            TieredScorecardCycleStatus.APPROVED -> return next == TieredScorecardCycleStatus.REJECTED
            TieredScorecardCycleStatus.REJECTED -> {
                when (next) {
                    TieredScorecardCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredScorecardCycleStatus) {
        if (!canMoveTo(next)) {
            error("tiered scorecard cycle $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tiered scorecard cycle"} $code"

    fun projectedLoad(): Double = round(odometer.toDouble() * rate * 100) / 100

    fun snapshot(): TieredScorecardCycle {
        return TieredScorecardCycle(
            code = code,
            title = title,
            rate = rate,
            odometer = odometer,
            endsAt = endsAt,
            externalRef = externalRef,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a tiered scorecard cycle in status proposed. */
fun newTieredScorecardCycle(code: String, title: String): TieredScorecardCycle {
    return TieredScorecardCycle(
        code = code,
        title = title,
        rate = 0.0,
        odometer = 0,
        endsAt = 0,
        externalRef = "",
        locale = "",
        status = TieredScorecardCycleStatus.PROPOSED,
    )
}
