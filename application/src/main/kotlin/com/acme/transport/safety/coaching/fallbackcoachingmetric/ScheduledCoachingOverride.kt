package com.acme.transport.safety.coaching.fallbackcoachingmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled coaching override is in its life cycle. */
enum class ScheduledCoachingOverrideStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A scheduled coaching override. Its code is unique within a tenant. */
data class ScheduledCoachingOverride(
    var code: String,
    var name: String,
    var carrier: String,
    var routeCode: String,
    var active: Boolean,
    var stopCount: Int,
    var windowDays: Int,
    var status: ScheduledCoachingOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledCoachingOverrideStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledCoachingOverrideStatus): Boolean {
        when (status) {
            ScheduledCoachingOverrideStatus.PROPOSED -> return next == ScheduledCoachingOverrideStatus.REJECTED
            ScheduledCoachingOverrideStatus.APPROVED -> {
                when (next) {
                    ScheduledCoachingOverrideStatus.REJECTED -> return true
                }
                return false
            }
            ScheduledCoachingOverrideStatus.REJECTED -> {
                when (next) {
                    ScheduledCoachingOverrideStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledCoachingOverrideStatus) {
        if (!canMoveTo(next)) {
            error("scheduled coaching override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedExposure(): Double = round((stopCount.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): ScheduledCoachingOverride {
        return ScheduledCoachingOverride(
            code = code,
            name = name,
            carrier = carrier,
            routeCode = routeCode,
            active = active,
            stopCount = stopCount,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled coaching override in status proposed. */
fun newScheduledCoachingOverride(code: String, name: String): ScheduledCoachingOverride {
    return ScheduledCoachingOverride(
        code = code,
        name = name,
        carrier = "",
        routeCode = "",
        active = false,
        stopCount = 0,
        windowDays = 0,
        status = ScheduledCoachingOverrideStatus.PROPOSED,
    )
}
