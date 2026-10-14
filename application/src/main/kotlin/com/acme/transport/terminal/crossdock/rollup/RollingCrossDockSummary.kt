package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.kit.text.normalize

/** Where a rolling cross dock summary is in its life cycle. */
enum class RollingCrossDockSummaryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a rolling cross dock summary and its current status. */
data class RollingCrossDockSummary(
    var code: String,
    var title: String,
    var summary: String,
    var visible: Boolean,
    var externalRef: String,
    var weight: Double,
    var status: RollingCrossDockSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCrossDockSummaryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCrossDockSummaryStatus): Boolean {
        when (status) {
            RollingCrossDockSummaryStatus.OPEN -> {
                when (next) {
                    RollingCrossDockSummaryStatus.REOPENED -> return true
                }
                return false
            }
            RollingCrossDockSummaryStatus.IN_REVIEW -> return next == RollingCrossDockSummaryStatus.REOPENED
            RollingCrossDockSummaryStatus.RESOLVED -> {
                when (next) {
                    RollingCrossDockSummaryStatus.REOPENED -> return true
                }
                return false
            }
            RollingCrossDockSummaryStatus.REOPENED -> {
                when (next) {
                    RollingCrossDockSummaryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingCrossDockSummaryStatus) {
        if (!canMoveTo(next)) {
            error("rolling cross dock summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling cross dock summary"} $code"

    fun measuredValue(): Double = weight

    fun snapshot(): RollingCrossDockSummary {
        return RollingCrossDockSummary(
            code = code,
            title = title,
            summary = summary,
            visible = visible,
            externalRef = externalRef,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling cross dock summary in status open. */
fun newRollingCrossDockSummary(code: String, title: String): RollingCrossDockSummary {
    return RollingCrossDockSummary(
        code = code,
        title = title,
        summary = "",
        visible = false,
        externalRef = "",
        weight = 0.0,
        status = RollingCrossDockSummaryStatus.OPEN,
    )
}
