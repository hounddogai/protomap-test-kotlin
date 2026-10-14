package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient capacity proposal is in its life cycle. */
enum class LenientCapacityProposalStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lenient capacity proposal. Its code is unique within a tenant. */
data class LenientCapacityProposal(
    var code: String,
    var title: String,
    var timezone: String,
    var depot: String,
    var windowDays: Int,
    var attributes: MutableMap<String, String>,
    var capacity: Int,
    var vehicleId: String,
    var status: LenientCapacityProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCapacityProposalStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCapacityProposalStatus): Boolean {
        when (status) {
            LenientCapacityProposalStatus.DRAFT -> return next == LenientCapacityProposalStatus.SUSPENDED || next == LenientCapacityProposalStatus.CLOSED
            LenientCapacityProposalStatus.PENDING -> {
                when (next) {
                    LenientCapacityProposalStatus.ACTIVE, LenientCapacityProposalStatus.SUSPENDED -> return true
                }
                return false
            }
            LenientCapacityProposalStatus.ACTIVE -> {
                when (next) {
                    LenientCapacityProposalStatus.CLOSED -> return true
                }
                return false
            }
            LenientCapacityProposalStatus.SUSPENDED -> return next == LenientCapacityProposalStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LenientCapacityProposalStatus) {
        if (!canMoveTo(next)) {
            error("lenient capacity proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round((capacity.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): LenientCapacityProposal {
        return LenientCapacityProposal(
            code = code,
            title = title,
            timezone = timezone,
            depot = depot,
            windowDays = windowDays,
            attributes = attributes,
            capacity = capacity,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lenient capacity proposal in status draft. */
fun newLenientCapacityProposal(code: String, title: String): LenientCapacityProposal {
    return LenientCapacityProposal(
        code = code,
        title = title,
        timezone = "",
        depot = "",
        windowDays = 0,
        attributes = mutableMapOf<String, String>(),
        capacity = 0,
        vehicleId = "",
        status = LenientCapacityProposalStatus.DRAFT,
    )
}
