package com.acme.transport.terminal.crossdock.docksummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered cross dock mapping is in its life cycle. */
enum class TieredCrossDockMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a tiered cross dock mapping and its current status. */
data class TieredCrossDockMapping(
    var code: String,
    var name: String,
    var odometer: Int,
    var timezone: String,
    var endsAt: Int,
    var attempts: Int,
    var status: TieredCrossDockMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCrossDockMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCrossDockMappingStatus): Boolean {
        when (status) {
            TieredCrossDockMappingStatus.PLANNED -> return next == TieredCrossDockMappingStatus.IN_PROGRESS || next == TieredCrossDockMappingStatus.CANCELLED
            TieredCrossDockMappingStatus.SCHEDULED -> return next == TieredCrossDockMappingStatus.IN_PROGRESS
            TieredCrossDockMappingStatus.IN_PROGRESS -> return next == TieredCrossDockMappingStatus.COMPLETED || next == TieredCrossDockMappingStatus.CANCELLED
            TieredCrossDockMappingStatus.COMPLETED -> {
                when (next) {
                    TieredCrossDockMappingStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredCrossDockMappingStatus) {
        if (!canMoveTo(next)) {
            error("tiered cross dock mapping $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tiered cross dock mapping"} $code"

    fun effectiveTotal(): Double = round(attempts.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): TieredCrossDockMapping {
        return TieredCrossDockMapping(
            code = code,
            name = name,
            odometer = odometer,
            timezone = timezone,
            endsAt = endsAt,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered cross dock mapping in status planned. */
fun newTieredCrossDockMapping(code: String, name: String): TieredCrossDockMapping {
    return TieredCrossDockMapping(
        code = code,
        name = name,
        odometer = 0,
        timezone = "",
        endsAt = 0,
        attempts = 0,
        status = TieredCrossDockMappingStatus.PLANNED,
    )
}
