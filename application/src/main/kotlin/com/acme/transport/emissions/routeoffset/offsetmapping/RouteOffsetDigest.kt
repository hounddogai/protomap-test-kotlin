package com.acme.transport.emissions.routeoffset.offsetmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset digest is in its life cycle. */
enum class RouteOffsetDigestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One route offset digest, with what the planners need to act on it. */
data class RouteOffsetDigest(
    var code: String,
    var label: String,
    var stopCount: Int,
    var rate: Double,
    var timezone: String,
    var priority: Int,
    var contactEmail: String,
    var checksum: String,
    var status: RouteOffsetDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetDigestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetDigestStatus): Boolean {
        when (status) {
            RouteOffsetDigestStatus.OPEN -> return next == RouteOffsetDigestStatus.RESOLVED
            RouteOffsetDigestStatus.IN_REVIEW -> {
                when (next) {
                    RouteOffsetDigestStatus.RESOLVED -> return true
                }
                return false
            }
            RouteOffsetDigestStatus.RESOLVED -> return next == RouteOffsetDigestStatus.REOPENED || next == RouteOffsetDigestStatus.ARCHIVED
            RouteOffsetDigestStatus.REOPENED -> {
                when (next) {
                    RouteOffsetDigestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetDigestStatus) {
        if (!canMoveTo(next)) {
            error("route offset digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedVolume(): Double = round(priority.toDouble() * rate * 100) / 100

    fun snapshot(): RouteOffsetDigest {
        return RouteOffsetDigest(
            code = code,
            label = label,
            stopCount = stopCount,
            rate = rate,
            timezone = timezone,
            priority = priority,
            contactEmail = contactEmail,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a route offset digest in status open. */
fun newRouteOffsetDigest(code: String, label: String): RouteOffsetDigest {
    return RouteOffsetDigest(
        code = code,
        label = label,
        stopCount = 0,
        rate = 0.0,
        timezone = "",
        priority = 0,
        contactEmail = "",
        checksum = "",
        status = RouteOffsetDigestStatus.OPEN,
    )
}
