package com.acme.transport.lastmile.drop.weighteddropnotice

import com.acme.transport.kit.text.normalize

/** Where a drop summary is in its life cycle. */
enum class DropSummaryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A drop summary. Its code is unique within a tenant. */
data class DropSummary(
    var code: String,
    var title: String,
    var lane: String,
    var channel: String,
    var summary: String,
    var contactEmail: String,
    var quantity: Int,
    var status: DropSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropSummaryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropSummaryStatus): Boolean {
        when (status) {
            DropSummaryStatus.PLANNED -> {
                when (next) {
                    DropSummaryStatus.IN_PROGRESS -> return true
                }
                return false
            }
            DropSummaryStatus.SCHEDULED -> {
                when (next) {
                    DropSummaryStatus.COMPLETED, DropSummaryStatus.CANCELLED -> return true
                }
                return false
            }
            DropSummaryStatus.IN_PROGRESS -> return next == DropSummaryStatus.COMPLETED
            DropSummaryStatus.COMPLETED -> return next == DropSummaryStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DropSummaryStatus) {
        if (!canMoveTo(next)) {
            error("drop summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): DropSummary {
        return DropSummary(
            code = code,
            title = title,
            lane = lane,
            channel = channel,
            summary = summary,
            contactEmail = contactEmail,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a drop summary in status planned. */
fun newDropSummary(code: String, title: String): DropSummary {
    return DropSummary(
        code = code,
        title = title,
        lane = "",
        channel = "",
        summary = "",
        contactEmail = "",
        quantity = 0,
        status = DropSummaryStatus.PLANNED,
    )
}
