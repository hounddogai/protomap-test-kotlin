package com.acme.transport.fleetops.tire.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tire milestone records of fleetops's tire work, and the rules, stores, and calculations around them.

/** Where a tire milestone is in its life cycle. */
enum class TireMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a tire milestone and its current status. */
data class TireMilestone(
    var code: String,
    var title: String,
    var source: String,
    var notes: String,
    var createdAt: Int,
    var threshold: Double,
    var weight: Double,
    var priority: Int,
    var attempts: Int,
    var status: TireMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireMilestoneStatus): Boolean {
        when (status) {
            TireMilestoneStatus.REQUESTED -> return next == TireMilestoneStatus.REVOKED
            TireMilestoneStatus.GRANTED -> {
                when (next) {
                    TireMilestoneStatus.REVOKED, TireMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
            TireMilestoneStatus.REVOKED -> return next == TireMilestoneStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TireMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("tire milestone $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tire milestone"} $code"

    fun adjustedTotal(): Double = round((priority.toDouble() - threshold) * 100) / 100

    fun snapshot(): TireMilestone {
        return TireMilestone(
            code = code,
            title = title,
            source = source,
            notes = notes,
            createdAt = createdAt,
            threshold = threshold,
            weight = weight,
            priority = priority,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tire milestone in status requested. */
fun newTireMilestone(code: String, title: String): TireMilestone {
    return TireMilestone(
        code = code,
        title = title,
        source = "",
        notes = "",
        createdAt = 0,
        threshold = 0.0,
        weight = 0.0,
        priority = 0,
        attempts = 0,
        status = TireMilestoneStatus.REQUESTED,
    )
}
