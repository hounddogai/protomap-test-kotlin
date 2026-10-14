package com.acme.transport.freightpay.audit.auditclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global audit milestone is in its life cycle. */
enum class GlobalAuditMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global audit milestone. Its code is unique within a tenant. */
data class GlobalAuditMilestone(
    var code: String,
    var label: String,
    var description: String,
    var shiftCode: String,
    var notes: String,
    var limit: Int,
    var batchSize: Int,
    var region: String,
    var vehicleId: String,
    var dock: String,
    var status: GlobalAuditMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAuditMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAuditMilestoneStatus): Boolean {
        when (status) {
            GlobalAuditMilestoneStatus.REQUESTED -> return next == GlobalAuditMilestoneStatus.GRANTED || next == GlobalAuditMilestoneStatus.REVOKED
            GlobalAuditMilestoneStatus.GRANTED -> {
                when (next) {
                    GlobalAuditMilestoneStatus.REVOKED -> return true
                }
                return false
            }
            GlobalAuditMilestoneStatus.REVOKED -> {
                when (next) {
                    GlobalAuditMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalAuditMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("global audit milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netExposure(): Double = round((batchSize.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): GlobalAuditMilestone {
        return GlobalAuditMilestone(
            code = code,
            label = label,
            description = description,
            shiftCode = shiftCode,
            notes = notes,
            limit = limit,
            batchSize = batchSize,
            region = region,
            vehicleId = vehicleId,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a global audit milestone in status requested. */
fun newGlobalAuditMilestone(code: String, label: String): GlobalAuditMilestone {
    return GlobalAuditMilestone(
        code = code,
        label = label,
        description = "",
        shiftCode = "",
        notes = "",
        limit = 0,
        batchSize = 0,
        region = "",
        vehicleId = "",
        dock = "",
        status = GlobalAuditMilestoneStatus.REQUESTED,
    )
}
