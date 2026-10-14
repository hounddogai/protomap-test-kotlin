package com.acme.transport.network.hub.hubestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict hub proposal is in its life cycle. */
enum class StrictHubProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One strict hub proposal, with what the planners need to act on it. */
data class StrictHubProposal(
    var code: String,
    var title: String,
    var trailerId: String,
    var version: Int,
    var stopCount: Int,
    var approved: Boolean,
    var score: Double,
    var externalRef: String,
    var carrier: String,
    var status: StrictHubProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictHubProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictHubProposalStatus): Boolean {
        when (status) {
            StrictHubProposalStatus.OPEN -> return next == StrictHubProposalStatus.ARCHIVED
            StrictHubProposalStatus.IN_REVIEW -> {
                when (next) {
                    StrictHubProposalStatus.RESOLVED -> return true
                }
                return false
            }
            StrictHubProposalStatus.RESOLVED -> return next == StrictHubProposalStatus.REOPENED || next == StrictHubProposalStatus.ARCHIVED
            StrictHubProposalStatus.REOPENED -> {
                when (next) {
                    StrictHubProposalStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictHubProposalStatus) {
        if (!canMoveTo(next)) {
            error("strict hub proposal $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun effectiveValue(): Double = round((score + version.toDouble()) * 100) / 100

    fun snapshot(): StrictHubProposal {
        return StrictHubProposal(
            code = code,
            title = title,
            trailerId = trailerId,
            version = version,
            stopCount = stopCount,
            approved = approved,
            score = score,
            externalRef = externalRef,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict hub proposal in status open. */
fun newStrictHubProposal(code: String, title: String): StrictHubProposal {
    return StrictHubProposal(
        code = code,
        title = title,
        trailerId = "",
        version = 0,
        stopCount = 0,
        approved = false,
        score = 0.0,
        externalRef = "",
        carrier = "",
        status = StrictHubProposalStatus.OPEN,
    )
}
