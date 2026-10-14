package com.acme.transport.crewing.swap.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap summary is in its life cycle. */
enum class SwapSummaryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A swap summary. Its code is unique within a tenant. */
data class SwapSummary(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var segment: String,
    var approved: Boolean,
    var contactEmail: String,
    var batchSize: Int,
    var status: SwapSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapSummaryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapSummaryStatus): Boolean {
        when (status) {
            SwapSummaryStatus.PLANNED -> return next == SwapSummaryStatus.IN_PROGRESS || next == SwapSummaryStatus.CANCELLED
            SwapSummaryStatus.SCHEDULED -> return next == SwapSummaryStatus.COMPLETED
            SwapSummaryStatus.IN_PROGRESS -> {
                when (next) {
                    SwapSummaryStatus.COMPLETED, SwapSummaryStatus.CANCELLED -> return true
                }
                return false
            }
            SwapSummaryStatus.COMPLETED -> {
                when (next) {
                    SwapSummaryStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapSummaryStatus) {
        if (!canMoveTo(next)) {
            error("swap summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"swap summary"} $code"

    fun adjustedTotal(): Double = round((durationMinutes.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): SwapSummary {
        return SwapSummary(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            segment = segment,
            approved = approved,
            contactEmail = contactEmail,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a swap summary in status planned. */
fun newSwapSummary(code: String, label: String): SwapSummary {
    return SwapSummary(
        code = code,
        label = label,
        durationMinutes = 0,
        segment = "",
        approved = false,
        contactEmail = "",
        batchSize = 0,
        status = SwapSummaryStatus.PLANNED,
    )
}
