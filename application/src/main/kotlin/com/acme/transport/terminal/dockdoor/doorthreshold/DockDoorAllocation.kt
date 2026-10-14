package com.acme.transport.terminal.dockdoor.doorthreshold

import com.acme.transport.kit.text.normalize

/** Where a dock door allocation is in its life cycle. */
enum class DockDoorAllocationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A dock door allocation. Its code is unique within a tenant. */
data class DockDoorAllocation(
    var code: String,
    var title: String,
    var priority: Int,
    var externalRef: String,
    var driverId: String,
    var status: DockDoorAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorAllocationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorAllocationStatus): Boolean {
        when (status) {
            DockDoorAllocationStatus.IDLE -> return next == DockDoorAllocationStatus.WARMING || next == DockDoorAllocationStatus.READY
            DockDoorAllocationStatus.WARMING -> {
                when (next) {
                    DockDoorAllocationStatus.READY, DockDoorAllocationStatus.DRAINING -> return true
                }
                return false
            }
            DockDoorAllocationStatus.READY -> {
                when (next) {
                    DockDoorAllocationStatus.DRAINING -> return true
                }
                return false
            }
            DockDoorAllocationStatus.DRAINING -> return next == DockDoorAllocationStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DockDoorAllocationStatus) {
        if (!canMoveTo(next)) {
            error("dock door allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"dock door allocation"} $code"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): DockDoorAllocation {
        return DockDoorAllocation(
            code = code,
            title = title,
            priority = priority,
            externalRef = externalRef,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a dock door allocation in status idle. */
fun newDockDoorAllocation(code: String, title: String): DockDoorAllocation {
    return DockDoorAllocation(
        code = code,
        title = title,
        priority = 0,
        externalRef = "",
        driverId = "",
        status = DockDoorAllocationStatus.IDLE,
    )
}
