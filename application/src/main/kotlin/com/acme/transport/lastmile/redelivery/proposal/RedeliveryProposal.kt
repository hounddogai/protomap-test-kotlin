package com.acme.transport.lastmile.redelivery.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The redelivery proposal records of lastmile's redelivery work, and the rules, stores, and calculations
// around them.

/** Where a redelivery proposal is in its life cycle. */
enum class RedeliveryProposalStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a redelivery proposal and its current status. */
data class RedeliveryProposal(
    var code: String,
    var name: String,
    var approved: Boolean,
    var graceDays: Int,
    var createdAt: Int,
    var rate: Double,
    var phone: String,
    var limit: Int,
    var status: RedeliveryProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryProposalStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryProposalStatus): Boolean {
        when (status) {
            RedeliveryProposalStatus.PLANNED -> return next == RedeliveryProposalStatus.COMPLETED
            RedeliveryProposalStatus.SCHEDULED -> {
                when (next) {
                    RedeliveryProposalStatus.COMPLETED, RedeliveryProposalStatus.CANCELLED -> return true
                }
                return false
            }
            RedeliveryProposalStatus.IN_PROGRESS -> return next == RedeliveryProposalStatus.COMPLETED || next == RedeliveryProposalStatus.CANCELLED
            RedeliveryProposalStatus.COMPLETED -> {
                when (next) {
                    RedeliveryProposalStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RedeliveryProposalStatus) {
        if (!canMoveTo(next)) {
            error("redelivery proposal $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun weightedValue(): Double = round((createdAt.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): RedeliveryProposal {
        return RedeliveryProposal(
            code = code,
            name = name,
            approved = approved,
            graceDays = graceDays,
            createdAt = createdAt,
            rate = rate,
            phone = phone,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a redelivery proposal in status planned. */
fun newRedeliveryProposal(code: String, name: String): RedeliveryProposal {
    return RedeliveryProposal(
        code = code,
        name = name,
        approved = false,
        graceDays = 0,
        createdAt = 0,
        rate = 0.0,
        phone = "",
        limit = 0,
        status = RedeliveryProposalStatus.PLANNED,
    )
}
