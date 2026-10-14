package com.acme.transport.safety.nearmiss.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict near miss proposal is in its life cycle. */
enum class StrictNearMissProposalStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A strict near miss proposal as the near miss team keeps it. */
data class StrictNearMissProposal(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var region: String,
    var reviewer: String,
    var pallets: Int,
    var weight: Double,
    var revision: Int,
    var source: String,
    var status: StrictNearMissProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictNearMissProposalStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictNearMissProposalStatus): Boolean {
        when (status) {
            StrictNearMissProposalStatus.PROPOSED -> return next == StrictNearMissProposalStatus.REJECTED
            StrictNearMissProposalStatus.APPROVED -> {
                when (next) {
                    StrictNearMissProposalStatus.WITHDRAWN -> return true
                }
                return false
            }
            StrictNearMissProposalStatus.REJECTED -> return next == StrictNearMissProposalStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: StrictNearMissProposalStatus) {
        if (!canMoveTo(next)) {
            error("strict near miss proposal $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"strict near miss proposal"} $code"

    fun weightedLoad(): Double = round((revision.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): StrictNearMissProposal {
        return StrictNearMissProposal(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            region = region,
            reviewer = reviewer,
            pallets = pallets,
            weight = weight,
            revision = revision,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a strict near miss proposal in status proposed. */
fun newStrictNearMissProposal(code: String, title: String): StrictNearMissProposal {
    return StrictNearMissProposal(
        code = code,
        title = title,
        fuelLiters = 0.0,
        region = "",
        reviewer = "",
        pallets = 0,
        weight = 0.0,
        revision = 0,
        source = "",
        status = StrictNearMissProposalStatus.PROPOSED,
    )
}
