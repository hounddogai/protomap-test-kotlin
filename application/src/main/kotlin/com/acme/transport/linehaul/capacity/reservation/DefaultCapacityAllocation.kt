package com.acme.transport.linehaul.capacity.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default capacity allocation is in its life cycle. */
enum class DefaultCapacityAllocationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a default capacity allocation and its current status. */
data class DefaultCapacityAllocation(
    var code: String,
    var name: String,
    var score: Double,
    var createdAt: Int,
    var reviewer: String,
    var floorValue: Double,
    var distanceKm: Double,
    var status: DefaultCapacityAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCapacityAllocationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCapacityAllocationStatus): Boolean {
        when (status) {
            DefaultCapacityAllocationStatus.PLANNED -> {
                when (next) {
                    DefaultCapacityAllocationStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultCapacityAllocationStatus.SCHEDULED -> {
                when (next) {
                    DefaultCapacityAllocationStatus.IN_PROGRESS, DefaultCapacityAllocationStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultCapacityAllocationStatus.IN_PROGRESS -> {
                when (next) {
                    DefaultCapacityAllocationStatus.COMPLETED, DefaultCapacityAllocationStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultCapacityAllocationStatus.COMPLETED -> {
                when (next) {
                    DefaultCapacityAllocationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultCapacityAllocationStatus) {
        if (!canMoveTo(next)) {
            error("default capacity allocation $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"default capacity allocation"} $code"

    fun netLoad(): Double = round((distanceKm + floorValue) * 100) / 100

    fun snapshot(): DefaultCapacityAllocation {
        return DefaultCapacityAllocation(
            code = code,
            name = name,
            score = score,
            createdAt = createdAt,
            reviewer = reviewer,
            floorValue = floorValue,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default capacity allocation in status planned. */
fun newDefaultCapacityAllocation(code: String, name: String): DefaultCapacityAllocation {
    return DefaultCapacityAllocation(
        code = code,
        name = name,
        score = 0.0,
        createdAt = 0,
        reviewer = "",
        floorValue = 0.0,
        distanceKm = 0.0,
        status = DefaultCapacityAllocationStatus.PLANNED,
    )
}
