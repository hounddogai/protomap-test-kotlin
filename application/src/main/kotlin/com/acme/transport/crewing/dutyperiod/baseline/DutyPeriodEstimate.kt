package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period estimate is in its life cycle. */
enum class DutyPeriodEstimateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A duty period estimate. Its code is unique within a tenant. */
data class DutyPeriodEstimate(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var floorValue: Double,
    var threshold: Double,
    var amount: Double,
    var cost: Double,
    var category: String,
    var status: DutyPeriodEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodEstimateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodEstimateStatus): Boolean {
        when (status) {
            DutyPeriodEstimateStatus.CREATED -> {
                when (next) {
                    DutyPeriodEstimateStatus.PUBLISHED, DutyPeriodEstimateStatus.RETIRED -> return true
                }
                return false
            }
            DutyPeriodEstimateStatus.VALIDATED -> return next == DutyPeriodEstimateStatus.PUBLISHED || next == DutyPeriodEstimateStatus.RETIRED
            DutyPeriodEstimateStatus.PUBLISHED -> {
                when (next) {
                    DutyPeriodEstimateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodEstimateStatus) {
        if (!canMoveTo(next)) {
            error("duty period estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"duty period estimate"} $code"

    fun weightedVolume(): Double = round(distanceKm * amount * 100) / 100

    fun snapshot(): DutyPeriodEstimate {
        return DutyPeriodEstimate(
            code = code,
            label = label,
            distanceKm = distanceKm,
            floorValue = floorValue,
            threshold = threshold,
            amount = amount,
            cost = cost,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a duty period estimate in status created. */
fun newDutyPeriodEstimate(code: String, label: String): DutyPeriodEstimate {
    return DutyPeriodEstimate(
        code = code,
        label = label,
        distanceKm = 0.0,
        floorValue = 0.0,
        threshold = 0.0,
        amount = 0.0,
        cost = 0.0,
        category = "",
        status = DutyPeriodEstimateStatus.CREATED,
    )
}
