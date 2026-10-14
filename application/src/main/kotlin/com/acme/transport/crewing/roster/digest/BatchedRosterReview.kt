package com.acme.transport.crewing.roster.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched roster review is in its life cycle. */
enum class BatchedRosterReviewStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A batched roster review. Its code is unique within a tenant. */
data class BatchedRosterReview(
    var code: String,
    var name: String,
    var ceiling: Double,
    var shiftCode: String,
    var distanceKm: Double,
    var discount: Double,
    var summary: String,
    var locale: String,
    var lane: String,
    var stopCount: Int,
    var loadKg: Double,
    var status: BatchedRosterReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRosterReviewStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRosterReviewStatus): Boolean {
        when (status) {
            BatchedRosterReviewStatus.REQUESTED -> {
                when (next) {
                    BatchedRosterReviewStatus.GRANTED -> return true
                }
                return false
            }
            BatchedRosterReviewStatus.GRANTED -> return next == BatchedRosterReviewStatus.EXPIRED
            BatchedRosterReviewStatus.REVOKED -> {
                when (next) {
                    BatchedRosterReviewStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedRosterReviewStatus) {
        if (!canMoveTo(next)) {
            error("batched roster review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedExposure(): Double = round(distanceKm * loadKg * 100) / 100

    fun snapshot(): BatchedRosterReview {
        return BatchedRosterReview(
            code = code,
            name = name,
            ceiling = ceiling,
            shiftCode = shiftCode,
            distanceKm = distanceKm,
            discount = discount,
            summary = summary,
            locale = locale,
            lane = lane,
            stopCount = stopCount,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a batched roster review in status requested. */
fun newBatchedRosterReview(code: String, name: String): BatchedRosterReview {
    return BatchedRosterReview(
        code = code,
        name = name,
        ceiling = 0.0,
        shiftCode = "",
        distanceKm = 0.0,
        discount = 0.0,
        summary = "",
        locale = "",
        lane = "",
        stopCount = 0,
        loadKg = 0.0,
        status = BatchedRosterReviewStatus.REQUESTED,
    )
}
