package com.acme.transport.linehaul.trailer.trailerrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy trailer milestone is in its life cycle. */
enum class LegacyTrailerMilestoneStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A legacy trailer milestone. Its code is unique within a tenant. */
data class LegacyTrailerMilestone(
    var code: String,
    var title: String,
    var rate: Double,
    var score: Double,
    var ownerId: String,
    var timezone: String,
    var status: LegacyTrailerMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTrailerMilestoneStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTrailerMilestoneStatus): Boolean {
        when (status) {
            LegacyTrailerMilestoneStatus.PROPOSED -> {
                when (next) {
                    LegacyTrailerMilestoneStatus.APPROVED -> return true
                }
                return false
            }
            LegacyTrailerMilestoneStatus.APPROVED -> return next == LegacyTrailerMilestoneStatus.WITHDRAWN
            LegacyTrailerMilestoneStatus.REJECTED -> return next == LegacyTrailerMilestoneStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacyTrailerMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("legacy trailer milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy trailer milestone"} $code"

    fun effectiveValue(): Double = round((score - rate) * 100) / 100

    fun snapshot(): LegacyTrailerMilestone {
        return LegacyTrailerMilestone(
            code = code,
            title = title,
            rate = rate,
            score = score,
            ownerId = ownerId,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy trailer milestone in status proposed. */
fun newLegacyTrailerMilestone(code: String, title: String): LegacyTrailerMilestone {
    return LegacyTrailerMilestone(
        code = code,
        title = title,
        rate = 0.0,
        score = 0.0,
        ownerId = "",
        timezone = "",
        status = LegacyTrailerMilestoneStatus.PROPOSED,
    )
}
