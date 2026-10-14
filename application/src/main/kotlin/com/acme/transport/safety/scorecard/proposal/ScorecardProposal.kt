package com.acme.transport.safety.scorecard.proposal

import com.acme.transport.kit.text.normalize

// The scorecard proposal records of safety's scorecard work, and the rules, stores, and calculations around
// them.

/** Where a scorecard proposal is in its life cycle. */
enum class ScorecardProposalStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scorecard proposal. Its code is unique within a tenant. */
data class ScorecardProposal(
    var code: String,
    var label: String,
    var reviewer: String,
    var externalRef: String,
    var lane: String,
    var etaMinutes: Int,
    var status: ScorecardProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardProposalStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardProposalStatus): Boolean {
        when (status) {
            ScorecardProposalStatus.DRAFT -> {
                when (next) {
                    ScorecardProposalStatus.SUSPENDED -> return true
                }
                return false
            }
            ScorecardProposalStatus.PENDING -> {
                when (next) {
                    ScorecardProposalStatus.ACTIVE -> return true
                }
                return false
            }
            ScorecardProposalStatus.ACTIVE -> {
                when (next) {
                    ScorecardProposalStatus.CLOSED -> return true
                }
                return false
            }
            ScorecardProposalStatus.SUSPENDED -> return next == ScorecardProposalStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScorecardProposalStatus) {
        if (!canMoveTo(next)) {
            error("scorecard proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scorecard proposal"} $code"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): ScorecardProposal {
        return ScorecardProposal(
            code = code,
            label = label,
            reviewer = reviewer,
            externalRef = externalRef,
            lane = lane,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a scorecard proposal in status draft. */
fun newScorecardProposal(code: String, label: String): ScorecardProposal {
    return ScorecardProposal(
        code = code,
        label = label,
        reviewer = "",
        externalRef = "",
        lane = "",
        etaMinutes = 0,
        status = ScorecardProposalStatus.DRAFT,
    )
}
