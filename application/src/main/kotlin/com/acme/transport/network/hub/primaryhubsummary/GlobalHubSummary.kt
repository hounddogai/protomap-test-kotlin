package com.acme.transport.network.hub.primaryhubsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global hub summary records of network's hub work, and the rules, stores, and calculations around them.

/** Where a global hub summary is in its life cycle. */
enum class GlobalHubSummaryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One global hub summary, with what the planners need to act on it. */
data class GlobalHubSummary(
    var code: String,
    var name: String,
    var pallets: Int,
    var threshold: Double,
    var weight: Double,
    var retries: Int,
    var version: Int,
    var fuelLiters: Double,
    var description: String,
    var status: GlobalHubSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalHubSummaryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalHubSummaryStatus): Boolean {
        when (status) {
            GlobalHubSummaryStatus.OPEN -> return next == GlobalHubSummaryStatus.RESOLVED
            GlobalHubSummaryStatus.IN_REVIEW -> return next == GlobalHubSummaryStatus.ARCHIVED
            GlobalHubSummaryStatus.RESOLVED -> {
                when (next) {
                    GlobalHubSummaryStatus.REOPENED -> return true
                }
                return false
            }
            GlobalHubSummaryStatus.REOPENED -> return next == GlobalHubSummaryStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: GlobalHubSummaryStatus) {
        if (!canMoveTo(next)) {
            error("global hub summary $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun projectedValue(): Double = round((retries.toDouble() + threshold) * 100) / 100

    fun snapshot(): GlobalHubSummary {
        return GlobalHubSummary(
            code = code,
            name = name,
            pallets = pallets,
            threshold = threshold,
            weight = weight,
            retries = retries,
            version = version,
            fuelLiters = fuelLiters,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global hub summary in status open. */
fun newGlobalHubSummary(code: String, name: String): GlobalHubSummary {
    return GlobalHubSummary(
        code = code,
        name = name,
        pallets = 0,
        threshold = 0.0,
        weight = 0.0,
        retries = 0,
        version = 0,
        fuelLiters = 0.0,
        description = "",
        status = GlobalHubSummaryStatus.OPEN,
    )
}
