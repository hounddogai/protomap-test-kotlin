package com.acme.transport.safety.incident.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident proposal is in its life cycle. */
enum class IncidentProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a incident proposal and its current status. */
data class IncidentProposal(
    var code: String,
    var label: String,
    var name: String,
    var pinned: Boolean,
    var ceiling: Double,
    var notes: String,
    var odometer: Int,
    var summary: String,
    var windowDays: Int,
    var amount: Double,
    var status: IncidentProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentProposalStatus): Boolean {
        when (status) {
            IncidentProposalStatus.CREATED -> {
                when (next) {
                    IncidentProposalStatus.VALIDATED, IncidentProposalStatus.PUBLISHED -> return true
                }
                return false
            }
            IncidentProposalStatus.VALIDATED -> {
                when (next) {
                    IncidentProposalStatus.PUBLISHED, IncidentProposalStatus.RETIRED -> return true
                }
                return false
            }
            IncidentProposalStatus.PUBLISHED -> return next == IncidentProposalStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: IncidentProposalStatus) {
        if (!canMoveTo(next)) {
            error("incident proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"incident proposal"} $code"

    fun adjustedVolume(): Double = round(ceiling * windowDays.toDouble() * 100) / 100

    fun snapshot(): IncidentProposal {
        return IncidentProposal(
            code = code,
            label = label,
            name = name,
            pinned = pinned,
            ceiling = ceiling,
            notes = notes,
            odometer = odometer,
            summary = summary,
            windowDays = windowDays,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a incident proposal in status created. */
fun newIncidentProposal(code: String, label: String): IncidentProposal {
    return IncidentProposal(
        code = code,
        label = label,
        name = "",
        pinned = false,
        ceiling = 0.0,
        notes = "",
        odometer = 0,
        summary = "",
        windowDays = 0,
        amount = 0.0,
        status = IncidentProposalStatus.CREATED,
    )
}
