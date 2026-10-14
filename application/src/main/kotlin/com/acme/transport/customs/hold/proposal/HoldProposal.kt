package com.acme.transport.customs.hold.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hold proposal records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a hold proposal is in its life cycle. */
enum class HoldProposalStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A hold proposal. Its code is unique within a tenant. */
data class HoldProposal(
    var code: String,
    var title: String,
    var driverId: String,
    var capacity: Int,
    var description: String,
    var discount: Double,
    var stopCount: Int,
    var budget: Double,
    var status: HoldProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldProposalStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldProposalStatus): Boolean {
        when (status) {
            HoldProposalStatus.PROPOSED -> {
                when (next) {
                    HoldProposalStatus.APPROVED, HoldProposalStatus.REJECTED -> return true
                }
                return false
            }
            HoldProposalStatus.APPROVED -> return next == HoldProposalStatus.REJECTED
            HoldProposalStatus.REJECTED -> {
                when (next) {
                    HoldProposalStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HoldProposalStatus) {
        if (!canMoveTo(next)) {
            error("hold proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveTotal(): Double = round((budget - stopCount.toDouble()) * 100) / 100

    fun snapshot(): HoldProposal {
        return HoldProposal(
            code = code,
            title = title,
            driverId = driverId,
            capacity = capacity,
            description = description,
            discount = discount,
            stopCount = stopCount,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a hold proposal in status proposed. */
fun newHoldProposal(code: String, title: String): HoldProposal {
    return HoldProposal(
        code = code,
        title = title,
        driverId = "",
        capacity = 0,
        description = "",
        discount = 0.0,
        stopCount = 0,
        budget = 0.0,
        status = HoldProposalStatus.PROPOSED,
    )
}
