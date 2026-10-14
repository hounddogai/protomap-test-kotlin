package com.acme.transport.freightpay.audit.template

import com.acme.transport.kit.text.normalize

/** Where a audit schedule is in its life cycle. */
enum class AuditScheduleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One audit schedule, with what the planners need to act on it. */
data class AuditSchedule(
    var code: String,
    var name: String,
    var approved: Boolean,
    var windowDays: Int,
    var depot: String,
    var status: AuditScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditScheduleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditScheduleStatus): Boolean {
        when (status) {
            AuditScheduleStatus.CREATED -> {
                when (next) {
                    AuditScheduleStatus.PUBLISHED, AuditScheduleStatus.RETIRED -> return true
                }
                return false
            }
            AuditScheduleStatus.VALIDATED -> return next == AuditScheduleStatus.PUBLISHED || next == AuditScheduleStatus.RETIRED
            AuditScheduleStatus.PUBLISHED -> {
                when (next) {
                    AuditScheduleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditScheduleStatus) {
        if (!canMoveTo(next)) {
            error("audit schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): AuditSchedule {
        return AuditSchedule(
            code = code,
            name = name,
            approved = approved,
            windowDays = windowDays,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a audit schedule in status created. */
fun newAuditSchedule(code: String, name: String): AuditSchedule {
    return AuditSchedule(
        code = code,
        name = name,
        approved = false,
        windowDays = 0,
        depot = "",
        status = AuditScheduleStatus.CREATED,
    )
}
