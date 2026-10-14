package com.acme.transport.emissions.fuelburn.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled fuel burn proposal records of emissions's fuel burn work, and the rules, stores, and
// calculations around them.

/** Where a scheduled fuel burn proposal is in its life cycle. */
enum class ScheduledFuelBurnProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One scheduled fuel burn proposal, with what the planners need to act on it. */
data class ScheduledFuelBurnProposal(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var country: String,
    var ceiling: Double,
    var contactEmail: String,
    var label: String,
    var status: ScheduledFuelBurnProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledFuelBurnProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledFuelBurnProposalStatus): Boolean {
        when (status) {
            ScheduledFuelBurnProposalStatus.CREATED -> return next == ScheduledFuelBurnProposalStatus.RETIRED
            ScheduledFuelBurnProposalStatus.VALIDATED -> return next == ScheduledFuelBurnProposalStatus.PUBLISHED || next == ScheduledFuelBurnProposalStatus.RETIRED
            ScheduledFuelBurnProposalStatus.PUBLISHED -> return next == ScheduledFuelBurnProposalStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledFuelBurnProposalStatus) {
        if (!canMoveTo(next)) {
            error("scheduled fuel burn proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $name"

    fun weightedExposure(): Double = round((ceiling + fuelLiters) * 100) / 100

    fun snapshot(): ScheduledFuelBurnProposal {
        return ScheduledFuelBurnProposal(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            country = country,
            ceiling = ceiling,
            contactEmail = contactEmail,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a scheduled fuel burn proposal in status created. */
fun newScheduledFuelBurnProposal(code: String, name: String): ScheduledFuelBurnProposal {
    return ScheduledFuelBurnProposal(
        code = code,
        name = name,
        fuelLiters = 0.0,
        country = "",
        ceiling = 0.0,
        contactEmail = "",
        label = "",
        status = ScheduledFuelBurnProposalStatus.CREATED,
    )
}
