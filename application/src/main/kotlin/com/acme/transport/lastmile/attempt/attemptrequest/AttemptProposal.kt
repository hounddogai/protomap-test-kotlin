package com.acme.transport.lastmile.attempt.attemptrequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a attempt proposal is in its life cycle. */
enum class AttemptProposalStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A attempt proposal as the attempt team keeps it. */
data class AttemptProposal(
    var code: String,
    var label: String,
    var pallets: Int,
    var segment: String,
    var attempts: Int,
    var odometer: Int,
    var status: AttemptProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptProposalStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptProposalStatus): Boolean {
        when (status) {
            AttemptProposalStatus.NEW -> return next == AttemptProposalStatus.RUNNING
            AttemptProposalStatus.QUEUED -> return next == AttemptProposalStatus.RUNNING || next == AttemptProposalStatus.FAILED
            AttemptProposalStatus.RUNNING -> return next == AttemptProposalStatus.DONE
            AttemptProposalStatus.DONE -> {
                when (next) {
                    AttemptProposalStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AttemptProposalStatus) {
        if (!canMoveTo(next)) {
            error("attempt proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveTotal(): Double = round((odometer.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): AttemptProposal {
        return AttemptProposal(
            code = code,
            label = label,
            pallets = pallets,
            segment = segment,
            attempts = attempts,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a attempt proposal in status new. */
fun newAttemptProposal(code: String, label: String): AttemptProposal {
    return AttemptProposal(
        code = code,
        label = label,
        pallets = 0,
        segment = "",
        attempts = 0,
        odometer = 0,
        status = AttemptProposalStatus.NEW,
    )
}
