package com.acme.transport.safety.scorecard.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled scorecard mapping is in its life cycle. */
enum class ScheduledScorecardMappingStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One scheduled scorecard mapping, with what the planners need to act on it. */
data class ScheduledScorecardMapping(
    var code: String,
    var name: String,
    var timezone: String,
    var endsAt: Int,
    var stopCount: Int,
    var pinned: Boolean,
    var status: ScheduledScorecardMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledScorecardMappingStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledScorecardMappingStatus): Boolean {
        when (status) {
            ScheduledScorecardMappingStatus.PROPOSED -> {
                when (next) {
                    ScheduledScorecardMappingStatus.REJECTED, ScheduledScorecardMappingStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScheduledScorecardMappingStatus.APPROVED -> return next == ScheduledScorecardMappingStatus.WITHDRAWN
            ScheduledScorecardMappingStatus.REJECTED -> {
                when (next) {
                    ScheduledScorecardMappingStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledScorecardMappingStatus) {
        if (!canMoveTo(next)) {
            error("scheduled scorecard mapping $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedLoad(): Double = round((stopCount.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledScorecardMapping {
        return ScheduledScorecardMapping(
            code = code,
            name = name,
            timezone = timezone,
            endsAt = endsAt,
            stopCount = stopCount,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled scorecard mapping in status proposed. */
fun newScheduledScorecardMapping(code: String, name: String): ScheduledScorecardMapping {
    return ScheduledScorecardMapping(
        code = code,
        name = name,
        timezone = "",
        endsAt = 0,
        stopCount = 0,
        pinned = false,
        status = ScheduledScorecardMappingStatus.PROPOSED,
    )
}
