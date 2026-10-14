package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled duty period estimate records of crewing's duty period work, and the rules, stores, and
// calculations around them.

/** Where a scheduled duty period estimate is in its life cycle. */
enum class ScheduledDutyPeriodEstimateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scheduled duty period estimate as the duty period team keeps it. */
data class ScheduledDutyPeriodEstimate(
    var code: String,
    var title: String,
    var graceDays: Int,
    var weight: Double,
    var locale: String,
    var checksum: String,
    var createdAt: Int,
    var status: ScheduledDutyPeriodEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDutyPeriodEstimateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDutyPeriodEstimateStatus): Boolean {
        when (status) {
            ScheduledDutyPeriodEstimateStatus.DRAFT -> return next == ScheduledDutyPeriodEstimateStatus.SUSPENDED
            ScheduledDutyPeriodEstimateStatus.PENDING -> {
                when (next) {
                    ScheduledDutyPeriodEstimateStatus.ACTIVE, ScheduledDutyPeriodEstimateStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledDutyPeriodEstimateStatus.ACTIVE -> {
                when (next) {
                    ScheduledDutyPeriodEstimateStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledDutyPeriodEstimateStatus.SUSPENDED -> {
                when (next) {
                    ScheduledDutyPeriodEstimateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledDutyPeriodEstimateStatus) {
        if (!canMoveTo(next)) {
            error("scheduled duty period estimate $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"scheduled duty period estimate"} $code"

    fun weightedVolume(): Double = round((weight + createdAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledDutyPeriodEstimate {
        return ScheduledDutyPeriodEstimate(
            code = code,
            title = title,
            graceDays = graceDays,
            weight = weight,
            locale = locale,
            checksum = checksum,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a scheduled duty period estimate in status draft. */
fun newScheduledDutyPeriodEstimate(code: String, title: String): ScheduledDutyPeriodEstimate {
    return ScheduledDutyPeriodEstimate(
        code = code,
        title = title,
        graceDays = 0,
        weight = 0.0,
        locale = "",
        checksum = "",
        createdAt = 0,
        status = ScheduledDutyPeriodEstimateStatus.DRAFT,
    )
}
