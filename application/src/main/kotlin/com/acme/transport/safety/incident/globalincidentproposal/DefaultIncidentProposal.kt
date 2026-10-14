package com.acme.transport.safety.incident.globalincidentproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default incident proposal records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a default incident proposal is in its life cycle. */
enum class DefaultIncidentProposalStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a default incident proposal and its current status. */
data class DefaultIncidentProposal(
    var code: String,
    var label: String,
    var graceDays: Int,
    var attempts: Int,
    var pallets: Int,
    var stopCount: Int,
    var status: DefaultIncidentProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultIncidentProposalStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultIncidentProposalStatus): Boolean {
        when (status) {
            DefaultIncidentProposalStatus.PROPOSED -> return next == DefaultIncidentProposalStatus.WITHDRAWN
            DefaultIncidentProposalStatus.APPROVED -> return next == DefaultIncidentProposalStatus.WITHDRAWN
            DefaultIncidentProposalStatus.REJECTED -> return next == DefaultIncidentProposalStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DefaultIncidentProposalStatus) {
        if (!canMoveTo(next)) {
            error("default incident proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netLoad(): Double = round(stopCount.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): DefaultIncidentProposal {
        return DefaultIncidentProposal(
            code = code,
            label = label,
            graceDays = graceDays,
            attempts = attempts,
            pallets = pallets,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default incident proposal in status proposed. */
fun newDefaultIncidentProposal(code: String, label: String): DefaultIncidentProposal {
    return DefaultIncidentProposal(
        code = code,
        label = label,
        graceDays = 0,
        attempts = 0,
        pallets = 0,
        stopCount = 0,
        status = DefaultIncidentProposalStatus.PROPOSED,
    )
}
