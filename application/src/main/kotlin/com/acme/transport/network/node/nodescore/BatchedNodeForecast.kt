package com.acme.transport.network.node.nodescore

import com.acme.transport.kit.text.normalize

/** Where a batched node forecast is in its life cycle. */
enum class BatchedNodeForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A batched node forecast. Its code is unique within a tenant. */
data class BatchedNodeForecast(
    var code: String,
    var title: String,
    var shiftCode: String,
    var batchSize: Int,
    var timezone: String,
    var summary: String,
    var trailerId: String,
    var status: BatchedNodeForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedNodeForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedNodeForecastStatus): Boolean {
        when (status) {
            BatchedNodeForecastStatus.CREATED -> return next == BatchedNodeForecastStatus.VALIDATED
            BatchedNodeForecastStatus.VALIDATED -> return next == BatchedNodeForecastStatus.PUBLISHED
            BatchedNodeForecastStatus.PUBLISHED -> return next == BatchedNodeForecastStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BatchedNodeForecastStatus) {
        if (!canMoveTo(next)) {
            error("batched node forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): BatchedNodeForecast {
        return BatchedNodeForecast(
            code = code,
            title = title,
            shiftCode = shiftCode,
            batchSize = batchSize,
            timezone = timezone,
            summary = summary,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched node forecast in status created. */
fun newBatchedNodeForecast(code: String, title: String): BatchedNodeForecast {
    return BatchedNodeForecast(
        code = code,
        title = title,
        shiftCode = "",
        batchSize = 0,
        timezone = "",
        summary = "",
        trailerId = "",
        status = BatchedNodeForecastStatus.CREATED,
    )
}
