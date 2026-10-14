package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report proposal is in its life cycle. */
enum class CarbonReportProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One carbon report proposal, with what the planners need to act on it. */
data class CarbonReportProposal(
    var code: String,
    var title: String,
    var label: String,
    var checksum: String,
    var threshold: Double,
    var etaMinutes: Int,
    var source: String,
    var status: CarbonReportProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportProposalStatus): Boolean {
        when (status) {
            CarbonReportProposalStatus.REQUESTED -> {
                when (next) {
                    CarbonReportProposalStatus.EXPIRED -> return true
                }
                return false
            }
            CarbonReportProposalStatus.GRANTED -> return next == CarbonReportProposalStatus.REVOKED || next == CarbonReportProposalStatus.EXPIRED
            CarbonReportProposalStatus.REVOKED -> return next == CarbonReportProposalStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CarbonReportProposalStatus) {
        if (!canMoveTo(next)) {
            error("carbon report proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($title)"

    fun adjustedValue(): Double = round(threshold * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): CarbonReportProposal {
        return CarbonReportProposal(
            code = code,
            title = title,
            label = label,
            checksum = checksum,
            threshold = threshold,
            etaMinutes = etaMinutes,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a carbon report proposal in status requested. */
fun newCarbonReportProposal(code: String, title: String): CarbonReportProposal {
    return CarbonReportProposal(
        code = code,
        title = title,
        label = "",
        checksum = "",
        threshold = 0.0,
        etaMinutes = 0,
        source = "",
        status = CarbonReportProposalStatus.REQUESTED,
    )
}
