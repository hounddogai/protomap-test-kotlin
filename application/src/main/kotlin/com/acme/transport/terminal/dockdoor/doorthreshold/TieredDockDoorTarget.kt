package com.acme.transport.terminal.dockdoor.doorthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered dock door target is in its life cycle. */
enum class TieredDockDoorTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a tiered dock door target and its current status. */
data class TieredDockDoorTarget(
    var code: String,
    var label: String,
    var priority: Int,
    var archived: Boolean,
    var version: Int,
    var capacity: Int,
    var status: TieredDockDoorTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDockDoorTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDockDoorTargetStatus): Boolean {
        when (status) {
            TieredDockDoorTargetStatus.CREATED -> return next == TieredDockDoorTargetStatus.VALIDATED
            TieredDockDoorTargetStatus.VALIDATED -> return next == TieredDockDoorTargetStatus.PUBLISHED
            TieredDockDoorTargetStatus.PUBLISHED -> return next == TieredDockDoorTargetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TieredDockDoorTargetStatus) {
        if (!canMoveTo(next)) {
            error("tiered dock door target $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedTotal(): Double = round((capacity.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): TieredDockDoorTarget {
        return TieredDockDoorTarget(
            code = code,
            label = label,
            priority = priority,
            archived = archived,
            version = version,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered dock door target in status created. */
fun newTieredDockDoorTarget(code: String, label: String): TieredDockDoorTarget {
    return TieredDockDoorTarget(
        code = code,
        label = label,
        priority = 0,
        archived = false,
        version = 0,
        capacity = 0,
        status = TieredDockDoorTargetStatus.CREATED,
    )
}
