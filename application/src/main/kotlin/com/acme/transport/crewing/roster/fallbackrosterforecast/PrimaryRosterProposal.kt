package com.acme.transport.crewing.roster.fallbackrosterforecast

import com.acme.transport.kit.text.normalize

/** Where a primary roster proposal is in its life cycle. */
enum class PrimaryRosterProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A primary roster proposal. Its code is unique within a tenant. */
data class PrimaryRosterProposal(
    var code: String,
    var name: String,
    var version: Int,
    var lane: String,
    var category: String,
    var locale: String,
    var status: PrimaryRosterProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRosterProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRosterProposalStatus): Boolean {
        when (status) {
            PrimaryRosterProposalStatus.REQUESTED -> {
                when (next) {
                    PrimaryRosterProposalStatus.GRANTED -> return true
                }
                return false
            }
            PrimaryRosterProposalStatus.GRANTED -> {
                when (next) {
                    PrimaryRosterProposalStatus.EXPIRED -> return true
                }
                return false
            }
            PrimaryRosterProposalStatus.REVOKED -> {
                when (next) {
                    PrimaryRosterProposalStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryRosterProposalStatus) {
        if (!canMoveTo(next)) {
            error("primary roster proposal $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): PrimaryRosterProposal {
        return PrimaryRosterProposal(
            code = code,
            name = name,
            version = version,
            lane = lane,
            category = category,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a primary roster proposal in status requested. */
fun newPrimaryRosterProposal(code: String, name: String): PrimaryRosterProposal {
    return PrimaryRosterProposal(
        code = code,
        name = name,
        version = 0,
        lane = "",
        category = "",
        locale = "",
        status = PrimaryRosterProposalStatus.REQUESTED,
    )
}
