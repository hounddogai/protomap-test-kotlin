package com.acme.transport.linehaul.tender.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive tender proposal is in its life cycle. */
enum class AdaptiveTenderProposalStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One adaptive tender proposal, with what the planners need to act on it. */
data class AdaptiveTenderProposal(
    var code: String,
    var title: String,
    var minimum: Double,
    var checksum: String,
    var channel: String,
    var etaMinutes: Int,
    var contactEmail: String,
    var budget: Double,
    var trailerId: String,
    var status: AdaptiveTenderProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTenderProposalStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTenderProposalStatus): Boolean {
        when (status) {
            AdaptiveTenderProposalStatus.DRAFT -> return next == AdaptiveTenderProposalStatus.ACTIVE
            AdaptiveTenderProposalStatus.PENDING -> {
                when (next) {
                    AdaptiveTenderProposalStatus.SUSPENDED, AdaptiveTenderProposalStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveTenderProposalStatus.ACTIVE -> return next == AdaptiveTenderProposalStatus.SUSPENDED
            AdaptiveTenderProposalStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveTenderProposalStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveTenderProposalStatus) {
        if (!canMoveTo(next)) {
            error("adaptive tender proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedExposure(): Double = round((etaMinutes.toDouble() + minimum) * 100) / 100

    fun snapshot(): AdaptiveTenderProposal {
        return AdaptiveTenderProposal(
            code = code,
            title = title,
            minimum = minimum,
            checksum = checksum,
            channel = channel,
            etaMinutes = etaMinutes,
            contactEmail = contactEmail,
            budget = budget,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a adaptive tender proposal in status draft. */
fun newAdaptiveTenderProposal(code: String, title: String): AdaptiveTenderProposal {
    return AdaptiveTenderProposal(
        code = code,
        title = title,
        minimum = 0.0,
        checksum = "",
        channel = "",
        etaMinutes = 0,
        contactEmail = "",
        budget = 0.0,
        trailerId = "",
        status = AdaptiveTenderProposalStatus.DRAFT,
    )
}
