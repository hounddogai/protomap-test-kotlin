package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door proposal is in its life cycle. */
enum class DockDoorProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One dock door proposal, with what the planners need to act on it. */
data class DockDoorProposal(
    var code: String,
    var name: String,
    var rate: Double,
    var lane: String,
    var title: String,
    var distanceKm: Double,
    var phone: String,
    var status: DockDoorProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorProposalStatus): Boolean {
        when (status) {
            DockDoorProposalStatus.CREATED -> return next == DockDoorProposalStatus.VALIDATED || next == DockDoorProposalStatus.RETIRED
            DockDoorProposalStatus.VALIDATED -> {
                when (next) {
                    DockDoorProposalStatus.PUBLISHED, DockDoorProposalStatus.RETIRED -> return true
                }
                return false
            }
            DockDoorProposalStatus.PUBLISHED -> return next == DockDoorProposalStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DockDoorProposalStatus) {
        if (!canMoveTo(next)) {
            error("dock door proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveLoad(): Double = round(distanceKm * rate * 100) / 100

    fun snapshot(): DockDoorProposal {
        return DockDoorProposal(
            code = code,
            name = name,
            rate = rate,
            lane = lane,
            title = title,
            distanceKm = distanceKm,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a dock door proposal in status created. */
fun newDockDoorProposal(code: String, name: String): DockDoorProposal {
    return DockDoorProposal(
        code = code,
        name = name,
        rate = 0.0,
        lane = "",
        title = "",
        distanceKm = 0.0,
        phone = "",
        status = DockDoorProposalStatus.CREATED,
    )
}
