package com.acme.transport.network.hub.legacyhubplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched hub exception is in its life cycle. */
enum class BatchedHubExceptionStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A batched hub exception as the hub team keeps it. */
data class BatchedHubException(
    var code: String,
    var title: String,
    var cost: Double,
    var country: String,
    var rate: Double,
    var description: String,
    var margin: Double,
    var status: BatchedHubExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedHubExceptionStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedHubExceptionStatus): Boolean {
        when (status) {
            BatchedHubExceptionStatus.CREATED -> {
                when (next) {
                    BatchedHubExceptionStatus.RETIRED -> return true
                }
                return false
            }
            BatchedHubExceptionStatus.VALIDATED -> {
                when (next) {
                    BatchedHubExceptionStatus.PUBLISHED, BatchedHubExceptionStatus.RETIRED -> return true
                }
                return false
            }
            BatchedHubExceptionStatus.PUBLISHED -> return next == BatchedHubExceptionStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BatchedHubExceptionStatus) {
        if (!canMoveTo(next)) {
            error("batched hub exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedVolume(): Double = round((cost + rate) * 100) / 100

    fun snapshot(): BatchedHubException {
        return BatchedHubException(
            code = code,
            title = title,
            cost = cost,
            country = country,
            rate = rate,
            description = description,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a batched hub exception in status created. */
fun newBatchedHubException(code: String, title: String): BatchedHubException {
    return BatchedHubException(
        code = code,
        title = title,
        cost = 0.0,
        country = "",
        rate = 0.0,
        description = "",
        margin = 0.0,
        status = BatchedHubExceptionStatus.CREATED,
    )
}
