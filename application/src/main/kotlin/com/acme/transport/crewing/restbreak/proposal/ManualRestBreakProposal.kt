package com.acme.transport.crewing.restbreak.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual rest break proposal records of crewing's rest break work, and the rules, stores, and
// calculations around them.

/** Where a manual rest break proposal is in its life cycle. */
enum class ManualRestBreakProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One manual rest break proposal, with what the planners need to act on it. */
data class ManualRestBreakProposal(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var revision: Int,
    var dock: String,
    var status: ManualRestBreakProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRestBreakProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRestBreakProposalStatus): Boolean {
        when (status) {
            ManualRestBreakProposalStatus.OPEN -> return next == ManualRestBreakProposalStatus.IN_REVIEW || next == ManualRestBreakProposalStatus.REOPENED
            ManualRestBreakProposalStatus.IN_REVIEW -> {
                when (next) {
                    ManualRestBreakProposalStatus.RESOLVED, ManualRestBreakProposalStatus.REOPENED -> return true
                }
                return false
            }
            ManualRestBreakProposalStatus.RESOLVED -> {
                when (next) {
                    ManualRestBreakProposalStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualRestBreakProposalStatus.REOPENED -> {
                when (next) {
                    ManualRestBreakProposalStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualRestBreakProposalStatus) {
        if (!canMoveTo(next)) {
            error("manual rest break proposal $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun weightedTotal(): Double = round((etaMinutes.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): ManualRestBreakProposal {
        return ManualRestBreakProposal(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            revision = revision,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual rest break proposal in status open. */
fun newManualRestBreakProposal(code: String, title: String): ManualRestBreakProposal {
    return ManualRestBreakProposal(
        code = code,
        title = title,
        etaMinutes = 0,
        revision = 0,
        dock = "",
        status = ManualRestBreakProposalStatus.OPEN,
    )
}
