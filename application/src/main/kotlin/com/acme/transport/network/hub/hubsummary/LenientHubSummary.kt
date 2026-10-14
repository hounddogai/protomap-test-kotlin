package com.acme.transport.network.hub.hubsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient hub summary records of network's hub work, and the rules, stores, and calculations around them.

/** Where a lenient hub summary is in its life cycle. */
enum class LenientHubSummaryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lenient hub summary as the hub team keeps it. */
data class LenientHubSummary(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var threshold: Double,
    var ratio: Double,
    var status: LenientHubSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientHubSummaryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientHubSummaryStatus): Boolean {
        when (status) {
            LenientHubSummaryStatus.NEW -> return next == LenientHubSummaryStatus.DONE
            LenientHubSummaryStatus.QUEUED -> {
                when (next) {
                    LenientHubSummaryStatus.DONE -> return true
                }
                return false
            }
            LenientHubSummaryStatus.RUNNING -> return next == LenientHubSummaryStatus.DONE || next == LenientHubSummaryStatus.FAILED
            LenientHubSummaryStatus.DONE -> {
                when (next) {
                    LenientHubSummaryStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientHubSummaryStatus) {
        if (!canMoveTo(next)) {
            error("lenient hub summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun netVolume(): Double = round(ratio * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): LenientHubSummary {
        return LenientHubSummary(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            threshold = threshold,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient hub summary in status new. */
fun newLenientHubSummary(code: String, title: String): LenientHubSummary {
    return LenientHubSummary(
        code = code,
        title = title,
        durationMinutes = 0,
        threshold = 0.0,
        ratio = 0.0,
        status = LenientHubSummaryStatus.NEW,
    )
}
