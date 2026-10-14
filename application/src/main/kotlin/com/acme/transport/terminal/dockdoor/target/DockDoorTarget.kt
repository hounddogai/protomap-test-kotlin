package com.acme.transport.terminal.dockdoor.target

import com.acme.transport.kit.text.normalize

// The dock door target records of terminal's dock door work, and the rules, stores, and calculations around
// them.

/** Where a dock door target is in its life cycle. */
enum class DockDoorTargetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One dock door target, with what the planners need to act on it. */
data class DockDoorTarget(
    var code: String,
    var name: String,
    var dock: String,
    var visible: Boolean,
    var checksum: String,
    var quantity: Int,
    var country: String,
    var summary: String,
    var status: DockDoorTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorTargetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorTargetStatus): Boolean {
        when (status) {
            DockDoorTargetStatus.NEW -> return next == DockDoorTargetStatus.RUNNING || next == DockDoorTargetStatus.FAILED
            DockDoorTargetStatus.QUEUED -> {
                when (next) {
                    DockDoorTargetStatus.RUNNING, DockDoorTargetStatus.FAILED -> return true
                }
                return false
            }
            DockDoorTargetStatus.RUNNING -> {
                when (next) {
                    DockDoorTargetStatus.FAILED -> return true
                }
                return false
            }
            DockDoorTargetStatus.DONE -> return next == DockDoorTargetStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DockDoorTargetStatus) {
        if (!canMoveTo(next)) {
            error("dock door target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"dock door target"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): DockDoorTarget {
        return DockDoorTarget(
            code = code,
            name = name,
            dock = dock,
            visible = visible,
            checksum = checksum,
            quantity = quantity,
            country = country,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a dock door target in status new. */
fun newDockDoorTarget(code: String, name: String): DockDoorTarget {
    return DockDoorTarget(
        code = code,
        name = name,
        dock = "",
        visible = false,
        checksum = "",
        quantity = 0,
        country = "",
        summary = "",
        status = DockDoorTargetStatus.NEW,
    )
}
