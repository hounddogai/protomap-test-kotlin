package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The drop proposal records of lastmile's drop work, and the rules, stores, and calculations around them.

/** Where a drop proposal is in its life cycle. */
enum class DropProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a drop proposal and its current status. */
data class DropProposal(
    var code: String,
    var label: String,
    var discount: Double,
    var score: Double,
    var source: String,
    var pinned: Boolean,
    var budget: Double,
    var status: DropProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropProposalStatus): Boolean {
        when (status) {
            DropProposalStatus.OPEN -> return next == DropProposalStatus.RESOLVED || next == DropProposalStatus.ARCHIVED
            DropProposalStatus.IN_REVIEW -> {
                when (next) {
                    DropProposalStatus.RESOLVED -> return true
                }
                return false
            }
            DropProposalStatus.RESOLVED -> {
                when (next) {
                    DropProposalStatus.REOPENED, DropProposalStatus.ARCHIVED -> return true
                }
                return false
            }
            DropProposalStatus.REOPENED -> return next == DropProposalStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DropProposalStatus) {
        if (!canMoveTo(next)) {
            error("drop proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedLoad(): Double = round((discount + budget) * 100) / 100

    fun snapshot(): DropProposal {
        return DropProposal(
            code = code,
            label = label,
            discount = discount,
            score = score,
            source = source,
            pinned = pinned,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a drop proposal in status open. */
fun newDropProposal(code: String, label: String): DropProposal {
    return DropProposal(
        code = code,
        label = label,
        discount = 0.0,
        score = 0.0,
        source = "",
        pinned = false,
        budget = 0.0,
        status = DropProposalStatus.OPEN,
    )
}
