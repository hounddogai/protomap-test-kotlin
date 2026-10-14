package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.kit.text.normalize

/** Where a primary lease return proposal is in its life cycle. */
enum class PrimaryLeaseReturnProposalStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a primary lease return proposal and its current status. */
data class PrimaryLeaseReturnProposal(
    var code: String,
    var title: String,
    var sequence: Int,
    var segment: String,
    var carrier: String,
    var externalRef: String,
    var shiftCode: String,
    var status: PrimaryLeaseReturnProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryLeaseReturnProposalStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryLeaseReturnProposalStatus): Boolean {
        when (status) {
            PrimaryLeaseReturnProposalStatus.PLANNED -> return next == PrimaryLeaseReturnProposalStatus.IN_PROGRESS
            PrimaryLeaseReturnProposalStatus.SCHEDULED -> {
                when (next) {
                    PrimaryLeaseReturnProposalStatus.IN_PROGRESS -> return true
                }
                return false
            }
            PrimaryLeaseReturnProposalStatus.IN_PROGRESS -> {
                when (next) {
                    PrimaryLeaseReturnProposalStatus.COMPLETED, PrimaryLeaseReturnProposalStatus.CANCELLED -> return true
                }
                return false
            }
            PrimaryLeaseReturnProposalStatus.COMPLETED -> {
                when (next) {
                    PrimaryLeaseReturnProposalStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryLeaseReturnProposalStatus) {
        if (!canMoveTo(next)) {
            error("primary lease return proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary lease return proposal"} $code"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): PrimaryLeaseReturnProposal {
        return PrimaryLeaseReturnProposal(
            code = code,
            title = title,
            sequence = sequence,
            segment = segment,
            carrier = carrier,
            externalRef = externalRef,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a primary lease return proposal in status planned. */
fun newPrimaryLeaseReturnProposal(code: String, title: String): PrimaryLeaseReturnProposal {
    return PrimaryLeaseReturnProposal(
        code = code,
        title = title,
        sequence = 0,
        segment = "",
        carrier = "",
        externalRef = "",
        shiftCode = "",
        status = PrimaryLeaseReturnProposalStatus.PLANNED,
    )
}
