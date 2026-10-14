package com.acme.transport.linehaul.tender.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global tender proposal is in its life cycle. */
enum class GlobalTenderProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A global tender proposal. Its code is unique within a tenant. */
data class GlobalTenderProposal(
    var code: String,
    var name: String,
    var ratio: Double,
    var capacity: Int,
    var label: String,
    var sequence: Int,
    var batchSize: Int,
    var score: Double,
    var cost: Double,
    var notes: String,
    var status: GlobalTenderProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTenderProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTenderProposalStatus): Boolean {
        when (status) {
            GlobalTenderProposalStatus.CREATED -> {
                when (next) {
                    GlobalTenderProposalStatus.VALIDATED -> return true
                }
                return false
            }
            GlobalTenderProposalStatus.VALIDATED -> return next == GlobalTenderProposalStatus.PUBLISHED || next == GlobalTenderProposalStatus.RETIRED
            GlobalTenderProposalStatus.PUBLISHED -> return next == GlobalTenderProposalStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: GlobalTenderProposalStatus) {
        if (!canMoveTo(next)) {
            error("global tender proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"global tender proposal"} $code"

    fun effectiveLoad(): Double = round(cost * sequence.toDouble() * 100) / 100

    fun snapshot(): GlobalTenderProposal {
        return GlobalTenderProposal(
            code = code,
            name = name,
            ratio = ratio,
            capacity = capacity,
            label = label,
            sequence = sequence,
            batchSize = batchSize,
            score = score,
            cost = cost,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global tender proposal in status created. */
fun newGlobalTenderProposal(code: String, name: String): GlobalTenderProposal {
    return GlobalTenderProposal(
        code = code,
        name = name,
        ratio = 0.0,
        capacity = 0,
        label = "",
        sequence = 0,
        batchSize = 0,
        score = 0.0,
        cost = 0.0,
        notes = "",
        status = GlobalTenderProposalStatus.CREATED,
    )
}
