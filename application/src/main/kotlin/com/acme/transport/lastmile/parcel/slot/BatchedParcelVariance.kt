package com.acme.transport.lastmile.parcel.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched parcel variance is in its life cycle. */
enum class BatchedParcelVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A batched parcel variance as the parcel team keeps it. */
data class BatchedParcelVariance(
    var code: String,
    var name: String,
    var score: Double,
    var revision: Int,
    var batchSize: Int,
    var attempts: Int,
    var driverId: String,
    var vehicleId: String,
    var timezone: String,
    var title: String,
    var checksum: String,
    var status: BatchedParcelVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedParcelVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedParcelVarianceStatus): Boolean {
        when (status) {
            BatchedParcelVarianceStatus.PLANNED -> return next == BatchedParcelVarianceStatus.SCHEDULED || next == BatchedParcelVarianceStatus.IN_PROGRESS
            BatchedParcelVarianceStatus.SCHEDULED -> return next == BatchedParcelVarianceStatus.COMPLETED
            BatchedParcelVarianceStatus.IN_PROGRESS -> {
                when (next) {
                    BatchedParcelVarianceStatus.COMPLETED, BatchedParcelVarianceStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedParcelVarianceStatus.COMPLETED -> return next == BatchedParcelVarianceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BatchedParcelVarianceStatus) {
        if (!canMoveTo(next)) {
            error("batched parcel variance $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun effectiveExposure(): Double = round((attempts.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): BatchedParcelVariance {
        return BatchedParcelVariance(
            code = code,
            name = name,
            score = score,
            revision = revision,
            batchSize = batchSize,
            attempts = attempts,
            driverId = driverId,
            vehicleId = vehicleId,
            timezone = timezone,
            title = title,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched parcel variance in status planned. */
fun newBatchedParcelVariance(code: String, name: String): BatchedParcelVariance {
    return BatchedParcelVariance(
        code = code,
        name = name,
        score = 0.0,
        revision = 0,
        batchSize = 0,
        attempts = 0,
        driverId = "",
        vehicleId = "",
        timezone = "",
        title = "",
        checksum = "",
        status = BatchedParcelVarianceStatus.PLANNED,
    )
}
