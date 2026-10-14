package com.acme.transport.lastmile.locker.slot

import com.acme.transport.kit.text.normalize

/** Where a weighted locker segment is in its life cycle. */
enum class WeightedLockerSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted locker segment. Its code is unique within a tenant. */
data class WeightedLockerSegment(
    var code: String,
    var name: String,
    var carrier: String,
    var description: String,
    var notes: String,
    var vehicleId: String,
    var score: Double,
    var status: WeightedLockerSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedLockerSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedLockerSegmentStatus): Boolean {
        when (status) {
            WeightedLockerSegmentStatus.PLANNED -> {
                when (next) {
                    WeightedLockerSegmentStatus.COMPLETED -> return true
                }
                return false
            }
            WeightedLockerSegmentStatus.SCHEDULED -> {
                when (next) {
                    WeightedLockerSegmentStatus.COMPLETED, WeightedLockerSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedLockerSegmentStatus.IN_PROGRESS -> return next == WeightedLockerSegmentStatus.COMPLETED || next == WeightedLockerSegmentStatus.CANCELLED
            WeightedLockerSegmentStatus.COMPLETED -> return next == WeightedLockerSegmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: WeightedLockerSegmentStatus) {
        if (!canMoveTo(next)) {
            error("weighted locker segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = score

    fun snapshot(): WeightedLockerSegment {
        return WeightedLockerSegment(
            code = code,
            name = name,
            carrier = carrier,
            description = description,
            notes = notes,
            vehicleId = vehicleId,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted locker segment in status planned. */
fun newWeightedLockerSegment(code: String, name: String): WeightedLockerSegment {
    return WeightedLockerSegment(
        code = code,
        name = name,
        carrier = "",
        description = "",
        notes = "",
        vehicleId = "",
        score = 0.0,
        status = WeightedLockerSegmentStatus.PLANNED,
    )
}
