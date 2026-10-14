package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal zone adjustment is in its life cycle. */
enum class SeasonalZoneAdjustmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A seasonal zone adjustment as the zone team keeps it. */
data class SeasonalZoneAdjustment(
    var code: String,
    var name: String,
    var driverId: String,
    var phone: String,
    var attempts: Int,
    var ceiling: Double,
    var status: SeasonalZoneAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalZoneAdjustmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalZoneAdjustmentStatus): Boolean {
        when (status) {
            SeasonalZoneAdjustmentStatus.OPEN -> {
                when (next) {
                    SeasonalZoneAdjustmentStatus.RESOLVED -> return true
                }
                return false
            }
            SeasonalZoneAdjustmentStatus.IN_REVIEW -> {
                when (next) {
                    SeasonalZoneAdjustmentStatus.RESOLVED, SeasonalZoneAdjustmentStatus.REOPENED -> return true
                }
                return false
            }
            SeasonalZoneAdjustmentStatus.RESOLVED -> {
                when (next) {
                    SeasonalZoneAdjustmentStatus.REOPENED, SeasonalZoneAdjustmentStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalZoneAdjustmentStatus.REOPENED -> return next == SeasonalZoneAdjustmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: SeasonalZoneAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal zone adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedVolume(): Double = round(ceiling * attempts.toDouble() * 100) / 100

    fun snapshot(): SeasonalZoneAdjustment {
        return SeasonalZoneAdjustment(
            code = code,
            name = name,
            driverId = driverId,
            phone = phone,
            attempts = attempts,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal zone adjustment in status open. */
fun newSeasonalZoneAdjustment(code: String, name: String): SeasonalZoneAdjustment {
    return SeasonalZoneAdjustment(
        code = code,
        name = name,
        driverId = "",
        phone = "",
        attempts = 0,
        ceiling = 0.0,
        status = SeasonalZoneAdjustmentStatus.OPEN,
    )
}
