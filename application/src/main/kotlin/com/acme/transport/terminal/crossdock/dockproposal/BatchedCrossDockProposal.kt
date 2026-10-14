package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched cross dock proposal records of terminal's cross dock work, and the rules, stores, and
// calculations around them.

/** Where a batched cross dock proposal is in its life cycle. */
enum class BatchedCrossDockProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One batched cross dock proposal, with what the planners need to act on it. */
data class BatchedCrossDockProposal(
    var code: String,
    var title: String,
    var ownerId: String,
    var capacity: Int,
    var retries: Int,
    var reviewer: String,
    var distanceKm: Double,
    var revision: Int,
    var status: BatchedCrossDockProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCrossDockProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCrossDockProposalStatus): Boolean {
        when (status) {
            BatchedCrossDockProposalStatus.REQUESTED -> return next == BatchedCrossDockProposalStatus.REVOKED || next == BatchedCrossDockProposalStatus.EXPIRED
            BatchedCrossDockProposalStatus.GRANTED -> {
                when (next) {
                    BatchedCrossDockProposalStatus.REVOKED -> return true
                }
                return false
            }
            BatchedCrossDockProposalStatus.REVOKED -> {
                when (next) {
                    BatchedCrossDockProposalStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedCrossDockProposalStatus) {
        if (!canMoveTo(next)) {
            error("batched cross dock proposal $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"batched cross dock proposal"} $code"

    fun netExposure(): Double = round((capacity.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): BatchedCrossDockProposal {
        return BatchedCrossDockProposal(
            code = code,
            title = title,
            ownerId = ownerId,
            capacity = capacity,
            retries = retries,
            reviewer = reviewer,
            distanceKm = distanceKm,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched cross dock proposal in status requested. */
fun newBatchedCrossDockProposal(code: String, title: String): BatchedCrossDockProposal {
    return BatchedCrossDockProposal(
        code = code,
        title = title,
        ownerId = "",
        capacity = 0,
        retries = 0,
        reviewer = "",
        distanceKm = 0.0,
        revision = 0,
        status = BatchedCrossDockProposalStatus.REQUESTED,
    )
}
