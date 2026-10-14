package com.acme.transport.safety.coaching.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling coaching segment is in its life cycle. */
enum class RollingCoachingSegmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One rolling coaching segment, with what the planners need to act on it. */
data class RollingCoachingSegment(
    var code: String,
    var title: String,
    var routeCode: String,
    var locale: String,
    var contactEmail: String,
    var summary: String,
    var expiresAt: Int,
    var attempts: Int,
    var cost: Double,
    var timezone: String,
    var ratio: Double,
    var status: RollingCoachingSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCoachingSegmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCoachingSegmentStatus): Boolean {
        when (status) {
            RollingCoachingSegmentStatus.REQUESTED -> {
                when (next) {
                    RollingCoachingSegmentStatus.EXPIRED -> return true
                }
                return false
            }
            RollingCoachingSegmentStatus.GRANTED -> return next == RollingCoachingSegmentStatus.REVOKED || next == RollingCoachingSegmentStatus.EXPIRED
            RollingCoachingSegmentStatus.REVOKED -> return next == RollingCoachingSegmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RollingCoachingSegmentStatus) {
        if (!canMoveTo(next)) {
            error("rolling coaching segment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"rolling coaching segment"} $code"

    fun projectedExposure(): Double = round(attempts.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): RollingCoachingSegment {
        return RollingCoachingSegment(
            code = code,
            title = title,
            routeCode = routeCode,
            locale = locale,
            contactEmail = contactEmail,
            summary = summary,
            expiresAt = expiresAt,
            attempts = attempts,
            cost = cost,
            timezone = timezone,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rolling coaching segment in status requested. */
fun newRollingCoachingSegment(code: String, title: String): RollingCoachingSegment {
    return RollingCoachingSegment(
        code = code,
        title = title,
        routeCode = "",
        locale = "",
        contactEmail = "",
        summary = "",
        expiresAt = 0,
        attempts = 0,
        cost = 0.0,
        timezone = "",
        ratio = 0.0,
        status = RollingCoachingSegmentStatus.REQUESTED,
    )
}
