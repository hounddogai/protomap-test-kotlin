package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched attempt proposal is in its life cycle. */
enum class BatchedAttemptProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a batched attempt proposal and its current status. */
data class BatchedAttemptProposal(
    var code: String,
    var label: String,
    var active: Boolean,
    var minimum: Double,
    var batchSize: Int,
    var contactEmail: String,
    var loadKg: Double,
    var status: BatchedAttemptProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAttemptProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAttemptProposalStatus): Boolean {
        when (status) {
            BatchedAttemptProposalStatus.REQUESTED -> {
                when (next) {
                    BatchedAttemptProposalStatus.REVOKED -> return true
                }
                return false
            }
            BatchedAttemptProposalStatus.GRANTED -> return next == BatchedAttemptProposalStatus.REVOKED || next == BatchedAttemptProposalStatus.EXPIRED
            BatchedAttemptProposalStatus.REVOKED -> return next == BatchedAttemptProposalStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BatchedAttemptProposalStatus) {
        if (!canMoveTo(next)) {
            error("batched attempt proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveLoad(): Double = round(batchSize.toDouble() * loadKg * 100) / 100

    fun snapshot(): BatchedAttemptProposal {
        return BatchedAttemptProposal(
            code = code,
            label = label,
            active = active,
            minimum = minimum,
            batchSize = batchSize,
            contactEmail = contactEmail,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched attempt proposal in status requested. */
fun newBatchedAttemptProposal(code: String, label: String): BatchedAttemptProposal {
    return BatchedAttemptProposal(
        code = code,
        label = label,
        active = false,
        minimum = 0.0,
        batchSize = 0,
        contactEmail = "",
        loadKg = 0.0,
        status = BatchedAttemptProposalStatus.REQUESTED,
    )
}
