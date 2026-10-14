package com.acme.transport.freightpay.freightinvoice.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The freight invoice proposal records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a freight invoice proposal is in its life cycle. */
enum class FreightInvoiceProposalStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One freight invoice proposal, with what the planners need to act on it. */
data class FreightInvoiceProposal(
    var code: String,
    var label: String,
    var segment: String,
    var approved: Boolean,
    var trailerId: String,
    var sequence: Int,
    var odometer: Int,
    var shiftCode: String,
    var status: FreightInvoiceProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceProposalStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceProposalStatus): Boolean {
        when (status) {
            FreightInvoiceProposalStatus.NEW -> {
                when (next) {
                    FreightInvoiceProposalStatus.QUEUED, FreightInvoiceProposalStatus.FAILED -> return true
                }
                return false
            }
            FreightInvoiceProposalStatus.QUEUED -> return next == FreightInvoiceProposalStatus.RUNNING || next == FreightInvoiceProposalStatus.DONE
            FreightInvoiceProposalStatus.RUNNING -> {
                when (next) {
                    FreightInvoiceProposalStatus.DONE -> return true
                }
                return false
            }
            FreightInvoiceProposalStatus.DONE -> {
                when (next) {
                    FreightInvoiceProposalStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceProposalStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedVolume(): Double = round(sequence.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): FreightInvoiceProposal {
        return FreightInvoiceProposal(
            code = code,
            label = label,
            segment = segment,
            approved = approved,
            trailerId = trailerId,
            sequence = sequence,
            odometer = odometer,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a freight invoice proposal in status new. */
fun newFreightInvoiceProposal(code: String, label: String): FreightInvoiceProposal {
    return FreightInvoiceProposal(
        code = code,
        label = label,
        segment = "",
        approved = false,
        trailerId = "",
        sequence = 0,
        odometer = 0,
        shiftCode = "",
        status = FreightInvoiceProposalStatus.NEW,
    )
}
