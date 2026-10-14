package com.acme.transport.terminal.dockdoor.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary dock door audit is in its life cycle. */
enum class PrimaryDockDoorAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One primary dock door audit, with what the planners need to act on it. */
data class PrimaryDockDoorAudit(
    var code: String,
    var label: String,
    var trailerId: String,
    var batchSize: Int,
    var budget: Double,
    var channel: String,
    var status: PrimaryDockDoorAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDockDoorAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDockDoorAuditStatus): Boolean {
        when (status) {
            PrimaryDockDoorAuditStatus.OPEN -> return next == PrimaryDockDoorAuditStatus.REOPENED
            PrimaryDockDoorAuditStatus.IN_REVIEW -> return next == PrimaryDockDoorAuditStatus.REOPENED
            PrimaryDockDoorAuditStatus.RESOLVED -> return next == PrimaryDockDoorAuditStatus.REOPENED
            PrimaryDockDoorAuditStatus.REOPENED -> return next == PrimaryDockDoorAuditStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PrimaryDockDoorAuditStatus) {
        if (!canMoveTo(next)) {
            error("primary dock door audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary dock door audit"} $code"

    fun weightedLoad(): Double = round((budget + batchSize.toDouble()) * 100) / 100

    fun snapshot(): PrimaryDockDoorAudit {
        return PrimaryDockDoorAudit(
            code = code,
            label = label,
            trailerId = trailerId,
            batchSize = batchSize,
            budget = budget,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a primary dock door audit in status open. */
fun newPrimaryDockDoorAudit(code: String, label: String): PrimaryDockDoorAudit {
    return PrimaryDockDoorAudit(
        code = code,
        label = label,
        trailerId = "",
        batchSize = 0,
        budget = 0.0,
        channel = "",
        status = PrimaryDockDoorAuditStatus.OPEN,
    )
}
