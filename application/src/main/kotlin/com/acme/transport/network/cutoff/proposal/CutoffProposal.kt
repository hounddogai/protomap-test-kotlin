package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.text.normalize

// The cutoff proposal records of network's cutoff work, and the rules, stores, and calculations around them.

/** Where a cutoff proposal is in its life cycle. */
enum class CutoffProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A cutoff proposal as the cutoff team keeps it. */
data class CutoffProposal(
    var code: String,
    var label: String,
    var title: String,
    var approved: Boolean,
    var batchSize: Int,
    var status: CutoffProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffProposalStatus): Boolean {
        when (status) {
            CutoffProposalStatus.REQUESTED -> return next == CutoffProposalStatus.REVOKED || next == CutoffProposalStatus.EXPIRED
            CutoffProposalStatus.GRANTED -> return next == CutoffProposalStatus.REVOKED || next == CutoffProposalStatus.EXPIRED
            CutoffProposalStatus.REVOKED -> return next == CutoffProposalStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CutoffProposalStatus) {
        if (!canMoveTo(next)) {
            error("cutoff proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"cutoff proposal"} $code"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): CutoffProposal {
        return CutoffProposal(
            code = code,
            label = label,
            title = title,
            approved = approved,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a cutoff proposal in status requested. */
fun newCutoffProposal(code: String, label: String): CutoffProposal {
    return CutoffProposal(
        code = code,
        label = label,
        title = "",
        approved = false,
        batchSize = 0,
        status = CutoffProposalStatus.REQUESTED,
    )
}
