package com.acme.transport.safety.violation.violationpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted violation summary is in its life cycle. */
enum class WeightedViolationSummaryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A weighted violation summary as the violation team keeps it. */
data class WeightedViolationSummary(
    var code: String,
    var name: String,
    var batchSize: Int,
    var ceiling: Double,
    var country: String,
    var status: WeightedViolationSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedViolationSummaryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedViolationSummaryStatus): Boolean {
        when (status) {
            WeightedViolationSummaryStatus.OPEN -> return next == WeightedViolationSummaryStatus.RESOLVED
            WeightedViolationSummaryStatus.IN_REVIEW -> {
                when (next) {
                    WeightedViolationSummaryStatus.RESOLVED, WeightedViolationSummaryStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedViolationSummaryStatus.RESOLVED -> return next == WeightedViolationSummaryStatus.ARCHIVED
            WeightedViolationSummaryStatus.REOPENED -> return next == WeightedViolationSummaryStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: WeightedViolationSummaryStatus) {
        if (!canMoveTo(next)) {
            error("weighted violation summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted violation summary"} $code"

    fun projectedValue(): Double = round(batchSize.toDouble() * ceiling * 100) / 100

    fun snapshot(): WeightedViolationSummary {
        return WeightedViolationSummary(
            code = code,
            name = name,
            batchSize = batchSize,
            ceiling = ceiling,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a weighted violation summary in status open. */
fun newWeightedViolationSummary(code: String, name: String): WeightedViolationSummary {
    return WeightedViolationSummary(
        code = code,
        name = name,
        batchSize = 0,
        ceiling = 0.0,
        country = "",
        status = WeightedViolationSummaryStatus.OPEN,
    )
}
