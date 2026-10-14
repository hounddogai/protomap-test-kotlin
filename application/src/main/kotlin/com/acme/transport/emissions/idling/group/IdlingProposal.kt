package com.acme.transport.emissions.idling.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling proposal is in its life cycle. */
enum class IdlingProposalStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a idling proposal and its current status. */
data class IdlingProposal(
    var code: String,
    var label: String,
    var score: Double,
    var pinned: Boolean,
    var ownerId: String,
    var driverId: String,
    var weight: Double,
    var ratio: Double,
    var updatedAt: Int,
    var tags: MutableList<String>,
    var status: IdlingProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingProposalStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingProposalStatus): Boolean {
        when (status) {
            IdlingProposalStatus.IDLE -> {
                when (next) {
                    IdlingProposalStatus.READY, IdlingProposalStatus.DRAINING -> return true
                }
                return false
            }
            IdlingProposalStatus.WARMING -> {
                when (next) {
                    IdlingProposalStatus.DRAINING, IdlingProposalStatus.STOPPED -> return true
                }
                return false
            }
            IdlingProposalStatus.READY -> return next == IdlingProposalStatus.DRAINING
            IdlingProposalStatus.DRAINING -> return next == IdlingProposalStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: IdlingProposalStatus) {
        if (!canMoveTo(next)) {
            error("idling proposal $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedValue(): Double = round((updatedAt.toDouble() - score) * 100) / 100

    fun snapshot(): IdlingProposal {
        return IdlingProposal(
            code = code,
            label = label,
            score = score,
            pinned = pinned,
            ownerId = ownerId,
            driverId = driverId,
            weight = weight,
            ratio = ratio,
            updatedAt = updatedAt,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a idling proposal in status idle. */
fun newIdlingProposal(code: String, label: String): IdlingProposal {
    return IdlingProposal(
        code = code,
        label = label,
        score = 0.0,
        pinned = false,
        ownerId = "",
        driverId = "",
        weight = 0.0,
        ratio = 0.0,
        updatedAt = 0,
        tags = mutableListOf<String>(),
        status = IdlingProposalStatus.IDLE,
    )
}
