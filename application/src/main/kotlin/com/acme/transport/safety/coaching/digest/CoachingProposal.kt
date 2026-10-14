package com.acme.transport.safety.coaching.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching proposal is in its life cycle. */
enum class CoachingProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a coaching proposal and its current status. */
data class CoachingProposal(
    var code: String,
    var name: String,
    var floorValue: Double,
    var unit: String,
    var ratio: Double,
    var sequence: Int,
    var depot: String,
    var title: String,
    var region: String,
    var status: CoachingProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingProposalStatus): Boolean {
        when (status) {
            CoachingProposalStatus.REQUESTED -> {
                when (next) {
                    CoachingProposalStatus.GRANTED, CoachingProposalStatus.EXPIRED -> return true
                }
                return false
            }
            CoachingProposalStatus.GRANTED -> return next == CoachingProposalStatus.REVOKED || next == CoachingProposalStatus.EXPIRED
            CoachingProposalStatus.REVOKED -> {
                when (next) {
                    CoachingProposalStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingProposalStatus) {
        if (!canMoveTo(next)) {
            error("coaching proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedTotal(): Double = round((floorValue - sequence.toDouble()) * 100) / 100

    fun snapshot(): CoachingProposal {
        return CoachingProposal(
            code = code,
            name = name,
            floorValue = floorValue,
            unit = unit,
            ratio = ratio,
            sequence = sequence,
            depot = depot,
            title = title,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a coaching proposal in status requested. */
fun newCoachingProposal(code: String, name: String): CoachingProposal {
    return CoachingProposal(
        code = code,
        name = name,
        floorValue = 0.0,
        unit = "",
        ratio = 0.0,
        sequence = 0,
        depot = "",
        title = "",
        region = "",
        status = CoachingProposalStatus.REQUESTED,
    )
}
