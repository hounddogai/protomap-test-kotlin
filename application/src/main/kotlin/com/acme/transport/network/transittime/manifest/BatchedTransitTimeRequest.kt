package com.acme.transport.network.transittime.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched transit time request is in its life cycle. */
enum class BatchedTransitTimeRequestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a batched transit time request and its current status. */
data class BatchedTransitTimeRequest(
    var code: String,
    var label: String,
    var floorValue: Double,
    var score: Double,
    var approved: Boolean,
    var discount: Double,
    var status: BatchedTransitTimeRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTransitTimeRequestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTransitTimeRequestStatus): Boolean {
        when (status) {
            BatchedTransitTimeRequestStatus.CREATED -> return next == BatchedTransitTimeRequestStatus.PUBLISHED || next == BatchedTransitTimeRequestStatus.RETIRED
            BatchedTransitTimeRequestStatus.VALIDATED -> return next == BatchedTransitTimeRequestStatus.PUBLISHED || next == BatchedTransitTimeRequestStatus.RETIRED
            BatchedTransitTimeRequestStatus.PUBLISHED -> return next == BatchedTransitTimeRequestStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BatchedTransitTimeRequestStatus) {
        if (!canMoveTo(next)) {
            error("batched transit time request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedVolume(): Double = round((score + discount) * 100) / 100

    fun snapshot(): BatchedTransitTimeRequest {
        return BatchedTransitTimeRequest(
            code = code,
            label = label,
            floorValue = floorValue,
            score = score,
            approved = approved,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched transit time request in status created. */
fun newBatchedTransitTimeRequest(code: String, label: String): BatchedTransitTimeRequest {
    return BatchedTransitTimeRequest(
        code = code,
        label = label,
        floorValue = 0.0,
        score = 0.0,
        approved = false,
        discount = 0.0,
        status = BatchedTransitTimeRequestStatus.CREATED,
    )
}
