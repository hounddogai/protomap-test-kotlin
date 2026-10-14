package com.acme.transport.network.transittime.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The transit time proposal records of network's transit time work, and the rules, stores, and calculations
// around them.

/** Where a transit time proposal is in its life cycle. */
enum class TransitTimeProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A transit time proposal. Its code is unique within a tenant. */
data class TransitTimeProposal(
    var code: String,
    var name: String,
    var budget: Double,
    var discount: Double,
    var externalRef: String,
    var etaMinutes: Int,
    var trailerId: String,
    var status: TransitTimeProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeProposalStatus): Boolean {
        when (status) {
            TransitTimeProposalStatus.REQUESTED -> return next == TransitTimeProposalStatus.GRANTED
            TransitTimeProposalStatus.GRANTED -> return next == TransitTimeProposalStatus.EXPIRED
            TransitTimeProposalStatus.REVOKED -> {
                when (next) {
                    TransitTimeProposalStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimeProposalStatus) {
        if (!canMoveTo(next)) {
            error("transit time proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"transit time proposal"} $code"

    fun netTotal(): Double = round(discount * budget * 100) / 100

    fun snapshot(): TransitTimeProposal {
        return TransitTimeProposal(
            code = code,
            name = name,
            budget = budget,
            discount = discount,
            externalRef = externalRef,
            etaMinutes = etaMinutes,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a transit time proposal in status requested. */
fun newTransitTimeProposal(code: String, name: String): TransitTimeProposal {
    return TransitTimeProposal(
        code = code,
        name = name,
        budget = 0.0,
        discount = 0.0,
        externalRef = "",
        etaMinutes = 0,
        trailerId = "",
        status = TransitTimeProposalStatus.REQUESTED,
    )
}
