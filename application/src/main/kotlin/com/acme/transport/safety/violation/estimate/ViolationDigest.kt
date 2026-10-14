package com.acme.transport.safety.violation.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation digest is in its life cycle. */
enum class ViolationDigestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One violation digest, with what the planners need to act on it. */
data class ViolationDigest(
    var code: String,
    var name: String,
    var capacity: Int,
    var timezone: String,
    var threshold: Double,
    var region: String,
    var limit: Int,
    var windowDays: Int,
    var lane: String,
    var status: ViolationDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationDigestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationDigestStatus): Boolean {
        when (status) {
            ViolationDigestStatus.PLANNED -> return next == ViolationDigestStatus.SCHEDULED || next == ViolationDigestStatus.IN_PROGRESS
            ViolationDigestStatus.SCHEDULED -> {
                when (next) {
                    ViolationDigestStatus.IN_PROGRESS, ViolationDigestStatus.CANCELLED -> return true
                }
                return false
            }
            ViolationDigestStatus.IN_PROGRESS -> return next == ViolationDigestStatus.CANCELLED
            ViolationDigestStatus.COMPLETED -> return next == ViolationDigestStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ViolationDigestStatus) {
        if (!canMoveTo(next)) {
            error("violation digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedExposure(): Double = round((threshold - windowDays.toDouble()) * 100) / 100

    fun snapshot(): ViolationDigest {
        return ViolationDigest(
            code = code,
            name = name,
            capacity = capacity,
            timezone = timezone,
            threshold = threshold,
            region = region,
            limit = limit,
            windowDays = windowDays,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a violation digest in status planned. */
fun newViolationDigest(code: String, name: String): ViolationDigest {
    return ViolationDigest(
        code = code,
        name = name,
        capacity = 0,
        timezone = "",
        threshold = 0.0,
        region = "",
        limit = 0,
        windowDays = 0,
        lane = "",
        status = ViolationDigestStatus.PLANNED,
    )
}
