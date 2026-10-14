package com.acme.transport.terminal.crossdock.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The cross dock proposal records of terminal's cross dock work, and the rules, stores, and calculations
// around them.

/** Where a cross dock proposal is in its life cycle. */
enum class CrossDockProposalStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a cross dock proposal and its current status. */
data class CrossDockProposal(
    var code: String,
    var name: String,
    var archived: Boolean,
    var quantity: Int,
    var channel: String,
    var score: Double,
    var carrier: String,
    var locale: String,
    var status: CrossDockProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockProposalStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockProposalStatus): Boolean {
        when (status) {
            CrossDockProposalStatus.NEW -> return next == CrossDockProposalStatus.QUEUED
            CrossDockProposalStatus.QUEUED -> return next == CrossDockProposalStatus.RUNNING
            CrossDockProposalStatus.RUNNING -> {
                when (next) {
                    CrossDockProposalStatus.DONE, CrossDockProposalStatus.FAILED -> return true
                }
                return false
            }
            CrossDockProposalStatus.DONE -> {
                when (next) {
                    CrossDockProposalStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CrossDockProposalStatus) {
        if (!canMoveTo(next)) {
            error("cross dock proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedLoad(): Double = round((quantity.toDouble() + score) * 100) / 100

    fun snapshot(): CrossDockProposal {
        return CrossDockProposal(
            code = code,
            name = name,
            archived = archived,
            quantity = quantity,
            channel = channel,
            score = score,
            carrier = carrier,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a cross dock proposal in status new. */
fun newCrossDockProposal(code: String, name: String): CrossDockProposal {
    return CrossDockProposal(
        code = code,
        name = name,
        archived = false,
        quantity = 0,
        channel = "",
        score = 0.0,
        carrier = "",
        locale = "",
        status = CrossDockProposalStatus.NEW,
    )
}
