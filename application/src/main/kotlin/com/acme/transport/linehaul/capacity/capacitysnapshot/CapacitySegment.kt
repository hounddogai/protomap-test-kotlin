package com.acme.transport.linehaul.capacity.capacitysnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity segment is in its life cycle. */
enum class CapacitySegmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One capacity segment, with what the planners need to act on it. */
data class CapacitySegment(
    var code: String,
    var label: String,
    var description: String,
    var retries: Int,
    var lane: String,
    var floorValue: Double,
    var category: String,
    var capacity: Int,
    var unit: String,
    var margin: Double,
    var externalRef: String,
    var status: CapacitySegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacitySegmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacitySegmentStatus): Boolean {
        when (status) {
            CapacitySegmentStatus.PROPOSED -> return next == CapacitySegmentStatus.REJECTED
            CapacitySegmentStatus.APPROVED -> return next == CapacitySegmentStatus.REJECTED || next == CapacitySegmentStatus.WITHDRAWN
            CapacitySegmentStatus.REJECTED -> return next == CapacitySegmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CapacitySegmentStatus) {
        if (!canMoveTo(next)) {
            error("capacity segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"capacity segment"} $code"

    fun weightedTotal(): Double = round(capacity.toDouble() * floorValue * 100) / 100

    fun snapshot(): CapacitySegment {
        return CapacitySegment(
            code = code,
            label = label,
            description = description,
            retries = retries,
            lane = lane,
            floorValue = floorValue,
            category = category,
            capacity = capacity,
            unit = unit,
            margin = margin,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a capacity segment in status proposed. */
fun newCapacitySegment(code: String, label: String): CapacitySegment {
    return CapacitySegment(
        code = code,
        label = label,
        description = "",
        retries = 0,
        lane = "",
        floorValue = 0.0,
        category = "",
        capacity = 0,
        unit = "",
        margin = 0.0,
        externalRef = "",
        status = CapacitySegmentStatus.PROPOSED,
    )
}
