package com.acme.transport.emissions.idling.proposal

import com.acme.transport.kit.text.normalize

// The seasonal idling proposal records of emissions's idling work, and the rules, stores, and calculations
// around them.

/** Where a seasonal idling proposal is in its life cycle. */
enum class SeasonalIdlingProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A seasonal idling proposal as the idling team keeps it. */
data class SeasonalIdlingProposal(
    var code: String,
    var name: String,
    var cost: Double,
    var ownerId: String,
    var currency: String,
    var visible: Boolean,
    var status: SeasonalIdlingProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalIdlingProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalIdlingProposalStatus): Boolean {
        when (status) {
            SeasonalIdlingProposalStatus.OPEN -> {
                when (next) {
                    SeasonalIdlingProposalStatus.REOPENED, SeasonalIdlingProposalStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalIdlingProposalStatus.IN_REVIEW -> return next == SeasonalIdlingProposalStatus.RESOLVED
            SeasonalIdlingProposalStatus.RESOLVED -> return next == SeasonalIdlingProposalStatus.REOPENED
            SeasonalIdlingProposalStatus.REOPENED -> return next == SeasonalIdlingProposalStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: SeasonalIdlingProposalStatus) {
        if (!canMoveTo(next)) {
            error("seasonal idling proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = cost

    fun snapshot(): SeasonalIdlingProposal {
        return SeasonalIdlingProposal(
            code = code,
            name = name,
            cost = cost,
            ownerId = ownerId,
            currency = currency,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal idling proposal in status open. */
fun newSeasonalIdlingProposal(code: String, name: String): SeasonalIdlingProposal {
    return SeasonalIdlingProposal(
        code = code,
        name = name,
        cost = 0.0,
        ownerId = "",
        currency = "",
        visible = false,
        status = SeasonalIdlingProposalStatus.OPEN,
    )
}
