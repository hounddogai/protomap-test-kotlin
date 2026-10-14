package com.acme.transport.lastmile.locker.lockerquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive locker proposal is in its life cycle. */
enum class AdaptiveLockerProposalStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One adaptive locker proposal, with what the planners need to act on it. */
data class AdaptiveLockerProposal(
    var code: String,
    var name: String,
    var capacity: Int,
    var currency: String,
    var updatedAt: Int,
    var ceiling: Double,
    var budget: Double,
    var phone: String,
    var status: AdaptiveLockerProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveLockerProposalStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveLockerProposalStatus): Boolean {
        when (status) {
            AdaptiveLockerProposalStatus.IDLE -> return next == AdaptiveLockerProposalStatus.READY
            AdaptiveLockerProposalStatus.WARMING -> return next == AdaptiveLockerProposalStatus.READY
            AdaptiveLockerProposalStatus.READY -> {
                when (next) {
                    AdaptiveLockerProposalStatus.DRAINING -> return true
                }
                return false
            }
            AdaptiveLockerProposalStatus.DRAINING -> return next == AdaptiveLockerProposalStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AdaptiveLockerProposalStatus) {
        if (!canMoveTo(next)) {
            error("adaptive locker proposal $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"adaptive locker proposal"} $code"

    fun projectedLoad(): Double = round(capacity.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): AdaptiveLockerProposal {
        return AdaptiveLockerProposal(
            code = code,
            name = name,
            capacity = capacity,
            currency = currency,
            updatedAt = updatedAt,
            ceiling = ceiling,
            budget = budget,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive locker proposal in status idle. */
fun newAdaptiveLockerProposal(code: String, name: String): AdaptiveLockerProposal {
    return AdaptiveLockerProposal(
        code = code,
        name = name,
        capacity = 0,
        currency = "",
        updatedAt = 0,
        ceiling = 0.0,
        budget = 0.0,
        phone = "",
        status = AdaptiveLockerProposalStatus.IDLE,
    )
}
