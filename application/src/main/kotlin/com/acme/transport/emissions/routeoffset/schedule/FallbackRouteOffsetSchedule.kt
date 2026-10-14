package com.acme.transport.emissions.routeoffset.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback route offset schedule records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a fallback route offset schedule is in its life cycle. */
enum class FallbackRouteOffsetScheduleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fallback route offset schedule. Its code is unique within a tenant. */
data class FallbackRouteOffsetSchedule(
    var code: String,
    var name: String,
    var loadKg: Double,
    var reviewer: String,
    var discount: Double,
    var status: FallbackRouteOffsetScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRouteOffsetScheduleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRouteOffsetScheduleStatus): Boolean {
        when (status) {
            FallbackRouteOffsetScheduleStatus.OPEN -> {
                when (next) {
                    FallbackRouteOffsetScheduleStatus.IN_REVIEW, FallbackRouteOffsetScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackRouteOffsetScheduleStatus.IN_REVIEW -> {
                when (next) {
                    FallbackRouteOffsetScheduleStatus.RESOLVED, FallbackRouteOffsetScheduleStatus.REOPENED -> return true
                }
                return false
            }
            FallbackRouteOffsetScheduleStatus.RESOLVED -> {
                when (next) {
                    FallbackRouteOffsetScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackRouteOffsetScheduleStatus.REOPENED -> {
                when (next) {
                    FallbackRouteOffsetScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackRouteOffsetScheduleStatus) {
        if (!canMoveTo(next)) {
            error("fallback route offset schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedValue(): Double = round((loadKg + discount) * 100) / 100

    fun snapshot(): FallbackRouteOffsetSchedule {
        return FallbackRouteOffsetSchedule(
            code = code,
            name = name,
            loadKg = loadKg,
            reviewer = reviewer,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback route offset schedule in status open. */
fun newFallbackRouteOffsetSchedule(code: String, name: String): FallbackRouteOffsetSchedule {
    return FallbackRouteOffsetSchedule(
        code = code,
        name = name,
        loadKg = 0.0,
        reviewer = "",
        discount = 0.0,
        status = FallbackRouteOffsetScheduleStatus.OPEN,
    )
}
