package com.acme.transport.fleetops.tire.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending tire target records of fleetops's tire work, and the rules, stores, and calculations around
// them.

/** Where a pending tire target is in its life cycle. */
enum class PendingTireTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A pending tire target as the tire team keeps it. */
data class PendingTireTarget(
    var code: String,
    var label: String,
    var description: String,
    var trailerId: String,
    var createdAt: Int,
    var archived: Boolean,
    var budget: Double,
    var rate: Double,
    var status: PendingTireTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingTireTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingTireTargetStatus): Boolean {
        when (status) {
            PendingTireTargetStatus.PLANNED -> {
                when (next) {
                    PendingTireTargetStatus.IN_PROGRESS, PendingTireTargetStatus.COMPLETED -> return true
                }
                return false
            }
            PendingTireTargetStatus.SCHEDULED -> return next == PendingTireTargetStatus.COMPLETED
            PendingTireTargetStatus.IN_PROGRESS -> {
                when (next) {
                    PendingTireTargetStatus.COMPLETED, PendingTireTargetStatus.CANCELLED -> return true
                }
                return false
            }
            PendingTireTargetStatus.COMPLETED -> return next == PendingTireTargetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingTireTargetStatus) {
        if (!canMoveTo(next)) {
            error("pending tire target $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"pending tire target"} $code"

    fun projectedExposure(): Double = round(rate * createdAt.toDouble() * 100) / 100

    fun snapshot(): PendingTireTarget {
        return PendingTireTarget(
            code = code,
            label = label,
            description = description,
            trailerId = trailerId,
            createdAt = createdAt,
            archived = archived,
            budget = budget,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending tire target in status planned. */
fun newPendingTireTarget(code: String, label: String): PendingTireTarget {
    return PendingTireTarget(
        code = code,
        label = label,
        description = "",
        trailerId = "",
        createdAt = 0,
        archived = false,
        budget = 0.0,
        rate = 0.0,
        status = PendingTireTargetStatus.PLANNED,
    )
}
