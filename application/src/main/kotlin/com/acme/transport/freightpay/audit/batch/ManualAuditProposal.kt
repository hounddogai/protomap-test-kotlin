package com.acme.transport.freightpay.audit.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual audit proposal is in its life cycle. */
enum class ManualAuditProposalStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A manual audit proposal as the audit team keeps it. */
data class ManualAuditProposal(
    var code: String,
    var title: String,
    var loadKg: Double,
    var capacity: Int,
    var active: Boolean,
    var limit: Int,
    var status: ManualAuditProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAuditProposalStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAuditProposalStatus): Boolean {
        when (status) {
            ManualAuditProposalStatus.PROPOSED -> {
                when (next) {
                    ManualAuditProposalStatus.APPROVED -> return true
                }
                return false
            }
            ManualAuditProposalStatus.APPROVED -> return next == ManualAuditProposalStatus.REJECTED
            ManualAuditProposalStatus.REJECTED -> {
                when (next) {
                    ManualAuditProposalStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualAuditProposalStatus) {
        if (!canMoveTo(next)) {
            error("manual audit proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"manual audit proposal"} $code"

    fun adjustedTotal(): Double = round((loadKg + capacity.toDouble()) * 100) / 100

    fun snapshot(): ManualAuditProposal {
        return ManualAuditProposal(
            code = code,
            title = title,
            loadKg = loadKg,
            capacity = capacity,
            active = active,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual audit proposal in status proposed. */
fun newManualAuditProposal(code: String, title: String): ManualAuditProposal {
    return ManualAuditProposal(
        code = code,
        title = title,
        loadKg = 0.0,
        capacity = 0,
        active = false,
        limit = 0,
        status = ManualAuditProposalStatus.PROPOSED,
    )
}
