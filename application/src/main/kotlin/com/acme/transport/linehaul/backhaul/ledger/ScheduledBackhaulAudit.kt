package com.acme.transport.linehaul.backhaul.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled backhaul audit is in its life cycle. */
enum class ScheduledBackhaulAuditStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a scheduled backhaul audit and its current status. */
data class ScheduledBackhaulAudit(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var region: String,
    var rate: Double,
    var phone: String,
    var segment: String,
    var updatedAt: Int,
    var capacity: Int,
    var status: ScheduledBackhaulAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledBackhaulAuditStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledBackhaulAuditStatus): Boolean {
        when (status) {
            ScheduledBackhaulAuditStatus.REQUESTED -> return next == ScheduledBackhaulAuditStatus.REVOKED || next == ScheduledBackhaulAuditStatus.EXPIRED
            ScheduledBackhaulAuditStatus.GRANTED -> {
                when (next) {
                    ScheduledBackhaulAuditStatus.EXPIRED -> return true
                }
                return false
            }
            ScheduledBackhaulAuditStatus.REVOKED -> return next == ScheduledBackhaulAuditStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ScheduledBackhaulAuditStatus) {
        if (!canMoveTo(next)) {
            error("scheduled backhaul audit $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"scheduled backhaul audit"} $code"

    fun netValue(): Double = round((durationMinutes.toDouble() + rate) * 100) / 100

    fun snapshot(): ScheduledBackhaulAudit {
        return ScheduledBackhaulAudit(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            region = region,
            rate = rate,
            phone = phone,
            segment = segment,
            updatedAt = updatedAt,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled backhaul audit in status requested. */
fun newScheduledBackhaulAudit(code: String, label: String): ScheduledBackhaulAudit {
    return ScheduledBackhaulAudit(
        code = code,
        label = label,
        durationMinutes = 0,
        region = "",
        rate = 0.0,
        phone = "",
        segment = "",
        updatedAt = 0,
        capacity = 0,
        status = ScheduledBackhaulAuditStatus.REQUESTED,
    )
}
