package com.acme.transport.network.zone.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual zone summary is in its life cycle. */
enum class ManualZoneSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One manual zone summary, with what the planners need to act on it. */
data class ManualZoneSummary(
    var code: String,
    var label: String,
    var limit: Int,
    var name: String,
    var category: String,
    var segment: String,
    var cost: Double,
    var status: ManualZoneSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualZoneSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualZoneSummaryStatus): Boolean {
        when (status) {
            ManualZoneSummaryStatus.PROPOSED -> return next == ManualZoneSummaryStatus.APPROVED || next == ManualZoneSummaryStatus.REJECTED
            ManualZoneSummaryStatus.APPROVED -> {
                when (next) {
                    ManualZoneSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualZoneSummaryStatus.REJECTED -> return next == ManualZoneSummaryStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ManualZoneSummaryStatus) {
        if (!canMoveTo(next)) {
            error("manual zone summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"manual zone summary"} $code"

    fun weightedLoad(): Double = round((limit.toDouble() - cost) * 100) / 100

    fun snapshot(): ManualZoneSummary {
        return ManualZoneSummary(
            code = code,
            label = label,
            limit = limit,
            name = name,
            category = category,
            segment = segment,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a manual zone summary in status proposed. */
fun newManualZoneSummary(code: String, label: String): ManualZoneSummary {
    return ManualZoneSummary(
        code = code,
        label = label,
        limit = 0,
        name = "",
        category = "",
        segment = "",
        cost = 0.0,
        status = ManualZoneSummaryStatus.PROPOSED,
    )
}
