package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.kit.text.normalize

/** Where a scheduled cross dock summary is in its life cycle. */
enum class ScheduledCrossDockSummaryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One scheduled cross dock summary, with what the planners need to act on it. */
data class ScheduledCrossDockSummary(
    var code: String,
    var label: String,
    var minimum: Double,
    var region: String,
    var locale: String,
    var summary: String,
    var status: ScheduledCrossDockSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledCrossDockSummaryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledCrossDockSummaryStatus): Boolean {
        when (status) {
            ScheduledCrossDockSummaryStatus.REQUESTED -> {
                when (next) {
                    ScheduledCrossDockSummaryStatus.EXPIRED -> return true
                }
                return false
            }
            ScheduledCrossDockSummaryStatus.GRANTED -> {
                when (next) {
                    ScheduledCrossDockSummaryStatus.REVOKED -> return true
                }
                return false
            }
            ScheduledCrossDockSummaryStatus.REVOKED -> return next == ScheduledCrossDockSummaryStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ScheduledCrossDockSummaryStatus) {
        if (!canMoveTo(next)) {
            error("scheduled cross dock summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = minimum

    fun snapshot(): ScheduledCrossDockSummary {
        return ScheduledCrossDockSummary(
            code = code,
            label = label,
            minimum = minimum,
            region = region,
            locale = locale,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled cross dock summary in status requested. */
fun newScheduledCrossDockSummary(code: String, label: String): ScheduledCrossDockSummary {
    return ScheduledCrossDockSummary(
        code = code,
        label = label,
        minimum = 0.0,
        region = "",
        locale = "",
        summary = "",
        status = ScheduledCrossDockSummaryStatus.REQUESTED,
    )
}
