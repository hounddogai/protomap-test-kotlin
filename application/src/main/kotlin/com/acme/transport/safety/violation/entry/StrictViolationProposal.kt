package com.acme.transport.safety.violation.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict violation proposal is in its life cycle. */
enum class StrictViolationProposalStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a strict violation proposal and its current status. */
data class StrictViolationProposal(
    var code: String,
    var name: String,
    var createdAt: Int,
    var score: Double,
    var ceiling: Double,
    var status: StrictViolationProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictViolationProposalStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictViolationProposalStatus): Boolean {
        when (status) {
            StrictViolationProposalStatus.PROPOSED -> return next == StrictViolationProposalStatus.APPROVED || next == StrictViolationProposalStatus.WITHDRAWN
            StrictViolationProposalStatus.APPROVED -> {
                when (next) {
                    StrictViolationProposalStatus.REJECTED, StrictViolationProposalStatus.WITHDRAWN -> return true
                }
                return false
            }
            StrictViolationProposalStatus.REJECTED -> {
                when (next) {
                    StrictViolationProposalStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictViolationProposalStatus) {
        if (!canMoveTo(next)) {
            error("strict violation proposal $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"strict violation proposal"} $code"

    fun weightedVolume(): Double = round(score * ceiling * 100) / 100

    fun snapshot(): StrictViolationProposal {
        return StrictViolationProposal(
            code = code,
            name = name,
            createdAt = createdAt,
            score = score,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict violation proposal in status proposed. */
fun newStrictViolationProposal(code: String, name: String): StrictViolationProposal {
    return StrictViolationProposal(
        code = code,
        name = name,
        createdAt = 0,
        score = 0.0,
        ceiling = 0.0,
        status = StrictViolationProposalStatus.PROPOSED,
    )
}
