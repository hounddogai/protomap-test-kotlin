package com.acme.transport.fleetops.utilization.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched utilization tier is in its life cycle. */
enum class BatchedUtilizationTierStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a batched utilization tier and its current status. */
data class BatchedUtilizationTier(
    var code: String,
    var label: String,
    var score: Double,
    var active: Boolean,
    var distanceKm: Double,
    var driverId: String,
    var startsAt: Int,
    var stopCount: Int,
    var carrier: String,
    var shiftCode: String,
    var vehicleId: String,
    var status: BatchedUtilizationTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedUtilizationTierStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedUtilizationTierStatus): Boolean {
        when (status) {
            BatchedUtilizationTierStatus.REQUESTED -> {
                when (next) {
                    BatchedUtilizationTierStatus.EXPIRED -> return true
                }
                return false
            }
            BatchedUtilizationTierStatus.GRANTED -> {
                when (next) {
                    BatchedUtilizationTierStatus.REVOKED, BatchedUtilizationTierStatus.EXPIRED -> return true
                }
                return false
            }
            BatchedUtilizationTierStatus.REVOKED -> return next == BatchedUtilizationTierStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BatchedUtilizationTierStatus) {
        if (!canMoveTo(next)) {
            error("batched utilization tier $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun netLoad(): Double = round((startsAt.toDouble() + score) * 100) / 100

    fun snapshot(): BatchedUtilizationTier {
        return BatchedUtilizationTier(
            code = code,
            label = label,
            score = score,
            active = active,
            distanceKm = distanceKm,
            driverId = driverId,
            startsAt = startsAt,
            stopCount = stopCount,
            carrier = carrier,
            shiftCode = shiftCode,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched utilization tier in status requested. */
fun newBatchedUtilizationTier(code: String, label: String): BatchedUtilizationTier {
    return BatchedUtilizationTier(
        code = code,
        label = label,
        score = 0.0,
        active = false,
        distanceKm = 0.0,
        driverId = "",
        startsAt = 0,
        stopCount = 0,
        carrier = "",
        shiftCode = "",
        vehicleId = "",
        status = BatchedUtilizationTierStatus.REQUESTED,
    )
}
