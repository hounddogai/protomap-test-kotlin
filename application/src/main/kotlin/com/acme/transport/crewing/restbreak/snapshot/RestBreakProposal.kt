package com.acme.transport.crewing.restbreak.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break proposal is in its life cycle. */
enum class RestBreakProposalStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a rest break proposal and its current status. */
data class RestBreakProposal(
    var code: String,
    var label: String,
    var amount: Double,
    var attempts: Int,
    var margin: Double,
    var status: RestBreakProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakProposalStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakProposalStatus): Boolean {
        when (status) {
            RestBreakProposalStatus.IDLE -> {
                when (next) {
                    RestBreakProposalStatus.READY, RestBreakProposalStatus.STOPPED -> return true
                }
                return false
            }
            RestBreakProposalStatus.WARMING -> {
                when (next) {
                    RestBreakProposalStatus.STOPPED -> return true
                }
                return false
            }
            RestBreakProposalStatus.READY -> return next == RestBreakProposalStatus.DRAINING || next == RestBreakProposalStatus.STOPPED
            RestBreakProposalStatus.DRAINING -> {
                when (next) {
                    RestBreakProposalStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakProposalStatus) {
        if (!canMoveTo(next)) {
            error("rest break proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rest break proposal"} $code"

    fun adjustedVolume(): Double = round(amount * attempts.toDouble() * 100) / 100

    fun snapshot(): RestBreakProposal {
        return RestBreakProposal(
            code = code,
            label = label,
            amount = amount,
            attempts = attempts,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rest break proposal in status idle. */
fun newRestBreakProposal(code: String, label: String): RestBreakProposal {
    return RestBreakProposal(
        code = code,
        label = label,
        amount = 0.0,
        attempts = 0,
        margin = 0.0,
        status = RestBreakProposalStatus.IDLE,
    )
}
