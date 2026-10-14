package com.acme.transport.network.node.template

import com.acme.transport.kit.text.normalize

/** Where a tiered node proposal is in its life cycle. */
enum class TieredNodeProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a tiered node proposal and its current status. */
data class TieredNodeProposal(
    var code: String,
    var label: String,
    var windowDays: Int,
    var visible: Boolean,
    var archived: Boolean,
    var summary: String,
    var status: TieredNodeProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredNodeProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredNodeProposalStatus): Boolean {
        when (status) {
            TieredNodeProposalStatus.OPEN -> return next == TieredNodeProposalStatus.IN_REVIEW || next == TieredNodeProposalStatus.RESOLVED
            TieredNodeProposalStatus.IN_REVIEW -> {
                when (next) {
                    TieredNodeProposalStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredNodeProposalStatus.RESOLVED -> return next == TieredNodeProposalStatus.REOPENED || next == TieredNodeProposalStatus.ARCHIVED
            TieredNodeProposalStatus.REOPENED -> {
                when (next) {
                    TieredNodeProposalStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredNodeProposalStatus) {
        if (!canMoveTo(next)) {
            error("tiered node proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): TieredNodeProposal {
        return TieredNodeProposal(
            code = code,
            label = label,
            windowDays = windowDays,
            visible = visible,
            archived = archived,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered node proposal in status open. */
fun newTieredNodeProposal(code: String, label: String): TieredNodeProposal {
    return TieredNodeProposal(
        code = code,
        label = label,
        windowDays = 0,
        visible = false,
        archived = false,
        summary = "",
        status = TieredNodeProposalStatus.OPEN,
    )
}
