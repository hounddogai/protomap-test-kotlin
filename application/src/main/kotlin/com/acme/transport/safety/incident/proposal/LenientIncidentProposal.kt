package com.acme.transport.safety.incident.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient incident proposal records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a lenient incident proposal is in its life cycle. */
enum class LenientIncidentProposalStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One lenient incident proposal, with what the planners need to act on it. */
data class LenientIncidentProposal(
    var code: String,
    var name: String,
    var stopCount: Int,
    var externalRef: String,
    var country: String,
    var ownerId: String,
    var revision: Int,
    var floorValue: Double,
    var status: LenientIncidentProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientIncidentProposalStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientIncidentProposalStatus): Boolean {
        when (status) {
            LenientIncidentProposalStatus.DRAFT -> return next == LenientIncidentProposalStatus.PENDING
            LenientIncidentProposalStatus.PENDING -> {
                when (next) {
                    LenientIncidentProposalStatus.ACTIVE, LenientIncidentProposalStatus.CLOSED -> return true
                }
                return false
            }
            LenientIncidentProposalStatus.ACTIVE -> {
                when (next) {
                    LenientIncidentProposalStatus.SUSPENDED, LenientIncidentProposalStatus.CLOSED -> return true
                }
                return false
            }
            LenientIncidentProposalStatus.SUSPENDED -> {
                when (next) {
                    LenientIncidentProposalStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientIncidentProposalStatus) {
        if (!canMoveTo(next)) {
            error("lenient incident proposal $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun netExposure(): Double = round((floorValue - revision.toDouble()) * 100) / 100

    fun snapshot(): LenientIncidentProposal {
        return LenientIncidentProposal(
            code = code,
            name = name,
            stopCount = stopCount,
            externalRef = externalRef,
            country = country,
            ownerId = ownerId,
            revision = revision,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient incident proposal in status draft. */
fun newLenientIncidentProposal(code: String, name: String): LenientIncidentProposal {
    return LenientIncidentProposal(
        code = code,
        name = name,
        stopCount = 0,
        externalRef = "",
        country = "",
        ownerId = "",
        revision = 0,
        floorValue = 0.0,
        status = LenientIncidentProposalStatus.DRAFT,
    )
}
