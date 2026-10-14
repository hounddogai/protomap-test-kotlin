package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered cross dock rollup records of terminal's cross dock work, and the rules, stores, and calculations
// around them.

/** Where a tiered cross dock rollup is in its life cycle. */
enum class TieredCrossDockRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a tiered cross dock rollup and its current status. */
data class TieredCrossDockRollup(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var locale: String,
    var graceDays: Int,
    var status: TieredCrossDockRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCrossDockRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCrossDockRollupStatus): Boolean {
        when (status) {
            TieredCrossDockRollupStatus.OPEN -> {
                when (next) {
                    TieredCrossDockRollupStatus.REOPENED, TieredCrossDockRollupStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredCrossDockRollupStatus.IN_REVIEW -> return next == TieredCrossDockRollupStatus.ARCHIVED
            TieredCrossDockRollupStatus.RESOLVED -> {
                when (next) {
                    TieredCrossDockRollupStatus.REOPENED, TieredCrossDockRollupStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredCrossDockRollupStatus.REOPENED -> return next == TieredCrossDockRollupStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TieredCrossDockRollupStatus) {
        if (!canMoveTo(next)) {
            error("tiered cross dock rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveExposure(): Double = round(fuelLiters * graceDays.toDouble() * 100) / 100

    fun snapshot(): TieredCrossDockRollup {
        return TieredCrossDockRollup(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            locale = locale,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a tiered cross dock rollup in status open. */
fun newTieredCrossDockRollup(code: String, name: String): TieredCrossDockRollup {
    return TieredCrossDockRollup(
        code = code,
        name = name,
        fuelLiters = 0.0,
        locale = "",
        graceDays = 0,
        status = TieredCrossDockRollupStatus.OPEN,
    )
}
