package com.acme.transport.lastmile.drop.milestone

import com.acme.transport.kit.text.normalize

// The drop milestone records of lastmile's drop work, and the rules, stores, and calculations around them.

/** Where a drop milestone is in its life cycle. */
enum class DropMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a drop milestone and its current status. */
data class DropMilestone(
    var code: String,
    var name: String,
    var routeCode: String,
    var locale: String,
    var label: String,
    var driverId: String,
    var quantity: Int,
    var status: DropMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropMilestoneStatus): Boolean {
        when (status) {
            DropMilestoneStatus.IDLE -> {
                when (next) {
                    DropMilestoneStatus.READY -> return true
                }
                return false
            }
            DropMilestoneStatus.WARMING -> {
                when (next) {
                    DropMilestoneStatus.STOPPED -> return true
                }
                return false
            }
            DropMilestoneStatus.READY -> {
                when (next) {
                    DropMilestoneStatus.STOPPED -> return true
                }
                return false
            }
            DropMilestoneStatus.DRAINING -> {
                when (next) {
                    DropMilestoneStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("drop milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"drop milestone"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): DropMilestone {
        return DropMilestone(
            code = code,
            name = name,
            routeCode = routeCode,
            locale = locale,
            label = label,
            driverId = driverId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a drop milestone in status idle. */
fun newDropMilestone(code: String, name: String): DropMilestone {
    return DropMilestone(
        code = code,
        name = name,
        routeCode = "",
        locale = "",
        label = "",
        driverId = "",
        quantity = 0,
        status = DropMilestoneStatus.IDLE,
    )
}
