package com.acme.transport.terminal.dockdoor.target

import com.acme.transport.kit.text.normalize

/** Where a dock door claim is in its life cycle. */
enum class DockDoorClaimStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A dock door claim as the dock door team keeps it. */
data class DockDoorClaim(
    var code: String,
    var title: String,
    var summary: String,
    var approved: Boolean,
    var etaMinutes: Int,
    var status: DockDoorClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorClaimStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorClaimStatus): Boolean {
        when (status) {
            DockDoorClaimStatus.IDLE -> return next == DockDoorClaimStatus.DRAINING || next == DockDoorClaimStatus.STOPPED
            DockDoorClaimStatus.WARMING -> {
                when (next) {
                    DockDoorClaimStatus.STOPPED -> return true
                }
                return false
            }
            DockDoorClaimStatus.READY -> {
                when (next) {
                    DockDoorClaimStatus.DRAINING, DockDoorClaimStatus.STOPPED -> return true
                }
                return false
            }
            DockDoorClaimStatus.DRAINING -> {
                when (next) {
                    DockDoorClaimStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DockDoorClaimStatus) {
        if (!canMoveTo(next)) {
            error("dock door claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): DockDoorClaim {
        return DockDoorClaim(
            code = code,
            title = title,
            summary = summary,
            approved = approved,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a dock door claim in status idle. */
fun newDockDoorClaim(code: String, title: String): DockDoorClaim {
    return DockDoorClaim(
        code = code,
        title = title,
        summary = "",
        approved = false,
        etaMinutes = 0,
        status = DockDoorClaimStatus.IDLE,
    )
}
