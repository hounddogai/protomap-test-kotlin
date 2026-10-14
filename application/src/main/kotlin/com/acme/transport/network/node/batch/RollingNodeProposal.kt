package com.acme.transport.network.node.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling node proposal is in its life cycle. */
enum class RollingNodeProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a rolling node proposal and its current status. */
data class RollingNodeProposal(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var locale: String,
    var ceiling: Double,
    var routeCode: String,
    var ratio: Double,
    var budget: Double,
    var windowDays: Int,
    var trailerId: String,
    var vehicleId: String,
    var status: RollingNodeProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingNodeProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingNodeProposalStatus): Boolean {
        when (status) {
            RollingNodeProposalStatus.OPEN -> {
                when (next) {
                    RollingNodeProposalStatus.RESOLVED -> return true
                }
                return false
            }
            RollingNodeProposalStatus.IN_REVIEW -> {
                when (next) {
                    RollingNodeProposalStatus.RESOLVED, RollingNodeProposalStatus.ARCHIVED -> return true
                }
                return false
            }
            RollingNodeProposalStatus.RESOLVED -> return next == RollingNodeProposalStatus.REOPENED || next == RollingNodeProposalStatus.ARCHIVED
            RollingNodeProposalStatus.REOPENED -> return next == RollingNodeProposalStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RollingNodeProposalStatus) {
        if (!canMoveTo(next)) {
            error("rolling node proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling node proposal"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round(ceiling * budget * 100) / 100

    fun snapshot(): RollingNodeProposal {
        return RollingNodeProposal(
            code = code,
            title = title,
            tags = tags,
            locale = locale,
            ceiling = ceiling,
            routeCode = routeCode,
            ratio = ratio,
            budget = budget,
            windowDays = windowDays,
            trailerId = trailerId,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rolling node proposal in status open. */
fun newRollingNodeProposal(code: String, title: String): RollingNodeProposal {
    return RollingNodeProposal(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        locale = "",
        ceiling = 0.0,
        routeCode = "",
        ratio = 0.0,
        budget = 0.0,
        windowDays = 0,
        trailerId = "",
        vehicleId = "",
        status = RollingNodeProposalStatus.OPEN,
    )
}
