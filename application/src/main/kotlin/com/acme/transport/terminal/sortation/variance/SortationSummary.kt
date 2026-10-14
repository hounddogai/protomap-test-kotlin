package com.acme.transport.terminal.sortation.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation summary is in its life cycle. */
enum class SortationSummaryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A sortation summary as the sortation team keeps it. */
data class SortationSummary(
    var code: String,
    var title: String,
    var visible: Boolean,
    var label: String,
    var externalRef: String,
    var depot: String,
    var createdAt: Int,
    var currency: String,
    var graceDays: Int,
    var status: SortationSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationSummaryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationSummaryStatus): Boolean {
        when (status) {
            SortationSummaryStatus.IDLE -> return next == SortationSummaryStatus.WARMING
            SortationSummaryStatus.WARMING -> return next == SortationSummaryStatus.DRAINING
            SortationSummaryStatus.READY -> {
                when (next) {
                    SortationSummaryStatus.DRAINING, SortationSummaryStatus.STOPPED -> return true
                }
                return false
            }
            SortationSummaryStatus.DRAINING -> return next == SortationSummaryStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: SortationSummaryStatus) {
        if (!canMoveTo(next)) {
            error("sortation summary $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($title)"

    fun adjustedLoad(): Double = round((graceDays.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): SortationSummary {
        return SortationSummary(
            code = code,
            title = title,
            visible = visible,
            label = label,
            externalRef = externalRef,
            depot = depot,
            createdAt = createdAt,
            currency = currency,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a sortation summary in status idle. */
fun newSortationSummary(code: String, title: String): SortationSummary {
    return SortationSummary(
        code = code,
        title = title,
        visible = false,
        label = "",
        externalRef = "",
        depot = "",
        createdAt = 0,
        currency = "",
        graceDays = 0,
        status = SortationSummaryStatus.IDLE,
    )
}
