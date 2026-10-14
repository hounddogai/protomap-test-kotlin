package com.acme.transport.safety.violation.violationpolicy

import com.acme.transport.kit.text.normalize

/** Where a global violation proposal is in its life cycle. */
enum class GlobalViolationProposalStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a global violation proposal and its current status. */
data class GlobalViolationProposal(
    var code: String,
    var name: String,
    var driverId: String,
    var channel: String,
    var region: String,
    var score: Double,
    var status: GlobalViolationProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalViolationProposalStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalViolationProposalStatus): Boolean {
        when (status) {
            GlobalViolationProposalStatus.DRAFT -> return next == GlobalViolationProposalStatus.PENDING
            GlobalViolationProposalStatus.PENDING -> {
                when (next) {
                    GlobalViolationProposalStatus.CLOSED -> return true
                }
                return false
            }
            GlobalViolationProposalStatus.ACTIVE -> return next == GlobalViolationProposalStatus.SUSPENDED || next == GlobalViolationProposalStatus.CLOSED
            GlobalViolationProposalStatus.SUSPENDED -> {
                when (next) {
                    GlobalViolationProposalStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalViolationProposalStatus) {
        if (!canMoveTo(next)) {
            error("global violation proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global violation proposal"} $code"

    fun measuredValue(): Double = score

    fun snapshot(): GlobalViolationProposal {
        return GlobalViolationProposal(
            code = code,
            name = name,
            driverId = driverId,
            channel = channel,
            region = region,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a global violation proposal in status draft. */
fun newGlobalViolationProposal(code: String, name: String): GlobalViolationProposal {
    return GlobalViolationProposal(
        code = code,
        name = name,
        driverId = "",
        channel = "",
        region = "",
        score = 0.0,
        status = GlobalViolationProposalStatus.DRAFT,
    )
}
