package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.kit.text.normalize

/** Where a adaptive capacity proposal is in its life cycle. */
enum class AdaptiveCapacityProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A adaptive capacity proposal as the capacity team keeps it. */
data class AdaptiveCapacityProposal(
    var code: String,
    var name: String,
    var notes: String,
    var phone: String,
    var region: String,
    var amount: Double,
    var status: AdaptiveCapacityProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCapacityProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCapacityProposalStatus): Boolean {
        when (status) {
            AdaptiveCapacityProposalStatus.REQUESTED -> {
                when (next) {
                    AdaptiveCapacityProposalStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveCapacityProposalStatus.GRANTED -> return next == AdaptiveCapacityProposalStatus.REVOKED || next == AdaptiveCapacityProposalStatus.EXPIRED
            AdaptiveCapacityProposalStatus.REVOKED -> return next == AdaptiveCapacityProposalStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveCapacityProposalStatus) {
        if (!canMoveTo(next)) {
            error("adaptive capacity proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = amount

    fun snapshot(): AdaptiveCapacityProposal {
        return AdaptiveCapacityProposal(
            code = code,
            name = name,
            notes = notes,
            phone = phone,
            region = region,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a adaptive capacity proposal in status requested. */
fun newAdaptiveCapacityProposal(code: String, name: String): AdaptiveCapacityProposal {
    return AdaptiveCapacityProposal(
        code = code,
        name = name,
        notes = "",
        phone = "",
        region = "",
        amount = 0.0,
        status = AdaptiveCapacityProposalStatus.REQUESTED,
    )
}
