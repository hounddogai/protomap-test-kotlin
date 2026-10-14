package com.acme.transport.linehaul.capacity.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capacity request records of linehaul's capacity work, and the rules, stores, and calculations around
// them.

/** Where a capacity request is in its life cycle. */
enum class CapacityRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capacity request. Its code is unique within a tenant. */
data class CapacityRequest(
    var code: String,
    var title: String,
    var pallets: Int,
    var vehicleId: String,
    var attempts: Int,
    var stopCount: Int,
    var category: String,
    var depot: String,
    var durationMinutes: Int,
    var endsAt: Int,
    var ratio: Double,
    var status: CapacityRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityRequestStatus): Boolean {
        when (status) {
            CapacityRequestStatus.PROPOSED -> {
                when (next) {
                    CapacityRequestStatus.APPROVED -> return true
                }
                return false
            }
            CapacityRequestStatus.APPROVED -> return next == CapacityRequestStatus.WITHDRAWN
            CapacityRequestStatus.REJECTED -> {
                when (next) {
                    CapacityRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityRequestStatus) {
        if (!canMoveTo(next)) {
            error("capacity request $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedVolume(): Double = round((attempts.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): CapacityRequest {
        return CapacityRequest(
            code = code,
            title = title,
            pallets = pallets,
            vehicleId = vehicleId,
            attempts = attempts,
            stopCount = stopCount,
            category = category,
            depot = depot,
            durationMinutes = durationMinutes,
            endsAt = endsAt,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capacity request in status proposed. */
fun newCapacityRequest(code: String, title: String): CapacityRequest {
    return CapacityRequest(
        code = code,
        title = title,
        pallets = 0,
        vehicleId = "",
        attempts = 0,
        stopCount = 0,
        category = "",
        depot = "",
        durationMinutes = 0,
        endsAt = 0,
        ratio = 0.0,
        status = CapacityRequestStatus.PROPOSED,
    )
}
