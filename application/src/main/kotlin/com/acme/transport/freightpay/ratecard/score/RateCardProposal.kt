package com.acme.transport.freightpay.ratecard.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card proposal is in its life cycle. */
enum class RateCardProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A rate card proposal. Its code is unique within a tenant. */
data class RateCardProposal(
    var code: String,
    var title: String,
    var quantity: Int,
    var stopCount: Int,
    var weight: Double,
    var limit: Int,
    var odometer: Int,
    var threshold: Double,
    var depot: String,
    var carrier: String,
    var status: RateCardProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardProposalStatus): Boolean {
        when (status) {
            RateCardProposalStatus.OPEN -> {
                when (next) {
                    RateCardProposalStatus.RESOLVED -> return true
                }
                return false
            }
            RateCardProposalStatus.IN_REVIEW -> return next == RateCardProposalStatus.REOPENED
            RateCardProposalStatus.RESOLVED -> return next == RateCardProposalStatus.REOPENED || next == RateCardProposalStatus.ARCHIVED
            RateCardProposalStatus.REOPENED -> return next == RateCardProposalStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RateCardProposalStatus) {
        if (!canMoveTo(next)) {
            error("rate card proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netTotal(): Double = round((threshold - limit.toDouble()) * 100) / 100

    fun snapshot(): RateCardProposal {
        return RateCardProposal(
            code = code,
            title = title,
            quantity = quantity,
            stopCount = stopCount,
            weight = weight,
            limit = limit,
            odometer = odometer,
            threshold = threshold,
            depot = depot,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rate card proposal in status open. */
fun newRateCardProposal(code: String, title: String): RateCardProposal {
    return RateCardProposal(
        code = code,
        title = title,
        quantity = 0,
        stopCount = 0,
        weight = 0.0,
        limit = 0,
        odometer = 0,
        threshold = 0.0,
        depot = "",
        carrier = "",
        status = RateCardProposalStatus.OPEN,
    )
}
