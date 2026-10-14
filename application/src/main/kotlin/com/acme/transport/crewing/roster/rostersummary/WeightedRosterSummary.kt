package com.acme.transport.crewing.roster.rostersummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted roster summary records of crewing's roster work, and the rules, stores, and calculations
// around them.

/** Where a weighted roster summary is in its life cycle. */
enum class WeightedRosterSummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A weighted roster summary as the roster team keeps it. */
data class WeightedRosterSummary(
    var code: String,
    var label: String,
    var name: String,
    var segment: String,
    var revision: Int,
    var graceDays: Int,
    var weight: Double,
    var version: Int,
    var createdAt: Int,
    var status: WeightedRosterSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedRosterSummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedRosterSummaryStatus): Boolean {
        when (status) {
            WeightedRosterSummaryStatus.CREATED -> return next == WeightedRosterSummaryStatus.VALIDATED
            WeightedRosterSummaryStatus.VALIDATED -> return next == WeightedRosterSummaryStatus.PUBLISHED || next == WeightedRosterSummaryStatus.RETIRED
            WeightedRosterSummaryStatus.PUBLISHED -> {
                when (next) {
                    WeightedRosterSummaryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedRosterSummaryStatus) {
        if (!canMoveTo(next)) {
            error("weighted roster summary $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedVolume(): Double = round((graceDays.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): WeightedRosterSummary {
        return WeightedRosterSummary(
            code = code,
            label = label,
            name = name,
            segment = segment,
            revision = revision,
            graceDays = graceDays,
            weight = weight,
            version = version,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a weighted roster summary in status created. */
fun newWeightedRosterSummary(code: String, label: String): WeightedRosterSummary {
    return WeightedRosterSummary(
        code = code,
        label = label,
        name = "",
        segment = "",
        revision = 0,
        graceDays = 0,
        weight = 0.0,
        version = 0,
        createdAt = 0,
        status = WeightedRosterSummaryStatus.CREATED,
    )
}
