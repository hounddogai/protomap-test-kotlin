package com.acme.transport.crewing.dutyperiod.periodsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period audit is in its life cycle. */
enum class DutyPeriodAuditStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A duty period audit. Its code is unique within a tenant. */
data class DutyPeriodAudit(
    var code: String,
    var name: String,
    var quantity: Int,
    var contactEmail: String,
    var budget: Double,
    var summary: String,
    var durationMinutes: Int,
    var status: DutyPeriodAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodAuditStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodAuditStatus): Boolean {
        when (status) {
            DutyPeriodAuditStatus.PROPOSED -> return next == DutyPeriodAuditStatus.WITHDRAWN
            DutyPeriodAuditStatus.APPROVED -> {
                when (next) {
                    DutyPeriodAuditStatus.WITHDRAWN -> return true
                }
                return false
            }
            DutyPeriodAuditStatus.REJECTED -> return next == DutyPeriodAuditStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DutyPeriodAuditStatus) {
        if (!canMoveTo(next)) {
            error("duty period audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedTotal(): Double = round(quantity.toDouble() * budget * 100) / 100

    fun snapshot(): DutyPeriodAudit {
        return DutyPeriodAudit(
            code = code,
            name = name,
            quantity = quantity,
            contactEmail = contactEmail,
            budget = budget,
            summary = summary,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a duty period audit in status proposed. */
fun newDutyPeriodAudit(code: String, name: String): DutyPeriodAudit {
    return DutyPeriodAudit(
        code = code,
        name = name,
        quantity = 0,
        contactEmail = "",
        budget = 0.0,
        summary = "",
        durationMinutes = 0,
        status = DutyPeriodAuditStatus.PROPOSED,
    )
}
