package com.acme.transport.safety.scorecard.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard segment is in its life cycle. */
enum class ScorecardSegmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A scorecard segment as the scorecard team keeps it. */
data class ScorecardSegment(
    var code: String,
    var label: String,
    var carrier: String,
    var priority: Int,
    var margin: Double,
    var etaMinutes: Int,
    var lane: String,
    var startsAt: Int,
    var threshold: Double,
    var tags: MutableList<String>,
    var currency: String,
    var status: ScorecardSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardSegmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardSegmentStatus): Boolean {
        when (status) {
            ScorecardSegmentStatus.REQUESTED -> return next == ScorecardSegmentStatus.GRANTED || next == ScorecardSegmentStatus.REVOKED
            ScorecardSegmentStatus.GRANTED -> return next == ScorecardSegmentStatus.REVOKED || next == ScorecardSegmentStatus.EXPIRED
            ScorecardSegmentStatus.REVOKED -> return next == ScorecardSegmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ScorecardSegmentStatus) {
        if (!canMoveTo(next)) {
            error("scorecard segment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"scorecard segment"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedVolume(): Double = round((margin - startsAt.toDouble()) * 100) / 100

    fun snapshot(): ScorecardSegment {
        return ScorecardSegment(
            code = code,
            label = label,
            carrier = carrier,
            priority = priority,
            margin = margin,
            etaMinutes = etaMinutes,
            lane = lane,
            startsAt = startsAt,
            threshold = threshold,
            tags = tags,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a scorecard segment in status requested. */
fun newScorecardSegment(code: String, label: String): ScorecardSegment {
    return ScorecardSegment(
        code = code,
        label = label,
        carrier = "",
        priority = 0,
        margin = 0.0,
        etaMinutes = 0,
        lane = "",
        startsAt = 0,
        threshold = 0.0,
        tags = mutableListOf<String>(),
        currency = "",
        status = ScorecardSegmentStatus.REQUESTED,
    )
}
