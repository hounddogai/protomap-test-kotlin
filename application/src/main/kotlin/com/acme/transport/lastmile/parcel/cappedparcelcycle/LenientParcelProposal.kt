package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient parcel proposal is in its life cycle. */
enum class LenientParcelProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a lenient parcel proposal and its current status. */
data class LenientParcelProposal(
    var code: String,
    var name: String,
    var attempts: Int,
    var cost: Double,
    var vehicleId: String,
    var tags: MutableList<String>,
    var status: LenientParcelProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientParcelProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientParcelProposalStatus): Boolean {
        when (status) {
            LenientParcelProposalStatus.CREATED -> {
                when (next) {
                    LenientParcelProposalStatus.VALIDATED, LenientParcelProposalStatus.RETIRED -> return true
                }
                return false
            }
            LenientParcelProposalStatus.VALIDATED -> return next == LenientParcelProposalStatus.PUBLISHED || next == LenientParcelProposalStatus.RETIRED
            LenientParcelProposalStatus.PUBLISHED -> return next == LenientParcelProposalStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientParcelProposalStatus) {
        if (!canMoveTo(next)) {
            error("lenient parcel proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedTotal(): Double = round((cost - attempts.toDouble()) * 100) / 100

    fun snapshot(): LenientParcelProposal {
        return LenientParcelProposal(
            code = code,
            name = name,
            attempts = attempts,
            cost = cost,
            vehicleId = vehicleId,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient parcel proposal in status created. */
fun newLenientParcelProposal(code: String, name: String): LenientParcelProposal {
    return LenientParcelProposal(
        code = code,
        name = name,
        attempts = 0,
        cost = 0.0,
        vehicleId = "",
        tags = mutableListOf<String>(),
        status = LenientParcelProposalStatus.CREATED,
    )
}
