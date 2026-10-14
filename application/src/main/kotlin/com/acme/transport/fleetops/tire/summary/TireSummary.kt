package com.acme.transport.fleetops.tire.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tire summary records of fleetops's tire work, and the rules, stores, and calculations around them.

/** Where a tire summary is in its life cycle. */
enum class TireSummaryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One tire summary, with what the planners need to act on it. */
data class TireSummary(
    var code: String,
    var title: String,
    var channel: String,
    var sequence: Int,
    var revision: Int,
    var distanceKm: Double,
    var status: TireSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireSummaryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireSummaryStatus): Boolean {
        when (status) {
            TireSummaryStatus.OPEN -> {
                when (next) {
                    TireSummaryStatus.RESOLVED, TireSummaryStatus.ARCHIVED -> return true
                }
                return false
            }
            TireSummaryStatus.IN_REVIEW -> return next == TireSummaryStatus.REOPENED || next == TireSummaryStatus.ARCHIVED
            TireSummaryStatus.RESOLVED -> {
                when (next) {
                    TireSummaryStatus.ARCHIVED -> return true
                }
                return false
            }
            TireSummaryStatus.REOPENED -> {
                when (next) {
                    TireSummaryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireSummaryStatus) {
        if (!canMoveTo(next)) {
            error("tire summary $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"tire summary"} $code"

    fun adjustedValue(): Double = round((revision.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): TireSummary {
        return TireSummary(
            code = code,
            title = title,
            channel = channel,
            sequence = sequence,
            revision = revision,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a tire summary in status open. */
fun newTireSummary(code: String, title: String): TireSummary {
    return TireSummary(
        code = code,
        title = title,
        channel = "",
        sequence = 0,
        revision = 0,
        distanceKm = 0.0,
        status = TireSummaryStatus.OPEN,
    )
}
