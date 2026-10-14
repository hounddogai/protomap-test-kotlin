package com.acme.transport.fleetops.leasereturn.returnpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive lease return proposal is in its life cycle. */
enum class AdaptiveLeaseReturnProposalStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive lease return proposal as the lease return team keeps it. */
data class AdaptiveLeaseReturnProposal(
    var code: String,
    var name: String,
    var timezone: String,
    var tags: MutableList<String>,
    var weight: Double,
    var segment: String,
    var revision: Int,
    var floorValue: Double,
    var fuelLiters: Double,
    var status: AdaptiveLeaseReturnProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveLeaseReturnProposalStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveLeaseReturnProposalStatus): Boolean {
        when (status) {
            AdaptiveLeaseReturnProposalStatus.IDLE -> return next == AdaptiveLeaseReturnProposalStatus.DRAINING
            AdaptiveLeaseReturnProposalStatus.WARMING -> {
                when (next) {
                    AdaptiveLeaseReturnProposalStatus.DRAINING -> return true
                }
                return false
            }
            AdaptiveLeaseReturnProposalStatus.READY -> return next == AdaptiveLeaseReturnProposalStatus.DRAINING
            AdaptiveLeaseReturnProposalStatus.DRAINING -> return next == AdaptiveLeaseReturnProposalStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AdaptiveLeaseReturnProposalStatus) {
        if (!canMoveTo(next)) {
            error("adaptive lease return proposal $code cannot move from $status to $next")
        }
        status = next
        revision += 1
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

    fun netExposure(): Double = round((floorValue - fuelLiters) * 100) / 100

    fun snapshot(): AdaptiveLeaseReturnProposal {
        return AdaptiveLeaseReturnProposal(
            code = code,
            name = name,
            timezone = timezone,
            tags = tags,
            weight = weight,
            segment = segment,
            revision = revision,
            floorValue = floorValue,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a adaptive lease return proposal in status idle. */
fun newAdaptiveLeaseReturnProposal(code: String, name: String): AdaptiveLeaseReturnProposal {
    return AdaptiveLeaseReturnProposal(
        code = code,
        name = name,
        timezone = "",
        tags = mutableListOf<String>(),
        weight = 0.0,
        segment = "",
        revision = 0,
        floorValue = 0.0,
        fuelLiters = 0.0,
        status = AdaptiveLeaseReturnProposalStatus.IDLE,
    )
}
