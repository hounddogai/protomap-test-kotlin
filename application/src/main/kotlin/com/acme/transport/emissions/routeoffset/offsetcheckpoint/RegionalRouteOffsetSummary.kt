package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional route offset summary is in its life cycle. */
enum class RegionalRouteOffsetSummaryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A regional route offset summary as the route offset team keeps it. */
data class RegionalRouteOffsetSummary(
    var code: String,
    var label: String,
    var currency: String,
    var capacity: Int,
    var margin: Double,
    var batchSize: Int,
    var fuelLiters: Double,
    var graceDays: Int,
    var attributes: MutableMap<String, String>,
    var source: String,
    var status: RegionalRouteOffsetSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalRouteOffsetSummaryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalRouteOffsetSummaryStatus): Boolean {
        when (status) {
            RegionalRouteOffsetSummaryStatus.PLANNED -> return next == RegionalRouteOffsetSummaryStatus.SCHEDULED || next == RegionalRouteOffsetSummaryStatus.CANCELLED
            RegionalRouteOffsetSummaryStatus.SCHEDULED -> return next == RegionalRouteOffsetSummaryStatus.IN_PROGRESS || next == RegionalRouteOffsetSummaryStatus.CANCELLED
            RegionalRouteOffsetSummaryStatus.IN_PROGRESS -> return next == RegionalRouteOffsetSummaryStatus.COMPLETED || next == RegionalRouteOffsetSummaryStatus.CANCELLED
            RegionalRouteOffsetSummaryStatus.COMPLETED -> return next == RegionalRouteOffsetSummaryStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RegionalRouteOffsetSummaryStatus) {
        if (!canMoveTo(next)) {
            error("regional route offset summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netTotal(): Double = round((capacity.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): RegionalRouteOffsetSummary {
        return RegionalRouteOffsetSummary(
            code = code,
            label = label,
            currency = currency,
            capacity = capacity,
            margin = margin,
            batchSize = batchSize,
            fuelLiters = fuelLiters,
            graceDays = graceDays,
            attributes = attributes,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a regional route offset summary in status planned. */
fun newRegionalRouteOffsetSummary(code: String, label: String): RegionalRouteOffsetSummary {
    return RegionalRouteOffsetSummary(
        code = code,
        label = label,
        currency = "",
        capacity = 0,
        margin = 0.0,
        batchSize = 0,
        fuelLiters = 0.0,
        graceDays = 0,
        attributes = mutableMapOf<String, String>(),
        source = "",
        status = RegionalRouteOffsetSummaryStatus.PLANNED,
    )
}
