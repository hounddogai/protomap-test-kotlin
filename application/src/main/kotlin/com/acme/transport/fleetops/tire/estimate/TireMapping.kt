package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire mapping is in its life cycle. */
enum class TireMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tire mapping as the tire team keeps it. */
data class TireMapping(
    var code: String,
    var name: String,
    var stopCount: Int,
    var summary: String,
    var startsAt: Int,
    var updatedAt: Int,
    var driverId: String,
    var endsAt: Int,
    var margin: Double,
    var timezone: String,
    var status: TireMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireMappingStatus): Boolean {
        when (status) {
            TireMappingStatus.PLANNED -> return next == TireMappingStatus.COMPLETED
            TireMappingStatus.SCHEDULED -> {
                when (next) {
                    TireMappingStatus.CANCELLED -> return true
                }
                return false
            }
            TireMappingStatus.IN_PROGRESS -> {
                when (next) {
                    TireMappingStatus.COMPLETED, TireMappingStatus.CANCELLED -> return true
                }
                return false
            }
            TireMappingStatus.COMPLETED -> {
                when (next) {
                    TireMappingStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireMappingStatus) {
        if (!canMoveTo(next)) {
            error("tire mapping $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedLoad(): Double = round(endsAt.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): TireMapping {
        return TireMapping(
            code = code,
            name = name,
            stopCount = stopCount,
            summary = summary,
            startsAt = startsAt,
            updatedAt = updatedAt,
            driverId = driverId,
            endsAt = endsAt,
            margin = margin,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tire mapping in status planned. */
fun newTireMapping(code: String, name: String): TireMapping {
    return TireMapping(
        code = code,
        name = name,
        stopCount = 0,
        summary = "",
        startsAt = 0,
        updatedAt = 0,
        driverId = "",
        endsAt = 0,
        margin = 0.0,
        timezone = "",
        status = TireMappingStatus.PLANNED,
    )
}
