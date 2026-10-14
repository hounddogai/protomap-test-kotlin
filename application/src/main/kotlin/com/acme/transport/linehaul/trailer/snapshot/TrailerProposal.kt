package com.acme.transport.linehaul.trailer.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer proposal is in its life cycle. */
enum class TrailerProposalStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A trailer proposal as the trailer team keeps it. */
data class TrailerProposal(
    var code: String,
    var name: String,
    var phone: String,
    var segment: String,
    var minimum: Double,
    var currency: String,
    var active: Boolean,
    var createdAt: Int,
    var status: TrailerProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerProposalStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerProposalStatus): Boolean {
        when (status) {
            TrailerProposalStatus.PROPOSED -> return next == TrailerProposalStatus.REJECTED
            TrailerProposalStatus.APPROVED -> return next == TrailerProposalStatus.WITHDRAWN
            TrailerProposalStatus.REJECTED -> {
                when (next) {
                    TrailerProposalStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerProposalStatus) {
        if (!canMoveTo(next)) {
            error("trailer proposal $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"trailer proposal"} $code"

    fun adjustedExposure(): Double = round((minimum + createdAt.toDouble()) * 100) / 100

    fun snapshot(): TrailerProposal {
        return TrailerProposal(
            code = code,
            name = name,
            phone = phone,
            segment = segment,
            minimum = minimum,
            currency = currency,
            active = active,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a trailer proposal in status proposed. */
fun newTrailerProposal(code: String, name: String): TrailerProposal {
    return TrailerProposal(
        code = code,
        name = name,
        phone = "",
        segment = "",
        minimum = 0.0,
        currency = "",
        active = false,
        createdAt = 0,
        status = TrailerProposalStatus.PROPOSED,
    )
}
