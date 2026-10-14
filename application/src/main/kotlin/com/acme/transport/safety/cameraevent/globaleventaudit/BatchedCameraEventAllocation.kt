package com.acme.transport.safety.cameraevent.globaleventaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched camera event allocation is in its life cycle. */
enum class BatchedCameraEventAllocationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A batched camera event allocation. Its code is unique within a tenant. */
data class BatchedCameraEventAllocation(
    var code: String,
    var name: String,
    var weight: Double,
    var lane: String,
    var margin: Double,
    var distanceKm: Double,
    var tags: MutableList<String>,
    var ownerId: String,
    var checksum: String,
    var vehicleId: String,
    var retries: Int,
    var status: BatchedCameraEventAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCameraEventAllocationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCameraEventAllocationStatus): Boolean {
        when (status) {
            BatchedCameraEventAllocationStatus.REQUESTED -> return next == BatchedCameraEventAllocationStatus.REVOKED || next == BatchedCameraEventAllocationStatus.EXPIRED
            BatchedCameraEventAllocationStatus.GRANTED -> {
                when (next) {
                    BatchedCameraEventAllocationStatus.REVOKED -> return true
                }
                return false
            }
            BatchedCameraEventAllocationStatus.REVOKED -> return next == BatchedCameraEventAllocationStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BatchedCameraEventAllocationStatus) {
        if (!canMoveTo(next)) {
            error("batched camera event allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedExposure(): Double = round(distanceKm * margin * 100) / 100

    fun snapshot(): BatchedCameraEventAllocation {
        return BatchedCameraEventAllocation(
            code = code,
            name = name,
            weight = weight,
            lane = lane,
            margin = margin,
            distanceKm = distanceKm,
            tags = tags,
            ownerId = ownerId,
            checksum = checksum,
            vehicleId = vehicleId,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a batched camera event allocation in status requested. */
fun newBatchedCameraEventAllocation(code: String, name: String): BatchedCameraEventAllocation {
    return BatchedCameraEventAllocation(
        code = code,
        name = name,
        weight = 0.0,
        lane = "",
        margin = 0.0,
        distanceKm = 0.0,
        tags = mutableListOf<String>(),
        ownerId = "",
        checksum = "",
        vehicleId = "",
        retries = 0,
        status = BatchedCameraEventAllocationStatus.REQUESTED,
    )
}
