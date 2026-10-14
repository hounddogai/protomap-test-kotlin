package com.acme.transport.linehaul.tender.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender proposal is in its life cycle. */
enum class TenderProposalStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One tender proposal, with what the planners need to act on it. */
data class TenderProposal(
    var code: String,
    var label: String,
    var active: Boolean,
    var archived: Boolean,
    var sequence: Int,
    var discount: Double,
    var description: String,
    var contactEmail: String,
    var status: TenderProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderProposalStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderProposalStatus): Boolean {
        when (status) {
            TenderProposalStatus.PLANNED -> return next == TenderProposalStatus.COMPLETED || next == TenderProposalStatus.CANCELLED
            TenderProposalStatus.SCHEDULED -> {
                when (next) {
                    TenderProposalStatus.COMPLETED, TenderProposalStatus.CANCELLED -> return true
                }
                return false
            }
            TenderProposalStatus.IN_PROGRESS -> {
                when (next) {
                    TenderProposalStatus.COMPLETED, TenderProposalStatus.CANCELLED -> return true
                }
                return false
            }
            TenderProposalStatus.COMPLETED -> {
                when (next) {
                    TenderProposalStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderProposalStatus) {
        if (!canMoveTo(next)) {
            error("tender proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tender proposal"} $code"

    fun adjustedLoad(): Double = round(sequence.toDouble() * discount * 100) / 100

    fun snapshot(): TenderProposal {
        return TenderProposal(
            code = code,
            label = label,
            active = active,
            archived = archived,
            sequence = sequence,
            discount = discount,
            description = description,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tender proposal in status planned. */
fun newTenderProposal(code: String, label: String): TenderProposal {
    return TenderProposal(
        code = code,
        label = label,
        active = false,
        archived = false,
        sequence = 0,
        discount = 0.0,
        description = "",
        contactEmail = "",
        status = TenderProposalStatus.PLANNED,
    )
}
