package com.acme.transport.lastmile.locker.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict locker target is in its life cycle. */
enum class StrictLockerTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A strict locker target as the locker team keeps it. */
data class StrictLockerTarget(
    var code: String,
    var title: String,
    var batchSize: Int,
    var capacity: Int,
    var unit: String,
    var sequence: Int,
    var status: StrictLockerTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictLockerTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictLockerTargetStatus): Boolean {
        when (status) {
            StrictLockerTargetStatus.PLANNED -> return next == StrictLockerTargetStatus.SCHEDULED || next == StrictLockerTargetStatus.CANCELLED
            StrictLockerTargetStatus.SCHEDULED -> {
                when (next) {
                    StrictLockerTargetStatus.COMPLETED, StrictLockerTargetStatus.CANCELLED -> return true
                }
                return false
            }
            StrictLockerTargetStatus.IN_PROGRESS -> return next == StrictLockerTargetStatus.COMPLETED || next == StrictLockerTargetStatus.CANCELLED
            StrictLockerTargetStatus.COMPLETED -> {
                when (next) {
                    StrictLockerTargetStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictLockerTargetStatus) {
        if (!canMoveTo(next)) {
            error("strict locker target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedVolume(): Double = round(capacity.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): StrictLockerTarget {
        return StrictLockerTarget(
            code = code,
            title = title,
            batchSize = batchSize,
            capacity = capacity,
            unit = unit,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a strict locker target in status planned. */
fun newStrictLockerTarget(code: String, title: String): StrictLockerTarget {
    return StrictLockerTarget(
        code = code,
        title = title,
        batchSize = 0,
        capacity = 0,
        unit = "",
        sequence = 0,
        status = StrictLockerTargetStatus.PLANNED,
    )
}
