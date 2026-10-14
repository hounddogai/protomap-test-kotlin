package com.acme.transport.crewing.roster.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback roster summary is in its life cycle. */
enum class FallbackRosterSummaryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fallback roster summary. Its code is unique within a tenant. */
data class FallbackRosterSummary(
    var code: String,
    var label: String,
    var lane: String,
    var revision: Int,
    var sequence: Int,
    var weight: Double,
    var priority: Int,
    var rate: Double,
    var region: String,
    var threshold: Double,
    var visible: Boolean,
    var status: FallbackRosterSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRosterSummaryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRosterSummaryStatus): Boolean {
        when (status) {
            FallbackRosterSummaryStatus.OPEN -> return next == FallbackRosterSummaryStatus.IN_REVIEW || next == FallbackRosterSummaryStatus.ARCHIVED
            FallbackRosterSummaryStatus.IN_REVIEW -> return next == FallbackRosterSummaryStatus.REOPENED
            FallbackRosterSummaryStatus.RESOLVED -> return next == FallbackRosterSummaryStatus.REOPENED
            FallbackRosterSummaryStatus.REOPENED -> {
                when (next) {
                    FallbackRosterSummaryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackRosterSummaryStatus) {
        if (!canMoveTo(next)) {
            error("fallback roster summary $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"fallback roster summary"} $code"

    fun adjustedTotal(): Double = round(priority.toDouble() * rate * 100) / 100

    fun snapshot(): FallbackRosterSummary {
        return FallbackRosterSummary(
            code = code,
            label = label,
            lane = lane,
            revision = revision,
            sequence = sequence,
            weight = weight,
            priority = priority,
            rate = rate,
            region = region,
            threshold = threshold,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a fallback roster summary in status open. */
fun newFallbackRosterSummary(code: String, label: String): FallbackRosterSummary {
    return FallbackRosterSummary(
        code = code,
        label = label,
        lane = "",
        revision = 0,
        sequence = 0,
        weight = 0.0,
        priority = 0,
        rate = 0.0,
        region = "",
        threshold = 0.0,
        visible = false,
        status = FallbackRosterSummaryStatus.OPEN,
    )
}
