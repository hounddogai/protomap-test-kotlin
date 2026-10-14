package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal ev charge adjustment records of emissions's ev charge work, and the rules, stores, and
// calculations around them.

/** Where a seasonal ev charge adjustment is in its life cycle. */
enum class SeasonalEvChargeAdjustmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A seasonal ev charge adjustment as the ev charge team keeps it. */
data class SeasonalEvChargeAdjustment(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var limit: Int,
    var batchSize: Int,
    var reviewer: String,
    var status: SeasonalEvChargeAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalEvChargeAdjustmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalEvChargeAdjustmentStatus): Boolean {
        when (status) {
            SeasonalEvChargeAdjustmentStatus.DRAFT -> {
                when (next) {
                    SeasonalEvChargeAdjustmentStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalEvChargeAdjustmentStatus.PENDING -> {
                when (next) {
                    SeasonalEvChargeAdjustmentStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalEvChargeAdjustmentStatus.ACTIVE -> {
                when (next) {
                    SeasonalEvChargeAdjustmentStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalEvChargeAdjustmentStatus.SUSPENDED -> return next == SeasonalEvChargeAdjustmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: SeasonalEvChargeAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal ev charge adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal ev charge adjustment"} $code"

    fun weightedLoad(): Double = round(distanceKm * batchSize.toDouble() * 100) / 100

    fun snapshot(): SeasonalEvChargeAdjustment {
        return SeasonalEvChargeAdjustment(
            code = code,
            name = name,
            distanceKm = distanceKm,
            limit = limit,
            batchSize = batchSize,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal ev charge adjustment in status draft. */
fun newSeasonalEvChargeAdjustment(code: String, name: String): SeasonalEvChargeAdjustment {
    return SeasonalEvChargeAdjustment(
        code = code,
        name = name,
        distanceKm = 0.0,
        limit = 0,
        batchSize = 0,
        reviewer = "",
        status = SeasonalEvChargeAdjustmentStatus.DRAFT,
    )
}
