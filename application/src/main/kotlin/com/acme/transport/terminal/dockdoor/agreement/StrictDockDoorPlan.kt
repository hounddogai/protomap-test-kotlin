package com.acme.transport.terminal.dockdoor.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict dock door plan is in its life cycle. */
enum class StrictDockDoorPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A strict dock door plan. Its code is unique within a tenant. */
data class StrictDockDoorPlan(
    var code: String,
    var label: String,
    var ownerId: String,
    var lane: String,
    var priority: Int,
    var checksum: String,
    var approved: Boolean,
    var attempts: Int,
    var ratio: Double,
    var title: String,
    var createdAt: Int,
    var status: StrictDockDoorPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDockDoorPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDockDoorPlanStatus): Boolean {
        when (status) {
            StrictDockDoorPlanStatus.CREATED -> return next == StrictDockDoorPlanStatus.RETIRED
            StrictDockDoorPlanStatus.VALIDATED -> return next == StrictDockDoorPlanStatus.PUBLISHED || next == StrictDockDoorPlanStatus.RETIRED
            StrictDockDoorPlanStatus.PUBLISHED -> {
                when (next) {
                    StrictDockDoorPlanStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictDockDoorPlanStatus) {
        if (!canMoveTo(next)) {
            error("strict dock door plan $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"strict dock door plan"} $code"

    fun effectiveTotal(): Double = round(ratio * attempts.toDouble() * 100) / 100

    fun snapshot(): StrictDockDoorPlan {
        return StrictDockDoorPlan(
            code = code,
            label = label,
            ownerId = ownerId,
            lane = lane,
            priority = priority,
            checksum = checksum,
            approved = approved,
            attempts = attempts,
            ratio = ratio,
            title = title,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a strict dock door plan in status created. */
fun newStrictDockDoorPlan(code: String, label: String): StrictDockDoorPlan {
    return StrictDockDoorPlan(
        code = code,
        label = label,
        ownerId = "",
        lane = "",
        priority = 0,
        checksum = "",
        approved = false,
        attempts = 0,
        ratio = 0.0,
        title = "",
        createdAt = 0,
        status = StrictDockDoorPlanStatus.CREATED,
    )
}
