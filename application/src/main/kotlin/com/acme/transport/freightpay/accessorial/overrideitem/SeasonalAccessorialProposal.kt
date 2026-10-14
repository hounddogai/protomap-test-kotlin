package com.acme.transport.freightpay.accessorial.overrideitem

import com.acme.transport.kit.text.normalize

/** Where a seasonal accessorial proposal is in its life cycle. */
enum class SeasonalAccessorialProposalStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A seasonal accessorial proposal. Its code is unique within a tenant. */
data class SeasonalAccessorialProposal(
    var code: String,
    var label: String,
    var vehicleId: String,
    var timezone: String,
    var routeCode: String,
    var quantity: Int,
    var status: SeasonalAccessorialProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalAccessorialProposalStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalAccessorialProposalStatus): Boolean {
        when (status) {
            SeasonalAccessorialProposalStatus.PROPOSED -> return next == SeasonalAccessorialProposalStatus.WITHDRAWN
            SeasonalAccessorialProposalStatus.APPROVED -> return next == SeasonalAccessorialProposalStatus.REJECTED || next == SeasonalAccessorialProposalStatus.WITHDRAWN
            SeasonalAccessorialProposalStatus.REJECTED -> return next == SeasonalAccessorialProposalStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: SeasonalAccessorialProposalStatus) {
        if (!canMoveTo(next)) {
            error("seasonal accessorial proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): SeasonalAccessorialProposal {
        return SeasonalAccessorialProposal(
            code = code,
            label = label,
            vehicleId = vehicleId,
            timezone = timezone,
            routeCode = routeCode,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal accessorial proposal in status proposed. */
fun newSeasonalAccessorialProposal(code: String, label: String): SeasonalAccessorialProposal {
    return SeasonalAccessorialProposal(
        code = code,
        label = label,
        vehicleId = "",
        timezone = "",
        routeCode = "",
        quantity = 0,
        status = SeasonalAccessorialProposalStatus.PROPOSED,
    )
}
