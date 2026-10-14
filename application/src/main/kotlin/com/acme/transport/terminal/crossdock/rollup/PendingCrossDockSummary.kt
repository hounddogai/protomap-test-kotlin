package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending cross dock summary is in its life cycle. */
enum class PendingCrossDockSummaryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A pending cross dock summary as the cross dock team keeps it. */
data class PendingCrossDockSummary(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var startsAt: Int,
    var sequence: Int,
    var endsAt: Int,
    var routeCode: String,
    var category: String,
    var status: PendingCrossDockSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCrossDockSummaryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCrossDockSummaryStatus): Boolean {
        when (status) {
            PendingCrossDockSummaryStatus.PLANNED -> return next == PendingCrossDockSummaryStatus.SCHEDULED || next == PendingCrossDockSummaryStatus.IN_PROGRESS
            PendingCrossDockSummaryStatus.SCHEDULED -> return next == PendingCrossDockSummaryStatus.COMPLETED
            PendingCrossDockSummaryStatus.IN_PROGRESS -> return next == PendingCrossDockSummaryStatus.COMPLETED
            PendingCrossDockSummaryStatus.COMPLETED -> return next == PendingCrossDockSummaryStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingCrossDockSummaryStatus) {
        if (!canMoveTo(next)) {
            error("pending cross dock summary $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedExposure(): Double = round((endsAt.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): PendingCrossDockSummary {
        return PendingCrossDockSummary(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            startsAt = startsAt,
            sequence = sequence,
            endsAt = endsAt,
            routeCode = routeCode,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a pending cross dock summary in status planned. */
fun newPendingCrossDockSummary(code: String, label: String): PendingCrossDockSummary {
    return PendingCrossDockSummary(
        code = code,
        label = label,
        etaMinutes = 0,
        startsAt = 0,
        sequence = 0,
        endsAt = 0,
        routeCode = "",
        category = "",
        status = PendingCrossDockSummaryStatus.PLANNED,
    )
}
