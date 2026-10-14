package com.acme.transport.safety.coaching.coachingdigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling coaching digest records of safety's coaching work, and the rules, stores, and calculations
// around them.

/** Where a rolling coaching digest is in its life cycle. */
enum class RollingCoachingDigestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a rolling coaching digest and its current status. */
data class RollingCoachingDigest(
    var code: String,
    var label: String,
    var createdAt: Int,
    var driverId: String,
    var locale: String,
    var budget: Double,
    var tags: MutableList<String>,
    var status: RollingCoachingDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCoachingDigestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCoachingDigestStatus): Boolean {
        when (status) {
            RollingCoachingDigestStatus.IDLE -> return next == RollingCoachingDigestStatus.DRAINING
            RollingCoachingDigestStatus.WARMING -> {
                when (next) {
                    RollingCoachingDigestStatus.DRAINING, RollingCoachingDigestStatus.STOPPED -> return true
                }
                return false
            }
            RollingCoachingDigestStatus.READY -> return next == RollingCoachingDigestStatus.STOPPED
            RollingCoachingDigestStatus.DRAINING -> return next == RollingCoachingDigestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RollingCoachingDigestStatus) {
        if (!canMoveTo(next)) {
            error("rolling coaching digest $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"rolling coaching digest"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedValue(): Double = round(budget * createdAt.toDouble() * 100) / 100

    fun snapshot(): RollingCoachingDigest {
        return RollingCoachingDigest(
            code = code,
            label = label,
            createdAt = createdAt,
            driverId = driverId,
            locale = locale,
            budget = budget,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rolling coaching digest in status idle. */
fun newRollingCoachingDigest(code: String, label: String): RollingCoachingDigest {
    return RollingCoachingDigest(
        code = code,
        label = label,
        createdAt = 0,
        driverId = "",
        locale = "",
        budget = 0.0,
        tags = mutableListOf<String>(),
        status = RollingCoachingDigestStatus.IDLE,
    )
}
