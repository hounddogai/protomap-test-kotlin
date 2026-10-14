package com.acme.transport.crewing.dutyperiod.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period exception is in its life cycle. */
enum class DutyPeriodExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A duty period exception. Its code is unique within a tenant. */
data class DutyPeriodException(
    var code: String,
    var label: String,
    var source: String,
    var graceDays: Int,
    var title: String,
    var updatedAt: Int,
    var timezone: String,
    var rate: Double,
    var active: Boolean,
    var threshold: Double,
    var cost: Double,
    var status: DutyPeriodExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodExceptionStatus): Boolean {
        when (status) {
            DutyPeriodExceptionStatus.IDLE -> return next == DutyPeriodExceptionStatus.READY
            DutyPeriodExceptionStatus.WARMING -> {
                when (next) {
                    DutyPeriodExceptionStatus.READY, DutyPeriodExceptionStatus.DRAINING -> return true
                }
                return false
            }
            DutyPeriodExceptionStatus.READY -> return next == DutyPeriodExceptionStatus.DRAINING || next == DutyPeriodExceptionStatus.STOPPED
            DutyPeriodExceptionStatus.DRAINING -> {
                when (next) {
                    DutyPeriodExceptionStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodExceptionStatus) {
        if (!canMoveTo(next)) {
            error("duty period exception $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun netExposure(): Double = round((updatedAt.toDouble() - threshold) * 100) / 100

    fun snapshot(): DutyPeriodException {
        return DutyPeriodException(
            code = code,
            label = label,
            source = source,
            graceDays = graceDays,
            title = title,
            updatedAt = updatedAt,
            timezone = timezone,
            rate = rate,
            active = active,
            threshold = threshold,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a duty period exception in status idle. */
fun newDutyPeriodException(code: String, label: String): DutyPeriodException {
    return DutyPeriodException(
        code = code,
        label = label,
        source = "",
        graceDays = 0,
        title = "",
        updatedAt = 0,
        timezone = "",
        rate = 0.0,
        active = false,
        threshold = 0.0,
        cost = 0.0,
        status = DutyPeriodExceptionStatus.IDLE,
    )
}
