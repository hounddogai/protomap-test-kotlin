package com.acme.transport.safety.scorecard.scorecardgroup

import com.acme.transport.kit.text.normalize

/** Where a adaptive scorecard segment is in its life cycle. */
enum class AdaptiveScorecardSegmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A adaptive scorecard segment as the scorecard team keeps it. */
data class AdaptiveScorecardSegment(
    var code: String,
    var title: String,
    var checksum: String,
    var pinned: Boolean,
    var shiftCode: String,
    var externalRef: String,
    var score: Double,
    var status: AdaptiveScorecardSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveScorecardSegmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveScorecardSegmentStatus): Boolean {
        when (status) {
            AdaptiveScorecardSegmentStatus.DRAFT -> {
                when (next) {
                    AdaptiveScorecardSegmentStatus.SUSPENDED, AdaptiveScorecardSegmentStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveScorecardSegmentStatus.PENDING -> return next == AdaptiveScorecardSegmentStatus.SUSPENDED || next == AdaptiveScorecardSegmentStatus.CLOSED
            AdaptiveScorecardSegmentStatus.ACTIVE -> return next == AdaptiveScorecardSegmentStatus.SUSPENDED || next == AdaptiveScorecardSegmentStatus.CLOSED
            AdaptiveScorecardSegmentStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveScorecardSegmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveScorecardSegmentStatus) {
        if (!canMoveTo(next)) {
            error("adaptive scorecard segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = score

    fun snapshot(): AdaptiveScorecardSegment {
        return AdaptiveScorecardSegment(
            code = code,
            title = title,
            checksum = checksum,
            pinned = pinned,
            shiftCode = shiftCode,
            externalRef = externalRef,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive scorecard segment in status draft. */
fun newAdaptiveScorecardSegment(code: String, title: String): AdaptiveScorecardSegment {
    return AdaptiveScorecardSegment(
        code = code,
        title = title,
        checksum = "",
        pinned = false,
        shiftCode = "",
        externalRef = "",
        score = 0.0,
        status = AdaptiveScorecardSegmentStatus.DRAFT,
    )
}
