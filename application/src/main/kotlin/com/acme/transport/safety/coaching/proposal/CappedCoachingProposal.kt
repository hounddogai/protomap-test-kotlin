package com.acme.transport.safety.coaching.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped coaching proposal records of safety's coaching work, and the rules, stores, and calculations
// around them.

/** Where a capped coaching proposal is in its life cycle. */
enum class CappedCoachingProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A capped coaching proposal. Its code is unique within a tenant. */
data class CappedCoachingProposal(
    var code: String,
    var label: String,
    var driverId: String,
    var approved: Boolean,
    var windowDays: Int,
    var batchSize: Int,
    var status: CappedCoachingProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCoachingProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCoachingProposalStatus): Boolean {
        when (status) {
            CappedCoachingProposalStatus.OPEN -> return next == CappedCoachingProposalStatus.ARCHIVED
            CappedCoachingProposalStatus.IN_REVIEW -> return next == CappedCoachingProposalStatus.REOPENED
            CappedCoachingProposalStatus.RESOLVED -> return next == CappedCoachingProposalStatus.REOPENED || next == CappedCoachingProposalStatus.ARCHIVED
            CappedCoachingProposalStatus.REOPENED -> {
                when (next) {
                    CappedCoachingProposalStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedCoachingProposalStatus) {
        if (!canMoveTo(next)) {
            error("capped coaching proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"capped coaching proposal"} $code"

    fun netTotal(): Double = round((windowDays.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): CappedCoachingProposal {
        return CappedCoachingProposal(
            code = code,
            label = label,
            driverId = driverId,
            approved = approved,
            windowDays = windowDays,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped coaching proposal in status open. */
fun newCappedCoachingProposal(code: String, label: String): CappedCoachingProposal {
    return CappedCoachingProposal(
        code = code,
        label = label,
        driverId = "",
        approved = false,
        windowDays = 0,
        batchSize = 0,
        status = CappedCoachingProposalStatus.OPEN,
    )
}
