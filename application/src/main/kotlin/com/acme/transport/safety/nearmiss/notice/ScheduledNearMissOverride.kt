package com.acme.transport.safety.nearmiss.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled near miss override is in its life cycle. */
enum class ScheduledNearMissOverrideStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a scheduled near miss override and its current status. */
data class ScheduledNearMissOverride(
    var code: String,
    var name: String,
    var category: String,
    var weight: Double,
    var fuelLiters: Double,
    var unit: String,
    var status: ScheduledNearMissOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledNearMissOverrideStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledNearMissOverrideStatus): Boolean {
        when (status) {
            ScheduledNearMissOverrideStatus.IDLE -> return next == ScheduledNearMissOverrideStatus.WARMING || next == ScheduledNearMissOverrideStatus.STOPPED
            ScheduledNearMissOverrideStatus.WARMING -> {
                when (next) {
                    ScheduledNearMissOverrideStatus.DRAINING, ScheduledNearMissOverrideStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledNearMissOverrideStatus.READY -> {
                when (next) {
                    ScheduledNearMissOverrideStatus.DRAINING -> return true
                }
                return false
            }
            ScheduledNearMissOverrideStatus.DRAINING -> return next == ScheduledNearMissOverrideStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ScheduledNearMissOverrideStatus) {
        if (!canMoveTo(next)) {
            error("scheduled near miss override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveExposure(): Double = round((weight - fuelLiters) * 100) / 100

    fun snapshot(): ScheduledNearMissOverride {
        return ScheduledNearMissOverride(
            code = code,
            name = name,
            category = category,
            weight = weight,
            fuelLiters = fuelLiters,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a scheduled near miss override in status idle. */
fun newScheduledNearMissOverride(code: String, name: String): ScheduledNearMissOverride {
    return ScheduledNearMissOverride(
        code = code,
        name = name,
        category = "",
        weight = 0.0,
        fuelLiters = 0.0,
        unit = "",
        status = ScheduledNearMissOverrideStatus.IDLE,
    )
}
