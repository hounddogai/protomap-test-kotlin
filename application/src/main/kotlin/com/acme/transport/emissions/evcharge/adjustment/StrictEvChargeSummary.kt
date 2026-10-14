package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict ev charge summary is in its life cycle. */
enum class StrictEvChargeSummaryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A strict ev charge summary as the ev charge team keeps it. */
data class StrictEvChargeSummary(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var score: Double,
    var rate: Double,
    var segment: String,
    var status: StrictEvChargeSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictEvChargeSummaryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictEvChargeSummaryStatus): Boolean {
        when (status) {
            StrictEvChargeSummaryStatus.IDLE -> return next == StrictEvChargeSummaryStatus.STOPPED
            StrictEvChargeSummaryStatus.WARMING -> {
                when (next) {
                    StrictEvChargeSummaryStatus.READY, StrictEvChargeSummaryStatus.STOPPED -> return true
                }
                return false
            }
            StrictEvChargeSummaryStatus.READY -> {
                when (next) {
                    StrictEvChargeSummaryStatus.DRAINING, StrictEvChargeSummaryStatus.STOPPED -> return true
                }
                return false
            }
            StrictEvChargeSummaryStatus.DRAINING -> return next == StrictEvChargeSummaryStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: StrictEvChargeSummaryStatus) {
        if (!canMoveTo(next)) {
            error("strict ev charge summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"strict ev charge summary"} $code"

    fun projectedVolume(): Double = round(rate * score * 100) / 100

    fun snapshot(): StrictEvChargeSummary {
        return StrictEvChargeSummary(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            score = score,
            rate = rate,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a strict ev charge summary in status idle. */
fun newStrictEvChargeSummary(code: String, label: String): StrictEvChargeSummary {
    return StrictEvChargeSummary(
        code = code,
        label = label,
        durationMinutes = 0,
        score = 0.0,
        rate = 0.0,
        segment = "",
        status = StrictEvChargeSummaryStatus.IDLE,
    )
}
