package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted route offset slot is in its life cycle. */
enum class WeightedRouteOffsetSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A weighted route offset slot as the route offset team keeps it. */
data class WeightedRouteOffsetSlot(
    var code: String,
    var label: String,
    var floorValue: Double,
    var retries: Int,
    var createdAt: Int,
    var locale: String,
    var status: WeightedRouteOffsetSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedRouteOffsetSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedRouteOffsetSlotStatus): Boolean {
        when (status) {
            WeightedRouteOffsetSlotStatus.OPEN -> {
                when (next) {
                    WeightedRouteOffsetSlotStatus.RESOLVED, WeightedRouteOffsetSlotStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedRouteOffsetSlotStatus.IN_REVIEW -> {
                when (next) {
                    WeightedRouteOffsetSlotStatus.RESOLVED -> return true
                }
                return false
            }
            WeightedRouteOffsetSlotStatus.RESOLVED -> return next == WeightedRouteOffsetSlotStatus.REOPENED || next == WeightedRouteOffsetSlotStatus.ARCHIVED
            WeightedRouteOffsetSlotStatus.REOPENED -> {
                when (next) {
                    WeightedRouteOffsetSlotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedRouteOffsetSlotStatus) {
        if (!canMoveTo(next)) {
            error("weighted route offset slot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"weighted route offset slot"} $code"

    fun netVolume(): Double = round((retries.toDouble() - floorValue) * 100) / 100

    fun snapshot(): WeightedRouteOffsetSlot {
        return WeightedRouteOffsetSlot(
            code = code,
            label = label,
            floorValue = floorValue,
            retries = retries,
            createdAt = createdAt,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a weighted route offset slot in status open. */
fun newWeightedRouteOffsetSlot(code: String, label: String): WeightedRouteOffsetSlot {
    return WeightedRouteOffsetSlot(
        code = code,
        label = label,
        floorValue = 0.0,
        retries = 0,
        createdAt = 0,
        locale = "",
        status = WeightedRouteOffsetSlotStatus.OPEN,
    )
}
