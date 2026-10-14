package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled audit proposal records of freightpay's audit work, and the rules, stores, and calculations
// around them.

/** Where a scheduled audit proposal is in its life cycle. */
enum class ScheduledAuditProposalStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A scheduled audit proposal as the audit team keeps it. */
data class ScheduledAuditProposal(
    var code: String,
    var name: String,
    var vehicleId: String,
    var contactEmail: String,
    var windowDays: Int,
    var stopCount: Int,
    var status: ScheduledAuditProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledAuditProposalStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledAuditProposalStatus): Boolean {
        when (status) {
            ScheduledAuditProposalStatus.IDLE -> return next == ScheduledAuditProposalStatus.WARMING || next == ScheduledAuditProposalStatus.STOPPED
            ScheduledAuditProposalStatus.WARMING -> {
                when (next) {
                    ScheduledAuditProposalStatus.READY, ScheduledAuditProposalStatus.DRAINING -> return true
                }
                return false
            }
            ScheduledAuditProposalStatus.READY -> return next == ScheduledAuditProposalStatus.DRAINING || next == ScheduledAuditProposalStatus.STOPPED
            ScheduledAuditProposalStatus.DRAINING -> return next == ScheduledAuditProposalStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ScheduledAuditProposalStatus) {
        if (!canMoveTo(next)) {
            error("scheduled audit proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netLoad(): Double = round(stopCount.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): ScheduledAuditProposal {
        return ScheduledAuditProposal(
            code = code,
            name = name,
            vehicleId = vehicleId,
            contactEmail = contactEmail,
            windowDays = windowDays,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled audit proposal in status idle. */
fun newScheduledAuditProposal(code: String, name: String): ScheduledAuditProposal {
    return ScheduledAuditProposal(
        code = code,
        name = name,
        vehicleId = "",
        contactEmail = "",
        windowDays = 0,
        stopCount = 0,
        status = ScheduledAuditProposalStatus.IDLE,
    )
}
