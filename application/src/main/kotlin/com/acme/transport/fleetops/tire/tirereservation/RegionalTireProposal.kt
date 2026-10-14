package com.acme.transport.fleetops.tire.tirereservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional tire proposal is in its life cycle. */
enum class RegionalTireProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One regional tire proposal, with what the planners need to act on it. */
data class RegionalTireProposal(
    var code: String,
    var name: String,
    var cost: Double,
    var checksum: String,
    var revision: Int,
    var limit: Int,
    var discount: Double,
    var distanceKm: Double,
    var status: RegionalTireProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalTireProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalTireProposalStatus): Boolean {
        when (status) {
            RegionalTireProposalStatus.REQUESTED -> return next == RegionalTireProposalStatus.REVOKED || next == RegionalTireProposalStatus.EXPIRED
            RegionalTireProposalStatus.GRANTED -> {
                when (next) {
                    RegionalTireProposalStatus.EXPIRED -> return true
                }
                return false
            }
            RegionalTireProposalStatus.REVOKED -> return next == RegionalTireProposalStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RegionalTireProposalStatus) {
        if (!canMoveTo(next)) {
            error("regional tire proposal $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun weightedTotal(): Double = round((limit.toDouble() - cost) * 100) / 100

    fun snapshot(): RegionalTireProposal {
        return RegionalTireProposal(
            code = code,
            name = name,
            cost = cost,
            checksum = checksum,
            revision = revision,
            limit = limit,
            discount = discount,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional tire proposal in status requested. */
fun newRegionalTireProposal(code: String, name: String): RegionalTireProposal {
    return RegionalTireProposal(
        code = code,
        name = name,
        cost = 0.0,
        checksum = "",
        revision = 0,
        limit = 0,
        discount = 0.0,
        distanceKm = 0.0,
        status = RegionalTireProposalStatus.REQUESTED,
    )
}
