package com.acme.transport.customs.hold.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending hold proposal is in its life cycle. */
enum class PendingHoldProposalStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a pending hold proposal and its current status. */
data class PendingHoldProposal(
    var code: String,
    var name: String,
    var label: String,
    var trailerId: String,
    var archived: Boolean,
    var checksum: String,
    var weight: Double,
    var windowDays: Int,
    var channel: String,
    var status: PendingHoldProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingHoldProposalStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingHoldProposalStatus): Boolean {
        when (status) {
            PendingHoldProposalStatus.DRAFT -> {
                when (next) {
                    PendingHoldProposalStatus.PENDING, PendingHoldProposalStatus.CLOSED -> return true
                }
                return false
            }
            PendingHoldProposalStatus.PENDING -> {
                when (next) {
                    PendingHoldProposalStatus.ACTIVE -> return true
                }
                return false
            }
            PendingHoldProposalStatus.ACTIVE -> return next == PendingHoldProposalStatus.SUSPENDED || next == PendingHoldProposalStatus.CLOSED
            PendingHoldProposalStatus.SUSPENDED -> return next == PendingHoldProposalStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PendingHoldProposalStatus) {
        if (!canMoveTo(next)) {
            error("pending hold proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$name]"

    fun effectiveValue(): Double = round((windowDays.toDouble() + weight) * 100) / 100

    fun snapshot(): PendingHoldProposal {
        return PendingHoldProposal(
            code = code,
            name = name,
            label = label,
            trailerId = trailerId,
            archived = archived,
            checksum = checksum,
            weight = weight,
            windowDays = windowDays,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending hold proposal in status draft. */
fun newPendingHoldProposal(code: String, name: String): PendingHoldProposal {
    return PendingHoldProposal(
        code = code,
        name = name,
        label = "",
        trailerId = "",
        archived = false,
        checksum = "",
        weight = 0.0,
        windowDays = 0,
        channel = "",
        status = PendingHoldProposalStatus.DRAFT,
    )
}
