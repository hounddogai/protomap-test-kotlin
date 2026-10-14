package com.acme.transport.linehaul.backhaul.backhaultarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict backhaul target records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a strict backhaul target is in its life cycle. */
enum class StrictBackhaulTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A strict backhaul target as the backhaul team keeps it. */
data class StrictBackhaulTarget(
    var code: String,
    var title: String,
    var archived: Boolean,
    var capacity: Int,
    var sequence: Int,
    var attempts: Int,
    var endsAt: Int,
    var weight: Double,
    var status: StrictBackhaulTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictBackhaulTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictBackhaulTargetStatus): Boolean {
        when (status) {
            StrictBackhaulTargetStatus.PLANNED -> return next == StrictBackhaulTargetStatus.IN_PROGRESS || next == StrictBackhaulTargetStatus.COMPLETED
            StrictBackhaulTargetStatus.SCHEDULED -> {
                when (next) {
                    StrictBackhaulTargetStatus.IN_PROGRESS -> return true
                }
                return false
            }
            StrictBackhaulTargetStatus.IN_PROGRESS -> return next == StrictBackhaulTargetStatus.CANCELLED
            StrictBackhaulTargetStatus.COMPLETED -> return next == StrictBackhaulTargetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: StrictBackhaulTargetStatus) {
        if (!canMoveTo(next)) {
            error("strict backhaul target $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"strict backhaul target"} $code"

    fun effectiveValue(): Double = round((sequence.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): StrictBackhaulTarget {
        return StrictBackhaulTarget(
            code = code,
            title = title,
            archived = archived,
            capacity = capacity,
            sequence = sequence,
            attempts = attempts,
            endsAt = endsAt,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict backhaul target in status planned. */
fun newStrictBackhaulTarget(code: String, title: String): StrictBackhaulTarget {
    return StrictBackhaulTarget(
        code = code,
        title = title,
        archived = false,
        capacity = 0,
        sequence = 0,
        attempts = 0,
        endsAt = 0,
        weight = 0.0,
        status = StrictBackhaulTargetStatus.PLANNED,
    )
}
