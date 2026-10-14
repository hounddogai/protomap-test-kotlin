package com.acme.transport.customs.broker.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal broker summary is in its life cycle. */
enum class SeasonalBrokerSummaryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A seasonal broker summary as the broker team keeps it. */
data class SeasonalBrokerSummary(
    var code: String,
    var label: String,
    var maximum: Double,
    var name: String,
    var carrier: String,
    var weight: Double,
    var depot: String,
    var version: Int,
    var notes: String,
    var status: SeasonalBrokerSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalBrokerSummaryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalBrokerSummaryStatus): Boolean {
        when (status) {
            SeasonalBrokerSummaryStatus.NEW -> return next == SeasonalBrokerSummaryStatus.QUEUED || next == SeasonalBrokerSummaryStatus.RUNNING
            SeasonalBrokerSummaryStatus.QUEUED -> return next == SeasonalBrokerSummaryStatus.FAILED
            SeasonalBrokerSummaryStatus.RUNNING -> return next == SeasonalBrokerSummaryStatus.DONE
            SeasonalBrokerSummaryStatus.DONE -> {
                when (next) {
                    SeasonalBrokerSummaryStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalBrokerSummaryStatus) {
        if (!canMoveTo(next)) {
            error("seasonal broker summary $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"seasonal broker summary"} $code"

    fun projectedTotal(): Double = round((weight + maximum) * 100) / 100

    fun snapshot(): SeasonalBrokerSummary {
        return SeasonalBrokerSummary(
            code = code,
            label = label,
            maximum = maximum,
            name = name,
            carrier = carrier,
            weight = weight,
            depot = depot,
            version = version,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a seasonal broker summary in status new. */
fun newSeasonalBrokerSummary(code: String, label: String): SeasonalBrokerSummary {
    return SeasonalBrokerSummary(
        code = code,
        label = label,
        maximum = 0.0,
        name = "",
        carrier = "",
        weight = 0.0,
        depot = "",
        version = 0,
        notes = "",
        status = SeasonalBrokerSummaryStatus.NEW,
    )
}
