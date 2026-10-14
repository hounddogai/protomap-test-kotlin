package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual scorecard proposal records of safety's scorecard work, and the rules, stores, and calculations
// around them.

/** Where a manual scorecard proposal is in its life cycle. */
enum class ManualScorecardProposalStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a manual scorecard proposal and its current status. */
data class ManualScorecardProposal(
    var code: String,
    var label: String,
    var contactEmail: String,
    var capacity: Int,
    var limit: Int,
    var status: ManualScorecardProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualScorecardProposalStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualScorecardProposalStatus): Boolean {
        when (status) {
            ManualScorecardProposalStatus.NEW -> return next == ManualScorecardProposalStatus.RUNNING
            ManualScorecardProposalStatus.QUEUED -> {
                when (next) {
                    ManualScorecardProposalStatus.DONE, ManualScorecardProposalStatus.FAILED -> return true
                }
                return false
            }
            ManualScorecardProposalStatus.RUNNING -> return next == ManualScorecardProposalStatus.DONE || next == ManualScorecardProposalStatus.FAILED
            ManualScorecardProposalStatus.DONE -> {
                when (next) {
                    ManualScorecardProposalStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualScorecardProposalStatus) {
        if (!canMoveTo(next)) {
            error("manual scorecard proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedExposure(): Double = round(limit.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): ManualScorecardProposal {
        return ManualScorecardProposal(
            code = code,
            label = label,
            contactEmail = contactEmail,
            capacity = capacity,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual scorecard proposal in status new. */
fun newManualScorecardProposal(code: String, label: String): ManualScorecardProposal {
    return ManualScorecardProposal(
        code = code,
        label = label,
        contactEmail = "",
        capacity = 0,
        limit = 0,
        status = ManualScorecardProposalStatus.NEW,
    )
}
