package com.acme.transport.freightpay.audit.auditpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict audit proposal is in its life cycle. */
enum class StrictAuditProposalStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One strict audit proposal, with what the planners need to act on it. */
data class StrictAuditProposal(
    var code: String,
    var title: String,
    var budget: Double,
    var depot: String,
    var updatedAt: Int,
    var limit: Int,
    var dock: String,
    var externalRef: String,
    var channel: String,
    var status: StrictAuditProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictAuditProposalStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictAuditProposalStatus): Boolean {
        when (status) {
            StrictAuditProposalStatus.DRAFT -> return next == StrictAuditProposalStatus.ACTIVE
            StrictAuditProposalStatus.PENDING -> {
                when (next) {
                    StrictAuditProposalStatus.CLOSED -> return true
                }
                return false
            }
            StrictAuditProposalStatus.ACTIVE -> {
                when (next) {
                    StrictAuditProposalStatus.SUSPENDED, StrictAuditProposalStatus.CLOSED -> return true
                }
                return false
            }
            StrictAuditProposalStatus.SUSPENDED -> return next == StrictAuditProposalStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: StrictAuditProposalStatus) {
        if (!canMoveTo(next)) {
            error("strict audit proposal $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedVolume(): Double = round(limit.toDouble() * budget * 100) / 100

    fun snapshot(): StrictAuditProposal {
        return StrictAuditProposal(
            code = code,
            title = title,
            budget = budget,
            depot = depot,
            updatedAt = updatedAt,
            limit = limit,
            dock = dock,
            externalRef = externalRef,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a strict audit proposal in status draft. */
fun newStrictAuditProposal(code: String, title: String): StrictAuditProposal {
    return StrictAuditProposal(
        code = code,
        title = title,
        budget = 0.0,
        depot = "",
        updatedAt = 0,
        limit = 0,
        dock = "",
        externalRef = "",
        channel = "",
        status = StrictAuditProposalStatus.DRAFT,
    )
}
