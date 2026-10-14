package com.acme.transport.crewing.qualification.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched qualification summary records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a batched qualification summary is in its life cycle. */
enum class BatchedQualificationSummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One batched qualification summary, with what the planners need to act on it. */
data class BatchedQualificationSummary(
    var code: String,
    var name: String,
    var depot: String,
    var label: String,
    var durationMinutes: Int,
    var loadKg: Double,
    var trailerId: String,
    var status: BatchedQualificationSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedQualificationSummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedQualificationSummaryStatus): Boolean {
        when (status) {
            BatchedQualificationSummaryStatus.CREATED -> return next == BatchedQualificationSummaryStatus.VALIDATED || next == BatchedQualificationSummaryStatus.PUBLISHED
            BatchedQualificationSummaryStatus.VALIDATED -> return next == BatchedQualificationSummaryStatus.PUBLISHED
            BatchedQualificationSummaryStatus.PUBLISHED -> {
                when (next) {
                    BatchedQualificationSummaryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedQualificationSummaryStatus) {
        if (!canMoveTo(next)) {
            error("batched qualification summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $name"

    fun adjustedValue(): Double = round(durationMinutes.toDouble() * loadKg * 100) / 100

    fun snapshot(): BatchedQualificationSummary {
        return BatchedQualificationSummary(
            code = code,
            name = name,
            depot = depot,
            label = label,
            durationMinutes = durationMinutes,
            loadKg = loadKg,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a batched qualification summary in status created. */
fun newBatchedQualificationSummary(code: String, name: String): BatchedQualificationSummary {
    return BatchedQualificationSummary(
        code = code,
        name = name,
        depot = "",
        label = "",
        durationMinutes = 0,
        loadKg = 0.0,
        trailerId = "",
        status = BatchedQualificationSummaryStatus.CREATED,
    )
}
