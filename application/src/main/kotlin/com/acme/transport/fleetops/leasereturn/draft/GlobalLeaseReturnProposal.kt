package com.acme.transport.fleetops.leasereturn.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global lease return proposal is in its life cycle. */
enum class GlobalLeaseReturnProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A global lease return proposal. Its code is unique within a tenant. */
data class GlobalLeaseReturnProposal(
    var code: String,
    var label: String,
    var name: String,
    var limit: Int,
    var quantity: Int,
    var sequence: Int,
    var status: GlobalLeaseReturnProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalLeaseReturnProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalLeaseReturnProposalStatus): Boolean {
        when (status) {
            GlobalLeaseReturnProposalStatus.OPEN -> {
                when (next) {
                    GlobalLeaseReturnProposalStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalLeaseReturnProposalStatus.IN_REVIEW -> {
                when (next) {
                    GlobalLeaseReturnProposalStatus.RESOLVED, GlobalLeaseReturnProposalStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalLeaseReturnProposalStatus.RESOLVED -> {
                when (next) {
                    GlobalLeaseReturnProposalStatus.REOPENED, GlobalLeaseReturnProposalStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalLeaseReturnProposalStatus.REOPENED -> {
                when (next) {
                    GlobalLeaseReturnProposalStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalLeaseReturnProposalStatus) {
        if (!canMoveTo(next)) {
            error("global lease return proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netValue(): Double = round(limit.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): GlobalLeaseReturnProposal {
        return GlobalLeaseReturnProposal(
            code = code,
            label = label,
            name = name,
            limit = limit,
            quantity = quantity,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global lease return proposal in status open. */
fun newGlobalLeaseReturnProposal(code: String, label: String): GlobalLeaseReturnProposal {
    return GlobalLeaseReturnProposal(
        code = code,
        label = label,
        name = "",
        limit = 0,
        quantity = 0,
        sequence = 0,
        status = GlobalLeaseReturnProposalStatus.OPEN,
    )
}
