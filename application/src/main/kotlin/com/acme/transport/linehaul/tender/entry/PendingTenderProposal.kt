package com.acme.transport.linehaul.tender.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending tender proposal is in its life cycle. */
enum class PendingTenderProposalStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a pending tender proposal and its current status. */
data class PendingTenderProposal(
    var code: String,
    var name: String,
    var score: Double,
    var threshold: Double,
    var approved: Boolean,
    var country: String,
    var phone: String,
    var status: PendingTenderProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingTenderProposalStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingTenderProposalStatus): Boolean {
        when (status) {
            PendingTenderProposalStatus.IDLE -> {
                when (next) {
                    PendingTenderProposalStatus.DRAINING -> return true
                }
                return false
            }
            PendingTenderProposalStatus.WARMING -> {
                when (next) {
                    PendingTenderProposalStatus.READY -> return true
                }
                return false
            }
            PendingTenderProposalStatus.READY -> {
                when (next) {
                    PendingTenderProposalStatus.DRAINING, PendingTenderProposalStatus.STOPPED -> return true
                }
                return false
            }
            PendingTenderProposalStatus.DRAINING -> {
                when (next) {
                    PendingTenderProposalStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingTenderProposalStatus) {
        if (!canMoveTo(next)) {
            error("pending tender proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending tender proposal"} $code"

    fun adjustedTotal(): Double = round((score + threshold) * 100) / 100

    fun snapshot(): PendingTenderProposal {
        return PendingTenderProposal(
            code = code,
            name = name,
            score = score,
            threshold = threshold,
            approved = approved,
            country = country,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a pending tender proposal in status idle. */
fun newPendingTenderProposal(code: String, name: String): PendingTenderProposal {
    return PendingTenderProposal(
        code = code,
        name = name,
        score = 0.0,
        threshold = 0.0,
        approved = false,
        country = "",
        phone = "",
        status = PendingTenderProposalStatus.IDLE,
    )
}
