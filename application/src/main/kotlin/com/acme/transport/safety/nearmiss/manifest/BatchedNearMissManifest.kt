package com.acme.transport.safety.nearmiss.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched near miss manifest records of safety's near miss work, and the rules, stores, and calculations
// around them.

/** Where a batched near miss manifest is in its life cycle. */
enum class BatchedNearMissManifestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a batched near miss manifest and its current status. */
data class BatchedNearMissManifest(
    var code: String,
    var title: String,
    var description: String,
    var ownerId: String,
    var attempts: Int,
    var odometer: Int,
    var threshold: Double,
    var driverId: String,
    var maximum: Double,
    var status: BatchedNearMissManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedNearMissManifestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedNearMissManifestStatus): Boolean {
        when (status) {
            BatchedNearMissManifestStatus.PLANNED -> {
                when (next) {
                    BatchedNearMissManifestStatus.SCHEDULED, BatchedNearMissManifestStatus.COMPLETED -> return true
                }
                return false
            }
            BatchedNearMissManifestStatus.SCHEDULED -> return next == BatchedNearMissManifestStatus.IN_PROGRESS || next == BatchedNearMissManifestStatus.CANCELLED
            BatchedNearMissManifestStatus.IN_PROGRESS -> return next == BatchedNearMissManifestStatus.CANCELLED
            BatchedNearMissManifestStatus.COMPLETED -> {
                when (next) {
                    BatchedNearMissManifestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedNearMissManifestStatus) {
        if (!canMoveTo(next)) {
            error("batched near miss manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun projectedExposure(): Double = round(maximum * odometer.toDouble() * 100) / 100

    fun snapshot(): BatchedNearMissManifest {
        return BatchedNearMissManifest(
            code = code,
            title = title,
            description = description,
            ownerId = ownerId,
            attempts = attempts,
            odometer = odometer,
            threshold = threshold,
            driverId = driverId,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched near miss manifest in status planned. */
fun newBatchedNearMissManifest(code: String, title: String): BatchedNearMissManifest {
    return BatchedNearMissManifest(
        code = code,
        title = title,
        description = "",
        ownerId = "",
        attempts = 0,
        odometer = 0,
        threshold = 0.0,
        driverId = "",
        maximum = 0.0,
        status = BatchedNearMissManifestStatus.PLANNED,
    )
}
