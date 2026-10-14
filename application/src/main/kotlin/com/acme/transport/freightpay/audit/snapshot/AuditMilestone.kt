package com.acme.transport.freightpay.audit.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit milestone is in its life cycle. */
enum class AuditMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One audit milestone, with what the planners need to act on it. */
data class AuditMilestone(
    var code: String,
    var label: String,
    var active: Boolean,
    var category: String,
    var driverId: String,
    var currency: String,
    var segment: String,
    var rate: Double,
    var vehicleId: String,
    var source: String,
    var ceiling: Double,
    var status: AuditMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditMilestoneStatus): Boolean {
        when (status) {
            AuditMilestoneStatus.REQUESTED -> return next == AuditMilestoneStatus.EXPIRED
            AuditMilestoneStatus.GRANTED -> return next == AuditMilestoneStatus.REVOKED
            AuditMilestoneStatus.REVOKED -> return next == AuditMilestoneStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AuditMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("audit milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"audit milestone"} $code"

    fun weightedLoad(): Double = round((rate - ceiling) * 100) / 100

    fun snapshot(): AuditMilestone {
        return AuditMilestone(
            code = code,
            label = label,
            active = active,
            category = category,
            driverId = driverId,
            currency = currency,
            segment = segment,
            rate = rate,
            vehicleId = vehicleId,
            source = source,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a audit milestone in status requested. */
fun newAuditMilestone(code: String, label: String): AuditMilestone {
    return AuditMilestone(
        code = code,
        label = label,
        active = false,
        category = "",
        driverId = "",
        currency = "",
        segment = "",
        rate = 0.0,
        vehicleId = "",
        source = "",
        ceiling = 0.0,
        status = AuditMilestoneStatus.REQUESTED,
    )
}
