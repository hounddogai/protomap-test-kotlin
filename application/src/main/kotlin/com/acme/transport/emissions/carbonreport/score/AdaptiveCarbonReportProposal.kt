package com.acme.transport.emissions.carbonreport.score

import com.acme.transport.kit.text.normalize

/** Where a adaptive carbon report proposal is in its life cycle. */
enum class AdaptiveCarbonReportProposalStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A adaptive carbon report proposal as the carbon report team keeps it. */
data class AdaptiveCarbonReportProposal(
    var code: String,
    var title: String,
    var lane: String,
    var startsAt: Int,
    var dock: String,
    var routeCode: String,
    var status: AdaptiveCarbonReportProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCarbonReportProposalStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCarbonReportProposalStatus): Boolean {
        when (status) {
            AdaptiveCarbonReportProposalStatus.PLANNED -> return next == AdaptiveCarbonReportProposalStatus.IN_PROGRESS
            AdaptiveCarbonReportProposalStatus.SCHEDULED -> return next == AdaptiveCarbonReportProposalStatus.IN_PROGRESS || next == AdaptiveCarbonReportProposalStatus.COMPLETED
            AdaptiveCarbonReportProposalStatus.IN_PROGRESS -> return next == AdaptiveCarbonReportProposalStatus.COMPLETED
            AdaptiveCarbonReportProposalStatus.COMPLETED -> {
                when (next) {
                    AdaptiveCarbonReportProposalStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveCarbonReportProposalStatus) {
        if (!canMoveTo(next)) {
            error("adaptive carbon report proposal $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"adaptive carbon report proposal"} $code"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): AdaptiveCarbonReportProposal {
        return AdaptiveCarbonReportProposal(
            code = code,
            title = title,
            lane = lane,
            startsAt = startsAt,
            dock = dock,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a adaptive carbon report proposal in status planned. */
fun newAdaptiveCarbonReportProposal(code: String, title: String): AdaptiveCarbonReportProposal {
    return AdaptiveCarbonReportProposal(
        code = code,
        title = title,
        lane = "",
        startsAt = 0,
        dock = "",
        routeCode = "",
        status = AdaptiveCarbonReportProposalStatus.PLANNED,
    )
}
