package com.acme.transport.network.hub.proposal

import com.acme.transport.kit.text.normalize

// The default hub proposal records of network's hub work, and the rules, stores, and calculations around
// them.

/** Where a default hub proposal is in its life cycle. */
enum class DefaultHubProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a default hub proposal and its current status. */
data class DefaultHubProposal(
    var code: String,
    var title: String,
    var lane: String,
    var shiftCode: String,
    var startsAt: Int,
    var carrier: String,
    var pinned: Boolean,
    var visible: Boolean,
    var status: DefaultHubProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultHubProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultHubProposalStatus): Boolean {
        when (status) {
            DefaultHubProposalStatus.REQUESTED -> return next == DefaultHubProposalStatus.GRANTED
            DefaultHubProposalStatus.GRANTED -> {
                when (next) {
                    DefaultHubProposalStatus.REVOKED, DefaultHubProposalStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultHubProposalStatus.REVOKED -> return next == DefaultHubProposalStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefaultHubProposalStatus) {
        if (!canMoveTo(next)) {
            error("default hub proposal $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): DefaultHubProposal {
        return DefaultHubProposal(
            code = code,
            title = title,
            lane = lane,
            shiftCode = shiftCode,
            startsAt = startsAt,
            carrier = carrier,
            pinned = pinned,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default hub proposal in status requested. */
fun newDefaultHubProposal(code: String, title: String): DefaultHubProposal {
    return DefaultHubProposal(
        code = code,
        title = title,
        lane = "",
        shiftCode = "",
        startsAt = 0,
        carrier = "",
        pinned = false,
        visible = false,
        status = DefaultHubProposalStatus.REQUESTED,
    )
}
