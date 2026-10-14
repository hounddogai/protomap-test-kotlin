package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling rest break milestone is in its life cycle. */
enum class RollingRestBreakMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A rolling rest break milestone. Its code is unique within a tenant. */
data class RollingRestBreakMilestone(
    var code: String,
    var title: String,
    var sequence: Int,
    var tags: MutableList<String>,
    var quantity: Int,
    var durationMinutes: Int,
    var timezone: String,
    var version: Int,
    var threshold: Double,
    var endsAt: Int,
    var weight: Double,
    var status: RollingRestBreakMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRestBreakMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRestBreakMilestoneStatus): Boolean {
        when (status) {
            RollingRestBreakMilestoneStatus.REQUESTED -> return next == RollingRestBreakMilestoneStatus.REVOKED || next == RollingRestBreakMilestoneStatus.EXPIRED
            RollingRestBreakMilestoneStatus.GRANTED -> return next == RollingRestBreakMilestoneStatus.REVOKED || next == RollingRestBreakMilestoneStatus.EXPIRED
            RollingRestBreakMilestoneStatus.REVOKED -> {
                when (next) {
                    RollingRestBreakMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingRestBreakMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("rolling rest break milestone $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedExposure(): Double = round((durationMinutes.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): RollingRestBreakMilestone {
        return RollingRestBreakMilestone(
            code = code,
            title = title,
            sequence = sequence,
            tags = tags,
            quantity = quantity,
            durationMinutes = durationMinutes,
            timezone = timezone,
            version = version,
            threshold = threshold,
            endsAt = endsAt,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling rest break milestone in status requested. */
fun newRollingRestBreakMilestone(code: String, title: String): RollingRestBreakMilestone {
    return RollingRestBreakMilestone(
        code = code,
        title = title,
        sequence = 0,
        tags = mutableListOf<String>(),
        quantity = 0,
        durationMinutes = 0,
        timezone = "",
        version = 0,
        threshold = 0.0,
        endsAt = 0,
        weight = 0.0,
        status = RollingRestBreakMilestoneStatus.REQUESTED,
    )
}
