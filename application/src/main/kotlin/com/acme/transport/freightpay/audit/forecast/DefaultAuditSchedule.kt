package com.acme.transport.freightpay.audit.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default audit schedule is in its life cycle. */
enum class DefaultAuditScheduleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A default audit schedule as the audit team keeps it. */
data class DefaultAuditSchedule(
    var code: String,
    var name: String,
    var amount: Double,
    var endsAt: Int,
    var budget: Double,
    var status: DefaultAuditScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultAuditScheduleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultAuditScheduleStatus): Boolean {
        when (status) {
            DefaultAuditScheduleStatus.OPEN -> return next == DefaultAuditScheduleStatus.IN_REVIEW
            DefaultAuditScheduleStatus.IN_REVIEW -> {
                when (next) {
                    DefaultAuditScheduleStatus.RESOLVED, DefaultAuditScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultAuditScheduleStatus.RESOLVED -> {
                when (next) {
                    DefaultAuditScheduleStatus.REOPENED, DefaultAuditScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultAuditScheduleStatus.REOPENED -> return next == DefaultAuditScheduleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DefaultAuditScheduleStatus) {
        if (!canMoveTo(next)) {
            error("default audit schedule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun weightedVolume(): Double = round((amount - budget) * 100) / 100

    fun snapshot(): DefaultAuditSchedule {
        return DefaultAuditSchedule(
            code = code,
            name = name,
            amount = amount,
            endsAt = endsAt,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default audit schedule in status open. */
fun newDefaultAuditSchedule(code: String, name: String): DefaultAuditSchedule {
    return DefaultAuditSchedule(
        code = code,
        name = name,
        amount = 0.0,
        endsAt = 0,
        budget = 0.0,
        status = DefaultAuditScheduleStatus.OPEN,
    )
}
