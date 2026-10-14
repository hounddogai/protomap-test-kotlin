package com.acme.transport.freightpay.audit.lenientauditproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global audit proposal records of freightpay's audit work, and the rules, stores, and calculations
// around them.

/** Where a global audit proposal is in its life cycle. */
enum class GlobalAuditProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a global audit proposal and its current status. */
data class GlobalAuditProposal(
    var code: String,
    var title: String,
    var locale: String,
    var region: String,
    var unit: String,
    var floorValue: Double,
    var source: String,
    var summary: String,
    var createdAt: Int,
    var attempts: Int,
    var revision: Int,
    var status: GlobalAuditProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAuditProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAuditProposalStatus): Boolean {
        when (status) {
            GlobalAuditProposalStatus.REQUESTED -> return next == GlobalAuditProposalStatus.GRANTED || next == GlobalAuditProposalStatus.EXPIRED
            GlobalAuditProposalStatus.GRANTED -> return next == GlobalAuditProposalStatus.REVOKED || next == GlobalAuditProposalStatus.EXPIRED
            GlobalAuditProposalStatus.REVOKED -> {
                when (next) {
                    GlobalAuditProposalStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalAuditProposalStatus) {
        if (!canMoveTo(next)) {
            error("global audit proposal $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun netLoad(): Double = round(createdAt.toDouble() * attempts.toDouble() * 100) / 100

    fun snapshot(): GlobalAuditProposal {
        return GlobalAuditProposal(
            code = code,
            title = title,
            locale = locale,
            region = region,
            unit = unit,
            floorValue = floorValue,
            source = source,
            summary = summary,
            createdAt = createdAt,
            attempts = attempts,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a global audit proposal in status requested. */
fun newGlobalAuditProposal(code: String, title: String): GlobalAuditProposal {
    return GlobalAuditProposal(
        code = code,
        title = title,
        locale = "",
        region = "",
        unit = "",
        floorValue = 0.0,
        source = "",
        summary = "",
        createdAt = 0,
        attempts = 0,
        revision = 0,
        status = GlobalAuditProposalStatus.REQUESTED,
    )
}
