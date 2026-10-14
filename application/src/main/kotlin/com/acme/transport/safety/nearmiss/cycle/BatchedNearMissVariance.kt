package com.acme.transport.safety.nearmiss.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched near miss variance is in its life cycle. */
enum class BatchedNearMissVarianceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A batched near miss variance as the near miss team keeps it. */
data class BatchedNearMissVariance(
    var code: String,
    var title: String,
    var notes: String,
    var reviewer: String,
    var region: String,
    var driverId: String,
    var floorValue: Double,
    var budget: Double,
    var retries: Int,
    var weight: Double,
    var description: String,
    var status: BatchedNearMissVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedNearMissVarianceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedNearMissVarianceStatus): Boolean {
        when (status) {
            BatchedNearMissVarianceStatus.DRAFT -> return next == BatchedNearMissVarianceStatus.PENDING || next == BatchedNearMissVarianceStatus.CLOSED
            BatchedNearMissVarianceStatus.PENDING -> return next == BatchedNearMissVarianceStatus.CLOSED
            BatchedNearMissVarianceStatus.ACTIVE -> return next == BatchedNearMissVarianceStatus.CLOSED
            BatchedNearMissVarianceStatus.SUSPENDED -> {
                when (next) {
                    BatchedNearMissVarianceStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedNearMissVarianceStatus) {
        if (!canMoveTo(next)) {
            error("batched near miss variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netLoad(): Double = round((budget - weight) * 100) / 100

    fun snapshot(): BatchedNearMissVariance {
        return BatchedNearMissVariance(
            code = code,
            title = title,
            notes = notes,
            reviewer = reviewer,
            region = region,
            driverId = driverId,
            floorValue = floorValue,
            budget = budget,
            retries = retries,
            weight = weight,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a batched near miss variance in status draft. */
fun newBatchedNearMissVariance(code: String, title: String): BatchedNearMissVariance {
    return BatchedNearMissVariance(
        code = code,
        title = title,
        notes = "",
        reviewer = "",
        region = "",
        driverId = "",
        floorValue = 0.0,
        budget = 0.0,
        retries = 0,
        weight = 0.0,
        description = "",
        status = BatchedNearMissVarianceStatus.DRAFT,
    )
}
