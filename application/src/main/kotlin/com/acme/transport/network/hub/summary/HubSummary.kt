package com.acme.transport.network.hub.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hub summary records of network's hub work, and the rules, stores, and calculations around them.

/** Where a hub summary is in its life cycle. */
enum class HubSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A hub summary as the hub team keeps it. */
data class HubSummary(
    var code: String,
    var label: String,
    var title: String,
    var weight: Double,
    var odometer: Int,
    var status: HubSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: HubSummaryStatus): Boolean {
        when (status) {
            HubSummaryStatus.PROPOSED -> {
                when (next) {
                    HubSummaryStatus.REJECTED, HubSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
            HubSummaryStatus.APPROVED -> return next == HubSummaryStatus.WITHDRAWN
            HubSummaryStatus.REJECTED -> return next == HubSummaryStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: HubSummaryStatus) {
        if (!canMoveTo(next)) {
            error("hub summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedValue(): Double = round((weight - odometer.toDouble()) * 100) / 100

    fun snapshot(): HubSummary {
        return HubSummary(
            code = code,
            label = label,
            title = title,
            weight = weight,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a hub summary in status proposed. */
fun newHubSummary(code: String, label: String): HubSummary {
    return HubSummary(
        code = code,
        label = label,
        title = "",
        weight = 0.0,
        odometer = 0,
        status = HubSummaryStatus.PROPOSED,
    )
}
