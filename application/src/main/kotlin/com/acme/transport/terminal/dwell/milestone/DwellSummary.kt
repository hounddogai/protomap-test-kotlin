package com.acme.transport.terminal.dwell.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell summary is in its life cycle. */
enum class DwellSummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A dwell summary as the dwell team keeps it. */
data class DwellSummary(
    var code: String,
    var title: String,
    var threshold: Double,
    var label: String,
    var trailerId: String,
    var expiresAt: Int,
    var margin: Double,
    var attempts: Int,
    var startsAt: Int,
    var rate: Double,
    var status: DwellSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellSummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellSummaryStatus): Boolean {
        when (status) {
            DwellSummaryStatus.CREATED -> return next == DwellSummaryStatus.PUBLISHED || next == DwellSummaryStatus.RETIRED
            DwellSummaryStatus.VALIDATED -> return next == DwellSummaryStatus.PUBLISHED
            DwellSummaryStatus.PUBLISHED -> {
                when (next) {
                    DwellSummaryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellSummaryStatus) {
        if (!canMoveTo(next)) {
            error("dwell summary $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($title)"

    fun adjustedLoad(): Double = round((threshold - attempts.toDouble()) * 100) / 100

    fun snapshot(): DwellSummary {
        return DwellSummary(
            code = code,
            title = title,
            threshold = threshold,
            label = label,
            trailerId = trailerId,
            expiresAt = expiresAt,
            margin = margin,
            attempts = attempts,
            startsAt = startsAt,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a dwell summary in status created. */
fun newDwellSummary(code: String, title: String): DwellSummary {
    return DwellSummary(
        code = code,
        title = title,
        threshold = 0.0,
        label = "",
        trailerId = "",
        expiresAt = 0,
        margin = 0.0,
        attempts = 0,
        startsAt = 0,
        rate = 0.0,
        status = DwellSummaryStatus.CREATED,
    )
}
