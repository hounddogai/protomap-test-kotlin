package com.acme.transport.lastmile.locker.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched locker tier is in its life cycle. */
enum class BatchedLockerTierStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A batched locker tier as the locker team keeps it. */
data class BatchedLockerTier(
    var code: String,
    var label: String,
    var createdAt: Int,
    var contactEmail: String,
    var routeCode: String,
    var rate: Double,
    var vehicleId: String,
    var notes: String,
    var segment: String,
    var status: BatchedLockerTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedLockerTierStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedLockerTierStatus): Boolean {
        when (status) {
            BatchedLockerTierStatus.PROPOSED -> {
                when (next) {
                    BatchedLockerTierStatus.APPROVED -> return true
                }
                return false
            }
            BatchedLockerTierStatus.APPROVED -> {
                when (next) {
                    BatchedLockerTierStatus.REJECTED, BatchedLockerTierStatus.WITHDRAWN -> return true
                }
                return false
            }
            BatchedLockerTierStatus.REJECTED -> return next == BatchedLockerTierStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedLockerTierStatus) {
        if (!canMoveTo(next)) {
            error("batched locker tier $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedExposure(): Double = round((rate - createdAt.toDouble()) * 100) / 100

    fun snapshot(): BatchedLockerTier {
        return BatchedLockerTier(
            code = code,
            label = label,
            createdAt = createdAt,
            contactEmail = contactEmail,
            routeCode = routeCode,
            rate = rate,
            vehicleId = vehicleId,
            notes = notes,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a batched locker tier in status proposed. */
fun newBatchedLockerTier(code: String, label: String): BatchedLockerTier {
    return BatchedLockerTier(
        code = code,
        label = label,
        createdAt = 0,
        contactEmail = "",
        routeCode = "",
        rate = 0.0,
        vehicleId = "",
        notes = "",
        segment = "",
        status = BatchedLockerTierStatus.PROPOSED,
    )
}
