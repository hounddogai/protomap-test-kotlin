package com.acme.transport.terminal.dockdoor.window

import com.acme.transport.kit.text.normalize

/** Where a dock door baseline is in its life cycle. */
enum class DockDoorBaselineStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A dock door baseline as the dock door team keeps it. */
data class DockDoorBaseline(
    var code: String,
    var label: String,
    var segment: String,
    var reviewer: String,
    var carrier: String,
    var description: String,
    var visible: Boolean,
    var sequence: Int,
    var contactEmail: String,
    var status: DockDoorBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorBaselineStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorBaselineStatus): Boolean {
        when (status) {
            DockDoorBaselineStatus.OPEN -> {
                when (next) {
                    DockDoorBaselineStatus.REOPENED -> return true
                }
                return false
            }
            DockDoorBaselineStatus.IN_REVIEW -> return next == DockDoorBaselineStatus.RESOLVED || next == DockDoorBaselineStatus.REOPENED
            DockDoorBaselineStatus.RESOLVED -> {
                when (next) {
                    DockDoorBaselineStatus.REOPENED, DockDoorBaselineStatus.ARCHIVED -> return true
                }
                return false
            }
            DockDoorBaselineStatus.REOPENED -> {
                when (next) {
                    DockDoorBaselineStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DockDoorBaselineStatus) {
        if (!canMoveTo(next)) {
            error("dock door baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): DockDoorBaseline {
        return DockDoorBaseline(
            code = code,
            label = label,
            segment = segment,
            reviewer = reviewer,
            carrier = carrier,
            description = description,
            visible = visible,
            sequence = sequence,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a dock door baseline in status open. */
fun newDockDoorBaseline(code: String, label: String): DockDoorBaseline {
    return DockDoorBaseline(
        code = code,
        label = label,
        segment = "",
        reviewer = "",
        carrier = "",
        description = "",
        visible = false,
        sequence = 0,
        contactEmail = "",
        status = DockDoorBaselineStatus.OPEN,
    )
}
