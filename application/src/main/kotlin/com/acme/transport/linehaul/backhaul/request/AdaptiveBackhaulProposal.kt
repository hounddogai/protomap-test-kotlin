package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive backhaul proposal is in its life cycle. */
enum class AdaptiveBackhaulProposalStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a adaptive backhaul proposal and its current status. */
data class AdaptiveBackhaulProposal(
    var code: String,
    var title: String,
    var priority: Int,
    var ratio: Double,
    var weight: Double,
    var status: AdaptiveBackhaulProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveBackhaulProposalStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveBackhaulProposalStatus): Boolean {
        when (status) {
            AdaptiveBackhaulProposalStatus.PROPOSED -> return next == AdaptiveBackhaulProposalStatus.REJECTED || next == AdaptiveBackhaulProposalStatus.WITHDRAWN
            AdaptiveBackhaulProposalStatus.APPROVED -> {
                when (next) {
                    AdaptiveBackhaulProposalStatus.WITHDRAWN -> return true
                }
                return false
            }
            AdaptiveBackhaulProposalStatus.REJECTED -> return next == AdaptiveBackhaulProposalStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AdaptiveBackhaulProposalStatus) {
        if (!canMoveTo(next)) {
            error("adaptive backhaul proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netExposure(): Double = round(priority.toDouble() * ratio * 100) / 100

    fun snapshot(): AdaptiveBackhaulProposal {
        return AdaptiveBackhaulProposal(
            code = code,
            title = title,
            priority = priority,
            ratio = ratio,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive backhaul proposal in status proposed. */
fun newAdaptiveBackhaulProposal(code: String, title: String): AdaptiveBackhaulProposal {
    return AdaptiveBackhaulProposal(
        code = code,
        title = title,
        priority = 0,
        ratio = 0.0,
        weight = 0.0,
        status = AdaptiveBackhaulProposalStatus.PROPOSED,
    )
}
