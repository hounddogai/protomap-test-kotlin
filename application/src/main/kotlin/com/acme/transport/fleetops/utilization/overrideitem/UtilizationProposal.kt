package com.acme.transport.fleetops.utilization.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization proposal is in its life cycle. */
enum class UtilizationProposalStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One utilization proposal, with what the planners need to act on it. */
data class UtilizationProposal(
    var code: String,
    var name: String,
    var label: String,
    var depot: String,
    var loadKg: Double,
    var channel: String,
    var routeCode: String,
    var cost: Double,
    var limit: Int,
    var lane: String,
    var status: UtilizationProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationProposalStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationProposalStatus): Boolean {
        when (status) {
            UtilizationProposalStatus.PROPOSED -> {
                when (next) {
                    UtilizationProposalStatus.APPROVED, UtilizationProposalStatus.REJECTED -> return true
                }
                return false
            }
            UtilizationProposalStatus.APPROVED -> {
                when (next) {
                    UtilizationProposalStatus.REJECTED -> return true
                }
                return false
            }
            UtilizationProposalStatus.REJECTED -> return next == UtilizationProposalStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: UtilizationProposalStatus) {
        if (!canMoveTo(next)) {
            error("utilization proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $name"

    fun projectedTotal(): Double = round(cost * limit.toDouble() * 100) / 100

    fun snapshot(): UtilizationProposal {
        return UtilizationProposal(
            code = code,
            name = name,
            label = label,
            depot = depot,
            loadKg = loadKg,
            channel = channel,
            routeCode = routeCode,
            cost = cost,
            limit = limit,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a utilization proposal in status proposed. */
fun newUtilizationProposal(code: String, name: String): UtilizationProposal {
    return UtilizationProposal(
        code = code,
        name = name,
        label = "",
        depot = "",
        loadKg = 0.0,
        channel = "",
        routeCode = "",
        cost = 0.0,
        limit = 0,
        lane = "",
        status = UtilizationProposalStatus.PROPOSED,
    )
}
