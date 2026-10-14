package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.kit.text.normalize

/** Where a dock door group is in its life cycle. */
enum class DockDoorGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One dock door group, with what the planners need to act on it. */
data class DockDoorGroup(
    var code: String,
    var label: String,
    var revision: Int,
    var description: String,
    var reviewer: String,
    var status: DockDoorGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorGroupStatus): Boolean {
        when (status) {
            DockDoorGroupStatus.REQUESTED -> return next == DockDoorGroupStatus.REVOKED
            DockDoorGroupStatus.GRANTED -> {
                when (next) {
                    DockDoorGroupStatus.EXPIRED -> return true
                }
                return false
            }
            DockDoorGroupStatus.REVOKED -> {
                when (next) {
                    DockDoorGroupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DockDoorGroupStatus) {
        if (!canMoveTo(next)) {
            error("dock door group $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = revision.toDouble()

    fun snapshot(): DockDoorGroup {
        return DockDoorGroup(
            code = code,
            label = label,
            revision = revision,
            description = description,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a dock door group in status requested. */
fun newDockDoorGroup(code: String, label: String): DockDoorGroup {
    return DockDoorGroup(
        code = code,
        label = label,
        revision = 0,
        description = "",
        reviewer = "",
        status = DockDoorGroupStatus.REQUESTED,
    )
}
