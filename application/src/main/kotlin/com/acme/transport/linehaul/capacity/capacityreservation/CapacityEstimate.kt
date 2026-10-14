package com.acme.transport.linehaul.capacity.capacityreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity estimate is in its life cycle. */
enum class CapacityEstimateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A capacity estimate. Its code is unique within a tenant. */
data class CapacityEstimate(
    var code: String,
    var title: String,
    var visible: Boolean,
    var pallets: Int,
    var ratio: Double,
    var channel: String,
    var sequence: Int,
    var updatedAt: Int,
    var ceiling: Double,
    var ownerId: String,
    var floorValue: Double,
    var status: CapacityEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityEstimateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityEstimateStatus): Boolean {
        when (status) {
            CapacityEstimateStatus.CREATED -> return next == CapacityEstimateStatus.VALIDATED || next == CapacityEstimateStatus.RETIRED
            CapacityEstimateStatus.VALIDATED -> {
                when (next) {
                    CapacityEstimateStatus.PUBLISHED, CapacityEstimateStatus.RETIRED -> return true
                }
                return false
            }
            CapacityEstimateStatus.PUBLISHED -> {
                when (next) {
                    CapacityEstimateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityEstimateStatus) {
        if (!canMoveTo(next)) {
            error("capacity estimate $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedTotal(): Double = round((ratio + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): CapacityEstimate {
        return CapacityEstimate(
            code = code,
            title = title,
            visible = visible,
            pallets = pallets,
            ratio = ratio,
            channel = channel,
            sequence = sequence,
            updatedAt = updatedAt,
            ceiling = ceiling,
            ownerId = ownerId,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a capacity estimate in status created. */
fun newCapacityEstimate(code: String, title: String): CapacityEstimate {
    return CapacityEstimate(
        code = code,
        title = title,
        visible = false,
        pallets = 0,
        ratio = 0.0,
        channel = "",
        sequence = 0,
        updatedAt = 0,
        ceiling = 0.0,
        ownerId = "",
        floorValue = 0.0,
        status = CapacityEstimateStatus.CREATED,
    )
}
