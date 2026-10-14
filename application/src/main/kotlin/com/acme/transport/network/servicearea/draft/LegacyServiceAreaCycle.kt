package com.acme.transport.network.servicearea.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy service area cycle is in its life cycle. */
enum class LegacyServiceAreaCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy service area cycle as the service area team keeps it. */
data class LegacyServiceAreaCycle(
    var code: String,
    var title: String,
    var region: String,
    var distanceKm: Double,
    var segment: String,
    var attempts: Int,
    var priority: Int,
    var visible: Boolean,
    var discount: Double,
    var fuelLiters: Double,
    var status: LegacyServiceAreaCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyServiceAreaCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyServiceAreaCycleStatus): Boolean {
        when (status) {
            LegacyServiceAreaCycleStatus.PLANNED -> {
                when (next) {
                    LegacyServiceAreaCycleStatus.SCHEDULED, LegacyServiceAreaCycleStatus.COMPLETED -> return true
                }
                return false
            }
            LegacyServiceAreaCycleStatus.SCHEDULED -> {
                when (next) {
                    LegacyServiceAreaCycleStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LegacyServiceAreaCycleStatus.IN_PROGRESS -> return next == LegacyServiceAreaCycleStatus.CANCELLED
            LegacyServiceAreaCycleStatus.COMPLETED -> return next == LegacyServiceAreaCycleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyServiceAreaCycleStatus) {
        if (!canMoveTo(next)) {
            error("legacy service area cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy service area cycle"} $code"

    fun effectiveLoad(): Double = round((attempts.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): LegacyServiceAreaCycle {
        return LegacyServiceAreaCycle(
            code = code,
            title = title,
            region = region,
            distanceKm = distanceKm,
            segment = segment,
            attempts = attempts,
            priority = priority,
            visible = visible,
            discount = discount,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a legacy service area cycle in status planned. */
fun newLegacyServiceAreaCycle(code: String, title: String): LegacyServiceAreaCycle {
    return LegacyServiceAreaCycle(
        code = code,
        title = title,
        region = "",
        distanceKm = 0.0,
        segment = "",
        attempts = 0,
        priority = 0,
        visible = false,
        discount = 0.0,
        fuelLiters = 0.0,
        status = LegacyServiceAreaCycleStatus.PLANNED,
    )
}
