package com.acme.transport.emissions.carbonreport.regionalreportrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict carbon report proposal is in its life cycle. */
enum class StrictCarbonReportProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One strict carbon report proposal, with what the planners need to act on it. */
data class StrictCarbonReportProposal(
    var code: String,
    var name: String,
    var label: String,
    var visible: Boolean,
    var createdAt: Int,
    var summary: String,
    var currency: String,
    var pinned: Boolean,
    var carrier: String,
    var endsAt: Int,
    var status: StrictCarbonReportProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictCarbonReportProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictCarbonReportProposalStatus): Boolean {
        when (status) {
            StrictCarbonReportProposalStatus.REQUESTED -> {
                when (next) {
                    StrictCarbonReportProposalStatus.GRANTED -> return true
                }
                return false
            }
            StrictCarbonReportProposalStatus.GRANTED -> {
                when (next) {
                    StrictCarbonReportProposalStatus.REVOKED -> return true
                }
                return false
            }
            StrictCarbonReportProposalStatus.REVOKED -> return next == StrictCarbonReportProposalStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: StrictCarbonReportProposalStatus) {
        if (!canMoveTo(next)) {
            error("strict carbon report proposal $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $name"

    fun netTotal(): Double = round((createdAt.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): StrictCarbonReportProposal {
        return StrictCarbonReportProposal(
            code = code,
            name = name,
            label = label,
            visible = visible,
            createdAt = createdAt,
            summary = summary,
            currency = currency,
            pinned = pinned,
            carrier = carrier,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict carbon report proposal in status requested. */
fun newStrictCarbonReportProposal(code: String, name: String): StrictCarbonReportProposal {
    return StrictCarbonReportProposal(
        code = code,
        name = name,
        label = "",
        visible = false,
        createdAt = 0,
        summary = "",
        currency = "",
        pinned = false,
        carrier = "",
        endsAt = 0,
        status = StrictCarbonReportProposalStatus.REQUESTED,
    )
}
