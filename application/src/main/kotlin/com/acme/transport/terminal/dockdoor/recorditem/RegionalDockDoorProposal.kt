package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional dock door proposal is in its life cycle. */
enum class RegionalDockDoorProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One regional dock door proposal, with what the planners need to act on it. */
data class RegionalDockDoorProposal(
    var code: String,
    var name: String,
    var channel: String,
    var etaMinutes: Int,
    var priority: Int,
    var segment: String,
    var status: RegionalDockDoorProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDockDoorProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDockDoorProposalStatus): Boolean {
        when (status) {
            RegionalDockDoorProposalStatus.REQUESTED -> return next == RegionalDockDoorProposalStatus.EXPIRED
            RegionalDockDoorProposalStatus.GRANTED -> return next == RegionalDockDoorProposalStatus.REVOKED || next == RegionalDockDoorProposalStatus.EXPIRED
            RegionalDockDoorProposalStatus.REVOKED -> return next == RegionalDockDoorProposalStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RegionalDockDoorProposalStatus) {
        if (!canMoveTo(next)) {
            error("regional dock door proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedExposure(): Double = round((etaMinutes.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): RegionalDockDoorProposal {
        return RegionalDockDoorProposal(
            code = code,
            name = name,
            channel = channel,
            etaMinutes = etaMinutes,
            priority = priority,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a regional dock door proposal in status requested. */
fun newRegionalDockDoorProposal(code: String, name: String): RegionalDockDoorProposal {
    return RegionalDockDoorProposal(
        code = code,
        name = name,
        channel = "",
        etaMinutes = 0,
        priority = 0,
        segment = "",
        status = RegionalDockDoorProposalStatus.REQUESTED,
    )
}
