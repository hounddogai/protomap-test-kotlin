package com.acme.transport.network.zone.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The zone proposal records of network's zone work, and the rules, stores, and calculations around them.

/** Where a zone proposal is in its life cycle. */
enum class ZoneProposalStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One zone proposal, with what the planners need to act on it. */
data class ZoneProposal(
    var code: String,
    var title: String,
    var attempts: Int,
    var locale: String,
    var graceDays: Int,
    var name: String,
    var fuelLiters: Double,
    var ownerId: String,
    var updatedAt: Int,
    var status: ZoneProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneProposalStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneProposalStatus): Boolean {
        when (status) {
            ZoneProposalStatus.PLANNED -> return next == ZoneProposalStatus.SCHEDULED || next == ZoneProposalStatus.COMPLETED
            ZoneProposalStatus.SCHEDULED -> {
                when (next) {
                    ZoneProposalStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ZoneProposalStatus.IN_PROGRESS -> return next == ZoneProposalStatus.CANCELLED
            ZoneProposalStatus.COMPLETED -> {
                when (next) {
                    ZoneProposalStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneProposalStatus) {
        if (!canMoveTo(next)) {
            error("zone proposal $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"zone proposal"} $code"

    fun projectedVolume(): Double = round((graceDays.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): ZoneProposal {
        return ZoneProposal(
            code = code,
            title = title,
            attempts = attempts,
            locale = locale,
            graceDays = graceDays,
            name = name,
            fuelLiters = fuelLiters,
            ownerId = ownerId,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a zone proposal in status planned. */
fun newZoneProposal(code: String, title: String): ZoneProposal {
    return ZoneProposal(
        code = code,
        title = title,
        attempts = 0,
        locale = "",
        graceDays = 0,
        name = "",
        fuelLiters = 0.0,
        ownerId = "",
        updatedAt = 0,
        status = ZoneProposalStatus.PLANNED,
    )
}
