package com.acme.transport.fleetops.inspection.group

import com.acme.transport.kit.text.normalize

/** Where a inspection proposal is in its life cycle. */
enum class InspectionProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A inspection proposal as the inspection team keeps it. */
data class InspectionProposal(
    var code: String,
    var title: String,
    var routeCode: String,
    var summary: String,
    var channel: String,
    var quantity: Int,
    var status: InspectionProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionProposalStatus): Boolean {
        when (status) {
            InspectionProposalStatus.OPEN -> {
                when (next) {
                    InspectionProposalStatus.REOPENED -> return true
                }
                return false
            }
            InspectionProposalStatus.IN_REVIEW -> return next == InspectionProposalStatus.ARCHIVED
            InspectionProposalStatus.RESOLVED -> return next == InspectionProposalStatus.REOPENED
            InspectionProposalStatus.REOPENED -> {
                when (next) {
                    InspectionProposalStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionProposalStatus) {
        if (!canMoveTo(next)) {
            error("inspection proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"inspection proposal"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): InspectionProposal {
        return InspectionProposal(
            code = code,
            title = title,
            routeCode = routeCode,
            summary = summary,
            channel = channel,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a inspection proposal in status open. */
fun newInspectionProposal(code: String, title: String): InspectionProposal {
    return InspectionProposal(
        code = code,
        title = title,
        routeCode = "",
        summary = "",
        channel = "",
        quantity = 0,
        status = InspectionProposalStatus.OPEN,
    )
}
