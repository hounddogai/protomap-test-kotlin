package com.acme.transport.customs.inspection.pendinginspectionrule

import com.acme.transport.kit.text.normalize

/** Where a inspection proposal is in its life cycle. */
enum class InspectionProposalStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One inspection proposal, with what the planners need to act on it. */
data class InspectionProposal(
    var code: String,
    var label: String,
    var shiftCode: String,
    var name: String,
    var score: Double,
    var reviewer: String,
    var status: InspectionProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionProposalStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionProposalStatus): Boolean {
        when (status) {
            InspectionProposalStatus.IDLE -> {
                when (next) {
                    InspectionProposalStatus.WARMING -> return true
                }
                return false
            }
            InspectionProposalStatus.WARMING -> {
                when (next) {
                    InspectionProposalStatus.DRAINING -> return true
                }
                return false
            }
            InspectionProposalStatus.READY -> {
                when (next) {
                    InspectionProposalStatus.STOPPED -> return true
                }
                return false
            }
            InspectionProposalStatus.DRAINING -> return next == InspectionProposalStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: InspectionProposalStatus) {
        if (!canMoveTo(next)) {
            error("inspection proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = score

    fun snapshot(): InspectionProposal {
        return InspectionProposal(
            code = code,
            label = label,
            shiftCode = shiftCode,
            name = name,
            score = score,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a inspection proposal in status idle. */
fun newInspectionProposal(code: String, label: String): InspectionProposal {
    return InspectionProposal(
        code = code,
        label = label,
        shiftCode = "",
        name = "",
        score = 0.0,
        reviewer = "",
        status = InspectionProposalStatus.IDLE,
    )
}
