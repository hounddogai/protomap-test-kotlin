package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal sortation adjustment records of terminal's sortation work, and the rules, stores, and
// calculations around them.

/** Where a seasonal sortation adjustment is in its life cycle. */
enum class SeasonalSortationAdjustmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a seasonal sortation adjustment and its current status. */
data class SeasonalSortationAdjustment(
    var code: String,
    var name: String,
    var timezone: String,
    var expiresAt: Int,
    var locale: String,
    var stopCount: Int,
    var cost: Double,
    var status: SeasonalSortationAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalSortationAdjustmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalSortationAdjustmentStatus): Boolean {
        when (status) {
            SeasonalSortationAdjustmentStatus.PLANNED -> {
                when (next) {
                    SeasonalSortationAdjustmentStatus.IN_PROGRESS, SeasonalSortationAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalSortationAdjustmentStatus.SCHEDULED -> return next == SeasonalSortationAdjustmentStatus.COMPLETED || next == SeasonalSortationAdjustmentStatus.CANCELLED
            SeasonalSortationAdjustmentStatus.IN_PROGRESS -> {
                when (next) {
                    SeasonalSortationAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalSortationAdjustmentStatus.COMPLETED -> {
                when (next) {
                    SeasonalSortationAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalSortationAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal sortation adjustment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun weightedTotal(): Double = round(cost * expiresAt.toDouble() * 100) / 100

    fun snapshot(): SeasonalSortationAdjustment {
        return SeasonalSortationAdjustment(
            code = code,
            name = name,
            timezone = timezone,
            expiresAt = expiresAt,
            locale = locale,
            stopCount = stopCount,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a seasonal sortation adjustment in status planned. */
fun newSeasonalSortationAdjustment(code: String, name: String): SeasonalSortationAdjustment {
    return SeasonalSortationAdjustment(
        code = code,
        name = name,
        timezone = "",
        expiresAt = 0,
        locale = "",
        stopCount = 0,
        cost = 0.0,
        status = SeasonalSortationAdjustmentStatus.PLANNED,
    )
}
