package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict duty period estimate is in its life cycle. */
enum class StrictDutyPeriodEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict duty period estimate as the duty period team keeps it. */
data class StrictDutyPeriodEstimate(
    var code: String,
    var name: String,
    var country: String,
    var timezone: String,
    var margin: Double,
    var category: String,
    var stopCount: Int,
    var active: Boolean,
    var status: StrictDutyPeriodEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDutyPeriodEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDutyPeriodEstimateStatus): Boolean {
        when (status) {
            StrictDutyPeriodEstimateStatus.OPEN -> {
                when (next) {
                    StrictDutyPeriodEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictDutyPeriodEstimateStatus.IN_REVIEW -> {
                when (next) {
                    StrictDutyPeriodEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictDutyPeriodEstimateStatus.RESOLVED -> return next == StrictDutyPeriodEstimateStatus.REOPENED || next == StrictDutyPeriodEstimateStatus.ARCHIVED
            StrictDutyPeriodEstimateStatus.REOPENED -> return next == StrictDutyPeriodEstimateStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: StrictDutyPeriodEstimateStatus) {
        if (!canMoveTo(next)) {
            error("strict duty period estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedLoad(): Double = round((margin - stopCount.toDouble()) * 100) / 100

    fun snapshot(): StrictDutyPeriodEstimate {
        return StrictDutyPeriodEstimate(
            code = code,
            name = name,
            country = country,
            timezone = timezone,
            margin = margin,
            category = category,
            stopCount = stopCount,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a strict duty period estimate in status open. */
fun newStrictDutyPeriodEstimate(code: String, name: String): StrictDutyPeriodEstimate {
    return StrictDutyPeriodEstimate(
        code = code,
        name = name,
        country = "",
        timezone = "",
        margin = 0.0,
        category = "",
        stopCount = 0,
        active = false,
        status = StrictDutyPeriodEstimateStatus.OPEN,
    )
}
