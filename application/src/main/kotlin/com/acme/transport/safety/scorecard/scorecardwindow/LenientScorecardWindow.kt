package com.acme.transport.safety.scorecard.scorecardwindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient scorecard window records of safety's scorecard work, and the rules, stores, and calculations
// around them.

/** Where a lenient scorecard window is in its life cycle. */
enum class LenientScorecardWindowStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a lenient scorecard window and its current status. */
data class LenientScorecardWindow(
    var code: String,
    var title: String,
    var description: String,
    var expiresAt: Int,
    var limit: Int,
    var segment: String,
    var checksum: String,
    var rate: Double,
    var weight: Double,
    var endsAt: Int,
    var attempts: Int,
    var status: LenientScorecardWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientScorecardWindowStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientScorecardWindowStatus): Boolean {
        when (status) {
            LenientScorecardWindowStatus.OPEN -> return next == LenientScorecardWindowStatus.RESOLVED || next == LenientScorecardWindowStatus.REOPENED
            LenientScorecardWindowStatus.IN_REVIEW -> return next == LenientScorecardWindowStatus.REOPENED
            LenientScorecardWindowStatus.RESOLVED -> return next == LenientScorecardWindowStatus.ARCHIVED
            LenientScorecardWindowStatus.REOPENED -> return next == LenientScorecardWindowStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LenientScorecardWindowStatus) {
        if (!canMoveTo(next)) {
            error("lenient scorecard window $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedVolume(): Double = round((rate - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): LenientScorecardWindow {
        return LenientScorecardWindow(
            code = code,
            title = title,
            description = description,
            expiresAt = expiresAt,
            limit = limit,
            segment = segment,
            checksum = checksum,
            rate = rate,
            weight = weight,
            endsAt = endsAt,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a lenient scorecard window in status open. */
fun newLenientScorecardWindow(code: String, title: String): LenientScorecardWindow {
    return LenientScorecardWindow(
        code = code,
        title = title,
        description = "",
        expiresAt = 0,
        limit = 0,
        segment = "",
        checksum = "",
        rate = 0.0,
        weight = 0.0,
        endsAt = 0,
        attempts = 0,
        status = LenientScorecardWindowStatus.OPEN,
    )
}
