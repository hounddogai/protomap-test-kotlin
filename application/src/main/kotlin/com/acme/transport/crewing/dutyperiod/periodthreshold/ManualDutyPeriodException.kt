package com.acme.transport.crewing.dutyperiod.periodthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual duty period exception is in its life cycle. */
enum class ManualDutyPeriodExceptionStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One manual duty period exception, with what the planners need to act on it. */
data class ManualDutyPeriodException(
    var code: String,
    var title: String,
    var contactEmail: String,
    var windowDays: Int,
    var priority: Int,
    var budget: Double,
    var status: ManualDutyPeriodExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDutyPeriodExceptionStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDutyPeriodExceptionStatus): Boolean {
        when (status) {
            ManualDutyPeriodExceptionStatus.REQUESTED -> return next == ManualDutyPeriodExceptionStatus.GRANTED
            ManualDutyPeriodExceptionStatus.GRANTED -> return next == ManualDutyPeriodExceptionStatus.REVOKED || next == ManualDutyPeriodExceptionStatus.EXPIRED
            ManualDutyPeriodExceptionStatus.REVOKED -> {
                when (next) {
                    ManualDutyPeriodExceptionStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualDutyPeriodExceptionStatus) {
        if (!canMoveTo(next)) {
            error("manual duty period exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"manual duty period exception"} $code"

    fun projectedExposure(): Double = round((priority.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): ManualDutyPeriodException {
        return ManualDutyPeriodException(
            code = code,
            title = title,
            contactEmail = contactEmail,
            windowDays = windowDays,
            priority = priority,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual duty period exception in status requested. */
fun newManualDutyPeriodException(code: String, title: String): ManualDutyPeriodException {
    return ManualDutyPeriodException(
        code = code,
        title = title,
        contactEmail = "",
        windowDays = 0,
        priority = 0,
        budget = 0.0,
        status = ManualDutyPeriodExceptionStatus.REQUESTED,
    )
}
