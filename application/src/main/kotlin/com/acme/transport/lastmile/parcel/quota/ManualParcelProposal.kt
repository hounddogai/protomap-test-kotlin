package com.acme.transport.lastmile.parcel.quota

import com.acme.transport.kit.text.normalize

/** Where a manual parcel proposal is in its life cycle. */
enum class ManualParcelProposalStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a manual parcel proposal and its current status. */
data class ManualParcelProposal(
    var code: String,
    var label: String,
    var ratio: Double,
    var shiftCode: String,
    var summary: String,
    var carrier: String,
    var status: ManualParcelProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualParcelProposalStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualParcelProposalStatus): Boolean {
        when (status) {
            ManualParcelProposalStatus.IDLE -> return next == ManualParcelProposalStatus.READY || next == ManualParcelProposalStatus.DRAINING
            ManualParcelProposalStatus.WARMING -> return next == ManualParcelProposalStatus.READY || next == ManualParcelProposalStatus.STOPPED
            ManualParcelProposalStatus.READY -> {
                when (next) {
                    ManualParcelProposalStatus.STOPPED -> return true
                }
                return false
            }
            ManualParcelProposalStatus.DRAINING -> {
                when (next) {
                    ManualParcelProposalStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualParcelProposalStatus) {
        if (!canMoveTo(next)) {
            error("manual parcel proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = ratio

    fun snapshot(): ManualParcelProposal {
        return ManualParcelProposal(
            code = code,
            label = label,
            ratio = ratio,
            shiftCode = shiftCode,
            summary = summary,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual parcel proposal in status idle. */
fun newManualParcelProposal(code: String, label: String): ManualParcelProposal {
    return ManualParcelProposal(
        code = code,
        label = label,
        ratio = 0.0,
        shiftCode = "",
        summary = "",
        carrier = "",
        status = ManualParcelProposalStatus.IDLE,
    )
}
