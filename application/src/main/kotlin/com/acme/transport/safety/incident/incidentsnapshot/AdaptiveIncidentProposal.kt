package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive incident proposal is in its life cycle. */
enum class AdaptiveIncidentProposalStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One adaptive incident proposal, with what the planners need to act on it. */
data class AdaptiveIncidentProposal(
    var code: String,
    var label: String,
    var timezone: String,
    var shiftCode: String,
    var cost: Double,
    var threshold: Double,
    var depot: String,
    var status: AdaptiveIncidentProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveIncidentProposalStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveIncidentProposalStatus): Boolean {
        when (status) {
            AdaptiveIncidentProposalStatus.DRAFT -> return next == AdaptiveIncidentProposalStatus.PENDING || next == AdaptiveIncidentProposalStatus.SUSPENDED
            AdaptiveIncidentProposalStatus.PENDING -> return next == AdaptiveIncidentProposalStatus.ACTIVE || next == AdaptiveIncidentProposalStatus.SUSPENDED
            AdaptiveIncidentProposalStatus.ACTIVE -> return next == AdaptiveIncidentProposalStatus.CLOSED
            AdaptiveIncidentProposalStatus.SUSPENDED -> return next == AdaptiveIncidentProposalStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AdaptiveIncidentProposalStatus) {
        if (!canMoveTo(next)) {
            error("adaptive incident proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveVolume(): Double = round((cost + threshold) * 100) / 100

    fun snapshot(): AdaptiveIncidentProposal {
        return AdaptiveIncidentProposal(
            code = code,
            label = label,
            timezone = timezone,
            shiftCode = shiftCode,
            cost = cost,
            threshold = threshold,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a adaptive incident proposal in status draft. */
fun newAdaptiveIncidentProposal(code: String, label: String): AdaptiveIncidentProposal {
    return AdaptiveIncidentProposal(
        code = code,
        label = label,
        timezone = "",
        shiftCode = "",
        cost = 0.0,
        threshold = 0.0,
        depot = "",
        status = AdaptiveIncidentProposalStatus.DRAFT,
    )
}
