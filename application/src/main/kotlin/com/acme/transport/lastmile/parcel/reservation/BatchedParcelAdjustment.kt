package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched parcel adjustment is in its life cycle. */
enum class BatchedParcelAdjustmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A batched parcel adjustment. Its code is unique within a tenant. */
data class BatchedParcelAdjustment(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var minimum: Double,
    var revision: Int,
    var ownerId: String,
    var floorValue: Double,
    var active: Boolean,
    var status: BatchedParcelAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedParcelAdjustmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedParcelAdjustmentStatus): Boolean {
        when (status) {
            BatchedParcelAdjustmentStatus.OPEN -> {
                when (next) {
                    BatchedParcelAdjustmentStatus.IN_REVIEW, BatchedParcelAdjustmentStatus.RESOLVED -> return true
                }
                return false
            }
            BatchedParcelAdjustmentStatus.IN_REVIEW -> {
                when (next) {
                    BatchedParcelAdjustmentStatus.REOPENED, BatchedParcelAdjustmentStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedParcelAdjustmentStatus.RESOLVED -> return next == BatchedParcelAdjustmentStatus.REOPENED || next == BatchedParcelAdjustmentStatus.ARCHIVED
            BatchedParcelAdjustmentStatus.REOPENED -> return next == BatchedParcelAdjustmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: BatchedParcelAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("batched parcel adjustment $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"batched parcel adjustment"} $code"

    fun netVolume(): Double = round((revision.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): BatchedParcelAdjustment {
        return BatchedParcelAdjustment(
            code = code,
            label = label,
            distanceKm = distanceKm,
            minimum = minimum,
            revision = revision,
            ownerId = ownerId,
            floorValue = floorValue,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched parcel adjustment in status open. */
fun newBatchedParcelAdjustment(code: String, label: String): BatchedParcelAdjustment {
    return BatchedParcelAdjustment(
        code = code,
        label = label,
        distanceKm = 0.0,
        minimum = 0.0,
        revision = 0,
        ownerId = "",
        floorValue = 0.0,
        active = false,
        status = BatchedParcelAdjustmentStatus.OPEN,
    )
}
