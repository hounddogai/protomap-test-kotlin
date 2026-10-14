package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled near miss segment is in its life cycle. */
enum class ScheduledNearMissSegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scheduled near miss segment as the near miss team keeps it. */
data class ScheduledNearMissSegment(
    var code: String,
    var name: String,
    var priority: Int,
    var routeCode: String,
    var phone: String,
    var version: Int,
    var status: ScheduledNearMissSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledNearMissSegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledNearMissSegmentStatus): Boolean {
        when (status) {
            ScheduledNearMissSegmentStatus.OPEN -> return next == ScheduledNearMissSegmentStatus.ARCHIVED
            ScheduledNearMissSegmentStatus.IN_REVIEW -> {
                when (next) {
                    ScheduledNearMissSegmentStatus.REOPENED -> return true
                }
                return false
            }
            ScheduledNearMissSegmentStatus.RESOLVED -> {
                when (next) {
                    ScheduledNearMissSegmentStatus.REOPENED -> return true
                }
                return false
            }
            ScheduledNearMissSegmentStatus.REOPENED -> {
                when (next) {
                    ScheduledNearMissSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledNearMissSegmentStatus) {
        if (!canMoveTo(next)) {
            error("scheduled near miss segment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun effectiveValue(): Double = round((priority.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): ScheduledNearMissSegment {
        return ScheduledNearMissSegment(
            code = code,
            name = name,
            priority = priority,
            routeCode = routeCode,
            phone = phone,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled near miss segment in status open. */
fun newScheduledNearMissSegment(code: String, name: String): ScheduledNearMissSegment {
    return ScheduledNearMissSegment(
        code = code,
        name = name,
        priority = 0,
        routeCode = "",
        phone = "",
        version = 0,
        status = ScheduledNearMissSegmentStatus.OPEN,
    )
}
