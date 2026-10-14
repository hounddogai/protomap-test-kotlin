package com.acme.transport.safety.nearmiss.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped near miss proposal records of safety's near miss work, and the rules, stores, and calculations
// around them.

/** Where a capped near miss proposal is in its life cycle. */
enum class CappedNearMissProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped near miss proposal as the near miss team keeps it. */
data class CappedNearMissProposal(
    var code: String,
    var label: String,
    var limit: Int,
    var version: Int,
    var channel: String,
    var depot: String,
    var status: CappedNearMissProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedNearMissProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedNearMissProposalStatus): Boolean {
        when (status) {
            CappedNearMissProposalStatus.REQUESTED -> {
                when (next) {
                    CappedNearMissProposalStatus.REVOKED, CappedNearMissProposalStatus.EXPIRED -> return true
                }
                return false
            }
            CappedNearMissProposalStatus.GRANTED -> {
                when (next) {
                    CappedNearMissProposalStatus.REVOKED -> return true
                }
                return false
            }
            CappedNearMissProposalStatus.REVOKED -> {
                when (next) {
                    CappedNearMissProposalStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedNearMissProposalStatus) {
        if (!canMoveTo(next)) {
            error("capped near miss proposal $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedLoad(): Double = round((limit.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): CappedNearMissProposal {
        return CappedNearMissProposal(
            code = code,
            label = label,
            limit = limit,
            version = version,
            channel = channel,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a capped near miss proposal in status requested. */
fun newCappedNearMissProposal(code: String, label: String): CappedNearMissProposal {
    return CappedNearMissProposal(
        code = code,
        label = label,
        limit = 0,
        version = 0,
        channel = "",
        depot = "",
        status = CappedNearMissProposalStatus.REQUESTED,
    )
}
