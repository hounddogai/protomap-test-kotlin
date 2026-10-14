package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The audit proposal records of freightpay's audit work, and the rules, stores, and calculations around them.

/** Where a audit proposal is in its life cycle. */
enum class AuditProposalStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One audit proposal, with what the planners need to act on it. */
data class AuditProposal(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var budget: Double,
    var reviewer: String,
    var threshold: Double,
    var category: String,
    var pinned: Boolean,
    var retries: Int,
    var status: AuditProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditProposalStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditProposalStatus): Boolean {
        when (status) {
            AuditProposalStatus.IDLE -> {
                when (next) {
                    AuditProposalStatus.READY -> return true
                }
                return false
            }
            AuditProposalStatus.WARMING -> return next == AuditProposalStatus.READY
            AuditProposalStatus.READY -> {
                when (next) {
                    AuditProposalStatus.DRAINING -> return true
                }
                return false
            }
            AuditProposalStatus.DRAINING -> return next == AuditProposalStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AuditProposalStatus) {
        if (!canMoveTo(next)) {
            error("audit proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedExposure(): Double = round(budget * retries.toDouble() * 100) / 100

    fun snapshot(): AuditProposal {
        return AuditProposal(
            code = code,
            name = name,
            attributes = attributes,
            budget = budget,
            reviewer = reviewer,
            threshold = threshold,
            category = category,
            pinned = pinned,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a audit proposal in status idle. */
fun newAuditProposal(code: String, name: String): AuditProposal {
    return AuditProposal(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        budget = 0.0,
        reviewer = "",
        threshold = 0.0,
        category = "",
        pinned = false,
        retries = 0,
        status = AuditProposalStatus.IDLE,
    )
}
