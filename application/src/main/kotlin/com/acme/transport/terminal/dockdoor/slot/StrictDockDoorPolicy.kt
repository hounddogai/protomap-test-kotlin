package com.acme.transport.terminal.dockdoor.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict dock door policy is in its life cycle. */
enum class StrictDockDoorPolicyStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One strict dock door policy, with what the planners need to act on it. */
data class StrictDockDoorPolicy(
    var code: String,
    var name: String,
    var ratio: Double,
    var attempts: Int,
    var category: String,
    var distanceKm: Double,
    var priority: Int,
    var revision: Int,
    var status: StrictDockDoorPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDockDoorPolicyStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDockDoorPolicyStatus): Boolean {
        when (status) {
            StrictDockDoorPolicyStatus.OPEN -> return next == StrictDockDoorPolicyStatus.REOPENED
            StrictDockDoorPolicyStatus.IN_REVIEW -> return next == StrictDockDoorPolicyStatus.REOPENED || next == StrictDockDoorPolicyStatus.ARCHIVED
            StrictDockDoorPolicyStatus.RESOLVED -> {
                when (next) {
                    StrictDockDoorPolicyStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictDockDoorPolicyStatus.REOPENED -> return next == StrictDockDoorPolicyStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: StrictDockDoorPolicyStatus) {
        if (!canMoveTo(next)) {
            error("strict dock door policy $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"strict dock door policy"} $code"

    fun effectiveExposure(): Double = round(attempts.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): StrictDockDoorPolicy {
        return StrictDockDoorPolicy(
            code = code,
            name = name,
            ratio = ratio,
            attempts = attempts,
            category = category,
            distanceKm = distanceKm,
            priority = priority,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a strict dock door policy in status open. */
fun newStrictDockDoorPolicy(code: String, name: String): StrictDockDoorPolicy {
    return StrictDockDoorPolicy(
        code = code,
        name = name,
        ratio = 0.0,
        attempts = 0,
        category = "",
        distanceKm = 0.0,
        priority = 0,
        revision = 0,
        status = StrictDockDoorPolicyStatus.OPEN,
    )
}
