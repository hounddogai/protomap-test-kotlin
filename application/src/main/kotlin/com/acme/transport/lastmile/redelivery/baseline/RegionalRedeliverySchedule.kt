package com.acme.transport.lastmile.redelivery.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional redelivery schedule is in its life cycle. */
enum class RegionalRedeliveryScheduleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A regional redelivery schedule as the redelivery team keeps it. */
data class RegionalRedeliverySchedule(
    var code: String,
    var label: String,
    var retries: Int,
    var expiresAt: Int,
    var dock: String,
    var stopCount: Int,
    var locale: String,
    var description: String,
    var status: RegionalRedeliveryScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalRedeliveryScheduleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalRedeliveryScheduleStatus): Boolean {
        when (status) {
            RegionalRedeliveryScheduleStatus.OPEN -> return next == RegionalRedeliveryScheduleStatus.RESOLVED
            RegionalRedeliveryScheduleStatus.IN_REVIEW -> return next == RegionalRedeliveryScheduleStatus.ARCHIVED
            RegionalRedeliveryScheduleStatus.RESOLVED -> return next == RegionalRedeliveryScheduleStatus.ARCHIVED
            RegionalRedeliveryScheduleStatus.REOPENED -> {
                when (next) {
                    RegionalRedeliveryScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalRedeliveryScheduleStatus) {
        if (!canMoveTo(next)) {
            error("regional redelivery schedule $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedLoad(): Double = round(retries.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): RegionalRedeliverySchedule {
        return RegionalRedeliverySchedule(
            code = code,
            label = label,
            retries = retries,
            expiresAt = expiresAt,
            dock = dock,
            stopCount = stopCount,
            locale = locale,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a regional redelivery schedule in status open. */
fun newRegionalRedeliverySchedule(code: String, label: String): RegionalRedeliverySchedule {
    return RegionalRedeliverySchedule(
        code = code,
        label = label,
        retries = 0,
        expiresAt = 0,
        dock = "",
        stopCount = 0,
        locale = "",
        description = "",
        status = RegionalRedeliveryScheduleStatus.OPEN,
    )
}
