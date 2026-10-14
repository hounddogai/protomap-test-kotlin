package com.acme.transport.terminal.dwell.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global dwell proposal is in its life cycle. */
enum class GlobalDwellProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One global dwell proposal, with what the planners need to act on it. */
data class GlobalDwellProposal(
    var code: String,
    var title: String,
    var maximum: Double,
    var amount: Double,
    var routeCode: String,
    var timezone: String,
    var status: GlobalDwellProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDwellProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDwellProposalStatus): Boolean {
        when (status) {
            GlobalDwellProposalStatus.REQUESTED -> return next == GlobalDwellProposalStatus.GRANTED || next == GlobalDwellProposalStatus.EXPIRED
            GlobalDwellProposalStatus.GRANTED -> {
                when (next) {
                    GlobalDwellProposalStatus.REVOKED -> return true
                }
                return false
            }
            GlobalDwellProposalStatus.REVOKED -> return next == GlobalDwellProposalStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: GlobalDwellProposalStatus) {
        if (!canMoveTo(next)) {
            error("global dwell proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedTotal(): Double = round((amount - maximum) * 100) / 100

    fun snapshot(): GlobalDwellProposal {
        return GlobalDwellProposal(
            code = code,
            title = title,
            maximum = maximum,
            amount = amount,
            routeCode = routeCode,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global dwell proposal in status requested. */
fun newGlobalDwellProposal(code: String, title: String): GlobalDwellProposal {
    return GlobalDwellProposal(
        code = code,
        title = title,
        maximum = 0.0,
        amount = 0.0,
        routeCode = "",
        timezone = "",
        status = GlobalDwellProposalStatus.REQUESTED,
    )
}
