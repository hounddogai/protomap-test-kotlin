package com.acme.transport.safety.incident.incidentproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped incident proposal records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a capped incident proposal is in its life cycle. */
enum class CappedIncidentProposalStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capped incident proposal as the incident team keeps it. */
data class CappedIncidentProposal(
    var code: String,
    var label: String,
    var timezone: String,
    var currency: String,
    var sequence: Int,
    var etaMinutes: Int,
    var trailerId: String,
    var fuelLiters: Double,
    var createdAt: Int,
    var status: CappedIncidentProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedIncidentProposalStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedIncidentProposalStatus): Boolean {
        when (status) {
            CappedIncidentProposalStatus.DRAFT -> {
                when (next) {
                    CappedIncidentProposalStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedIncidentProposalStatus.PENDING -> return next == CappedIncidentProposalStatus.SUSPENDED || next == CappedIncidentProposalStatus.CLOSED
            CappedIncidentProposalStatus.ACTIVE -> return next == CappedIncidentProposalStatus.SUSPENDED || next == CappedIncidentProposalStatus.CLOSED
            CappedIncidentProposalStatus.SUSPENDED -> {
                when (next) {
                    CappedIncidentProposalStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedIncidentProposalStatus) {
        if (!canMoveTo(next)) {
            error("capped incident proposal $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"capped incident proposal"} $code"

    fun effectiveVolume(): Double = round((etaMinutes.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): CappedIncidentProposal {
        return CappedIncidentProposal(
            code = code,
            label = label,
            timezone = timezone,
            currency = currency,
            sequence = sequence,
            etaMinutes = etaMinutes,
            trailerId = trailerId,
            fuelLiters = fuelLiters,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped incident proposal in status draft. */
fun newCappedIncidentProposal(code: String, label: String): CappedIncidentProposal {
    return CappedIncidentProposal(
        code = code,
        label = label,
        timezone = "",
        currency = "",
        sequence = 0,
        etaMinutes = 0,
        trailerId = "",
        fuelLiters = 0.0,
        createdAt = 0,
        status = CappedIncidentProposalStatus.DRAFT,
    )
}
