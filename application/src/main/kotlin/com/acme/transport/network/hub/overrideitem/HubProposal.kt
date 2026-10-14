package com.acme.transport.network.hub.overrideitem

import com.acme.transport.kit.text.normalize

/** Where a hub proposal is in its life cycle. */
enum class HubProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A hub proposal as the hub team keeps it. */
data class HubProposal(
    var code: String,
    var name: String,
    var notes: String,
    var odometer: Int,
    var locale: String,
    var segment: String,
    var pinned: Boolean,
    var archived: Boolean,
    var status: HubProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubProposalStatus): Boolean {
        when (status) {
            HubProposalStatus.CREATED -> {
                when (next) {
                    HubProposalStatus.VALIDATED -> return true
                }
                return false
            }
            HubProposalStatus.VALIDATED -> return next == HubProposalStatus.PUBLISHED || next == HubProposalStatus.RETIRED
            HubProposalStatus.PUBLISHED -> return next == HubProposalStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: HubProposalStatus) {
        if (!canMoveTo(next)) {
            error("hub proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): HubProposal {
        return HubProposal(
            code = code,
            name = name,
            notes = notes,
            odometer = odometer,
            locale = locale,
            segment = segment,
            pinned = pinned,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a hub proposal in status created. */
fun newHubProposal(code: String, name: String): HubProposal {
    return HubProposal(
        code = code,
        name = name,
        notes = "",
        odometer = 0,
        locale = "",
        segment = "",
        pinned = false,
        archived = false,
        status = HubProposalStatus.CREATED,
    )
}
