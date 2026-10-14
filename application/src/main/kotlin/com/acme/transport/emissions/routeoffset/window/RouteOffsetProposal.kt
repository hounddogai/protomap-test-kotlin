package com.acme.transport.emissions.routeoffset.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset proposal is in its life cycle. */
enum class RouteOffsetProposalStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a route offset proposal and its current status. */
data class RouteOffsetProposal(
    var code: String,
    var title: String,
    var notes: String,
    var expiresAt: Int,
    var trailerId: String,
    var fuelLiters: Double,
    var checksum: String,
    var phone: String,
    var status: RouteOffsetProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetProposalStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetProposalStatus): Boolean {
        when (status) {
            RouteOffsetProposalStatus.PLANNED -> return next == RouteOffsetProposalStatus.IN_PROGRESS || next == RouteOffsetProposalStatus.CANCELLED
            RouteOffsetProposalStatus.SCHEDULED -> return next == RouteOffsetProposalStatus.IN_PROGRESS || next == RouteOffsetProposalStatus.COMPLETED
            RouteOffsetProposalStatus.IN_PROGRESS -> {
                when (next) {
                    RouteOffsetProposalStatus.CANCELLED -> return true
                }
                return false
            }
            RouteOffsetProposalStatus.COMPLETED -> {
                when (next) {
                    RouteOffsetProposalStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetProposalStatus) {
        if (!canMoveTo(next)) {
            error("route offset proposal $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedExposure(): Double = round(expiresAt.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): RouteOffsetProposal {
        return RouteOffsetProposal(
            code = code,
            title = title,
            notes = notes,
            expiresAt = expiresAt,
            trailerId = trailerId,
            fuelLiters = fuelLiters,
            checksum = checksum,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a route offset proposal in status planned. */
fun newRouteOffsetProposal(code: String, title: String): RouteOffsetProposal {
    return RouteOffsetProposal(
        code = code,
        title = title,
        notes = "",
        expiresAt = 0,
        trailerId = "",
        fuelLiters = 0.0,
        checksum = "",
        phone = "",
        status = RouteOffsetProposalStatus.PLANNED,
    )
}
