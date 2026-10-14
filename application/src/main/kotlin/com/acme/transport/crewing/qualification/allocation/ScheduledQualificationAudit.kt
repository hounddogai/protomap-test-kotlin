package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled qualification audit is in its life cycle. */
enum class ScheduledQualificationAuditStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A scheduled qualification audit as the qualification team keeps it. */
data class ScheduledQualificationAudit(
    var code: String,
    var title: String,
    var approved: Boolean,
    var odometer: Int,
    var cost: Double,
    var retries: Int,
    var status: ScheduledQualificationAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledQualificationAuditStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledQualificationAuditStatus): Boolean {
        when (status) {
            ScheduledQualificationAuditStatus.IDLE -> {
                when (next) {
                    ScheduledQualificationAuditStatus.WARMING -> return true
                }
                return false
            }
            ScheduledQualificationAuditStatus.WARMING -> {
                when (next) {
                    ScheduledQualificationAuditStatus.READY, ScheduledQualificationAuditStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledQualificationAuditStatus.READY -> {
                when (next) {
                    ScheduledQualificationAuditStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledQualificationAuditStatus.DRAINING -> {
                when (next) {
                    ScheduledQualificationAuditStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledQualificationAuditStatus) {
        if (!canMoveTo(next)) {
            error("scheduled qualification audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled qualification audit"} $code"

    fun adjustedLoad(): Double = round(odometer.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): ScheduledQualificationAudit {
        return ScheduledQualificationAudit(
            code = code,
            title = title,
            approved = approved,
            odometer = odometer,
            cost = cost,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled qualification audit in status idle. */
fun newScheduledQualificationAudit(code: String, title: String): ScheduledQualificationAudit {
    return ScheduledQualificationAudit(
        code = code,
        title = title,
        approved = false,
        odometer = 0,
        cost = 0.0,
        retries = 0,
        status = ScheduledQualificationAuditStatus.IDLE,
    )
}
