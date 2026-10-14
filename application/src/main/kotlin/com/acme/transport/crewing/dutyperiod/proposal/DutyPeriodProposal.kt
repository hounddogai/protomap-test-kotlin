package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The duty period proposal records of crewing's duty period work, and the rules, stores, and calculations
// around them.

/** Where a duty period proposal is in its life cycle. */
enum class DutyPeriodProposalStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One duty period proposal, with what the planners need to act on it. */
data class DutyPeriodProposal(
    var code: String,
    var title: String,
    var stopCount: Int,
    var amount: Double,
    var category: String,
    var quantity: Int,
    var status: DutyPeriodProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodProposalStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodProposalStatus): Boolean {
        when (status) {
            DutyPeriodProposalStatus.PROPOSED -> return next == DutyPeriodProposalStatus.WITHDRAWN
            DutyPeriodProposalStatus.APPROVED -> {
                when (next) {
                    DutyPeriodProposalStatus.REJECTED -> return true
                }
                return false
            }
            DutyPeriodProposalStatus.REJECTED -> return next == DutyPeriodProposalStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DutyPeriodProposalStatus) {
        if (!canMoveTo(next)) {
            error("duty period proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveTotal(): Double = round(quantity.toDouble() * amount * 100) / 100

    fun snapshot(): DutyPeriodProposal {
        return DutyPeriodProposal(
            code = code,
            title = title,
            stopCount = stopCount,
            amount = amount,
            category = category,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a duty period proposal in status proposed. */
fun newDutyPeriodProposal(code: String, title: String): DutyPeriodProposal {
    return DutyPeriodProposal(
        code = code,
        title = title,
        stopCount = 0,
        amount = 0.0,
        category = "",
        quantity = 0,
        status = DutyPeriodProposalStatus.PROPOSED,
    )
}
