package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional duty period estimate is in its life cycle. */
enum class RegionalDutyPeriodEstimateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A regional duty period estimate. Its code is unique within a tenant. */
data class RegionalDutyPeriodEstimate(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var distanceKm: Double,
    var approved: Boolean,
    var status: RegionalDutyPeriodEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDutyPeriodEstimateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDutyPeriodEstimateStatus): Boolean {
        when (status) {
            RegionalDutyPeriodEstimateStatus.NEW -> {
                when (next) {
                    RegionalDutyPeriodEstimateStatus.QUEUED, RegionalDutyPeriodEstimateStatus.FAILED -> return true
                }
                return false
            }
            RegionalDutyPeriodEstimateStatus.QUEUED -> {
                when (next) {
                    RegionalDutyPeriodEstimateStatus.RUNNING, RegionalDutyPeriodEstimateStatus.FAILED -> return true
                }
                return false
            }
            RegionalDutyPeriodEstimateStatus.RUNNING -> {
                when (next) {
                    RegionalDutyPeriodEstimateStatus.DONE, RegionalDutyPeriodEstimateStatus.FAILED -> return true
                }
                return false
            }
            RegionalDutyPeriodEstimateStatus.DONE -> return next == RegionalDutyPeriodEstimateStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RegionalDutyPeriodEstimateStatus) {
        if (!canMoveTo(next)) {
            error("regional duty period estimate $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"regional duty period estimate"} $code"

    fun effectiveVolume(): Double = round((distanceKm - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalDutyPeriodEstimate {
        return RegionalDutyPeriodEstimate(
            code = code,
            title = title,
            updatedAt = updatedAt,
            distanceKm = distanceKm,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional duty period estimate in status new. */
fun newRegionalDutyPeriodEstimate(code: String, title: String): RegionalDutyPeriodEstimate {
    return RegionalDutyPeriodEstimate(
        code = code,
        title = title,
        updatedAt = 0,
        distanceKm = 0.0,
        approved = false,
        status = RegionalDutyPeriodEstimateStatus.NEW,
    )
}
