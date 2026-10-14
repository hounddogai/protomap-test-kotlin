package com.acme.transport.crewing.availability.weightedavailabilityreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a availability proposal is in its life cycle. */
enum class AvailabilityProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One availability proposal, with what the planners need to act on it. */
data class AvailabilityProposal(
    var code: String,
    var title: String,
    var ceiling: Double,
    var capacity: Int,
    var country: String,
    var locale: String,
    var ratio: Double,
    var status: AvailabilityProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityProposalStatus): Boolean {
        when (status) {
            AvailabilityProposalStatus.CREATED -> {
                when (next) {
                    AvailabilityProposalStatus.PUBLISHED -> return true
                }
                return false
            }
            AvailabilityProposalStatus.VALIDATED -> {
                when (next) {
                    AvailabilityProposalStatus.PUBLISHED, AvailabilityProposalStatus.RETIRED -> return true
                }
                return false
            }
            AvailabilityProposalStatus.PUBLISHED -> return next == AvailabilityProposalStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AvailabilityProposalStatus) {
        if (!canMoveTo(next)) {
            error("availability proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedVolume(): Double = round(ratio * capacity.toDouble() * 100) / 100

    fun snapshot(): AvailabilityProposal {
        return AvailabilityProposal(
            code = code,
            title = title,
            ceiling = ceiling,
            capacity = capacity,
            country = country,
            locale = locale,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a availability proposal in status created. */
fun newAvailabilityProposal(code: String, title: String): AvailabilityProposal {
    return AvailabilityProposal(
        code = code,
        title = title,
        ceiling = 0.0,
        capacity = 0,
        country = "",
        locale = "",
        ratio = 0.0,
        status = AvailabilityProposalStatus.CREATED,
    )
}
