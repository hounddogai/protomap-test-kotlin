package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.kit.text.normalize

/** Where a cross dock summary is in its life cycle. */
enum class CrossDockSummaryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A cross dock summary as the cross dock team keeps it. */
data class CrossDockSummary(
    var code: String,
    var label: String,
    var stopCount: Int,
    var currency: String,
    var archived: Boolean,
    var segment: String,
    var pinned: Boolean,
    var status: CrossDockSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockSummaryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockSummaryStatus): Boolean {
        when (status) {
            CrossDockSummaryStatus.PLANNED -> {
                when (next) {
                    CrossDockSummaryStatus.COMPLETED -> return true
                }
                return false
            }
            CrossDockSummaryStatus.SCHEDULED -> {
                when (next) {
                    CrossDockSummaryStatus.CANCELLED -> return true
                }
                return false
            }
            CrossDockSummaryStatus.IN_PROGRESS -> {
                when (next) {
                    CrossDockSummaryStatus.COMPLETED -> return true
                }
                return false
            }
            CrossDockSummaryStatus.COMPLETED -> {
                when (next) {
                    CrossDockSummaryStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CrossDockSummaryStatus) {
        if (!canMoveTo(next)) {
            error("cross dock summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"cross dock summary"} $code"

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): CrossDockSummary {
        return CrossDockSummary(
            code = code,
            label = label,
            stopCount = stopCount,
            currency = currency,
            archived = archived,
            segment = segment,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a cross dock summary in status planned. */
fun newCrossDockSummary(code: String, label: String): CrossDockSummary {
    return CrossDockSummary(
        code = code,
        label = label,
        stopCount = 0,
        currency = "",
        archived = false,
        segment = "",
        pinned = false,
        status = CrossDockSummaryStatus.PLANNED,
    )
}
