package com.acme.transport.lastmile.parcel.quota

import com.acme.transport.kit.text.normalize

/** Where a scheduled parcel summary is in its life cycle. */
enum class ScheduledParcelSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A scheduled parcel summary as the parcel team keeps it. */
data class ScheduledParcelSummary(
    var code: String,
    var label: String,
    var unit: String,
    var summary: String,
    var shiftCode: String,
    var quantity: Int,
    var status: ScheduledParcelSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledParcelSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledParcelSummaryStatus): Boolean {
        when (status) {
            ScheduledParcelSummaryStatus.PROPOSED -> return next == ScheduledParcelSummaryStatus.REJECTED || next == ScheduledParcelSummaryStatus.WITHDRAWN
            ScheduledParcelSummaryStatus.APPROVED -> {
                when (next) {
                    ScheduledParcelSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScheduledParcelSummaryStatus.REJECTED -> return next == ScheduledParcelSummaryStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ScheduledParcelSummaryStatus) {
        if (!canMoveTo(next)) {
            error("scheduled parcel summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scheduled parcel summary"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): ScheduledParcelSummary {
        return ScheduledParcelSummary(
            code = code,
            label = label,
            unit = unit,
            summary = summary,
            shiftCode = shiftCode,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scheduled parcel summary in status proposed. */
fun newScheduledParcelSummary(code: String, label: String): ScheduledParcelSummary {
    return ScheduledParcelSummary(
        code = code,
        label = label,
        unit = "",
        summary = "",
        shiftCode = "",
        quantity = 0,
        status = ScheduledParcelSummaryStatus.PROPOSED,
    )
}
