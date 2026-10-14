package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.kit.text.normalize

/** Where a tiered sortation summary is in its life cycle. */
enum class TieredSortationSummaryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One tiered sortation summary, with what the planners need to act on it. */
data class TieredSortationSummary(
    var code: String,
    var label: String,
    var retries: Int,
    var name: String,
    var archived: Boolean,
    var status: TieredSortationSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredSortationSummaryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredSortationSummaryStatus): Boolean {
        when (status) {
            TieredSortationSummaryStatus.PLANNED -> return next == TieredSortationSummaryStatus.CANCELLED
            TieredSortationSummaryStatus.SCHEDULED -> return next == TieredSortationSummaryStatus.COMPLETED
            TieredSortationSummaryStatus.IN_PROGRESS -> return next == TieredSortationSummaryStatus.COMPLETED || next == TieredSortationSummaryStatus.CANCELLED
            TieredSortationSummaryStatus.COMPLETED -> {
                when (next) {
                    TieredSortationSummaryStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredSortationSummaryStatus) {
        if (!canMoveTo(next)) {
            error("tiered sortation summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): TieredSortationSummary {
        return TieredSortationSummary(
            code = code,
            label = label,
            retries = retries,
            name = name,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered sortation summary in status planned. */
fun newTieredSortationSummary(code: String, label: String): TieredSortationSummary {
    return TieredSortationSummary(
        code = code,
        label = label,
        retries = 0,
        name = "",
        archived = false,
        status = TieredSortationSummaryStatus.PLANNED,
    )
}
