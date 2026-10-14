package com.acme.transport.network.transittime.timebatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time cycle is in its life cycle. */
enum class TransitTimeCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One transit time cycle, with what the planners need to act on it. */
data class TransitTimeCycle(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var version: Int,
    var expiresAt: Int,
    var routeCode: String,
    var loadKg: Double,
    var status: TransitTimeCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeCycleStatus): Boolean {
        when (status) {
            TransitTimeCycleStatus.PLANNED -> return next == TransitTimeCycleStatus.IN_PROGRESS
            TransitTimeCycleStatus.SCHEDULED -> {
                when (next) {
                    TransitTimeCycleStatus.COMPLETED, TransitTimeCycleStatus.CANCELLED -> return true
                }
                return false
            }
            TransitTimeCycleStatus.IN_PROGRESS -> return next == TransitTimeCycleStatus.CANCELLED
            TransitTimeCycleStatus.COMPLETED -> return next == TransitTimeCycleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TransitTimeCycleStatus) {
        if (!canMoveTo(next)) {
            error("transit time cycle $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun effectiveVolume(): Double = round(expiresAt.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): TransitTimeCycle {
        return TransitTimeCycle(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            version = version,
            expiresAt = expiresAt,
            routeCode = routeCode,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a transit time cycle in status planned. */
fun newTransitTimeCycle(code: String, name: String): TransitTimeCycle {
    return TransitTimeCycle(
        code = code,
        name = name,
        fuelLiters = 0.0,
        version = 0,
        expiresAt = 0,
        routeCode = "",
        loadKg = 0.0,
        status = TransitTimeCycleStatus.PLANNED,
    )
}
