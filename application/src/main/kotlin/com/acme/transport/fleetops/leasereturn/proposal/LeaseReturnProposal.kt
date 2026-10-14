package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lease return proposal records of fleetops's lease return work, and the rules, stores, and calculations
// around them.

/** Where a lease return proposal is in its life cycle. */
enum class LeaseReturnProposalStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One lease return proposal, with what the planners need to act on it. */
data class LeaseReturnProposal(
    var code: String,
    var label: String,
    var createdAt: Int,
    var region: String,
    var active: Boolean,
    var limit: Int,
    var status: LeaseReturnProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnProposalStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnProposalStatus): Boolean {
        when (status) {
            LeaseReturnProposalStatus.PROPOSED -> return next == LeaseReturnProposalStatus.APPROVED || next == LeaseReturnProposalStatus.REJECTED
            LeaseReturnProposalStatus.APPROVED -> {
                when (next) {
                    LeaseReturnProposalStatus.REJECTED, LeaseReturnProposalStatus.WITHDRAWN -> return true
                }
                return false
            }
            LeaseReturnProposalStatus.REJECTED -> {
                when (next) {
                    LeaseReturnProposalStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnProposalStatus) {
        if (!canMoveTo(next)) {
            error("lease return proposal $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"lease return proposal"} $code"

    fun projectedVolume(): Double = round(limit.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): LeaseReturnProposal {
        return LeaseReturnProposal(
            code = code,
            label = label,
            createdAt = createdAt,
            region = region,
            active = active,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a lease return proposal in status proposed. */
fun newLeaseReturnProposal(code: String, label: String): LeaseReturnProposal {
    return LeaseReturnProposal(
        code = code,
        label = label,
        createdAt = 0,
        region = "",
        active = false,
        limit = 0,
        status = LeaseReturnProposalStatus.PROPOSED,
    )
}
