package com.acme.transport.safety.coaching.coachingtier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching override is in its life cycle. */
enum class CoachingOverrideStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One coaching override, with what the planners need to act on it. */
data class CoachingOverride(
    var code: String,
    var title: String,
    var category: String,
    var routeCode: String,
    var createdAt: Int,
    var etaMinutes: Int,
    var pallets: Int,
    var startsAt: Int,
    var locale: String,
    var status: CoachingOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingOverrideStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingOverrideStatus): Boolean {
        when (status) {
            CoachingOverrideStatus.IDLE -> return next == CoachingOverrideStatus.STOPPED
            CoachingOverrideStatus.WARMING -> {
                when (next) {
                    CoachingOverrideStatus.DRAINING -> return true
                }
                return false
            }
            CoachingOverrideStatus.READY -> return next == CoachingOverrideStatus.STOPPED
            CoachingOverrideStatus.DRAINING -> return next == CoachingOverrideStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CoachingOverrideStatus) {
        if (!canMoveTo(next)) {
            error("coaching override $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"coaching override"} $code"

    fun weightedVolume(): Double = round((startsAt.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): CoachingOverride {
        return CoachingOverride(
            code = code,
            title = title,
            category = category,
            routeCode = routeCode,
            createdAt = createdAt,
            etaMinutes = etaMinutes,
            pallets = pallets,
            startsAt = startsAt,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a coaching override in status idle. */
fun newCoachingOverride(code: String, title: String): CoachingOverride {
    return CoachingOverride(
        code = code,
        title = title,
        category = "",
        routeCode = "",
        createdAt = 0,
        etaMinutes = 0,
        pallets = 0,
        startsAt = 0,
        locale = "",
        status = CoachingOverrideStatus.IDLE,
    )
}
