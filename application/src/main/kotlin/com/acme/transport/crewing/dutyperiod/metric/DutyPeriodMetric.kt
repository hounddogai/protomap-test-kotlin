package com.acme.transport.crewing.dutyperiod.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The duty period metric records of crewing's duty period work, and the rules, stores, and calculations
// around them.

/** Where a duty period metric is in its life cycle. */
enum class DutyPeriodMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A duty period metric as the duty period team keeps it. */
data class DutyPeriodMetric(
    var code: String,
    var title: String,
    var lane: String,
    var updatedAt: Int,
    var externalRef: String,
    var revision: Int,
    var odometer: Int,
    var ownerId: String,
    var notes: String,
    var maximum: Double,
    var budget: Double,
    var status: DutyPeriodMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodMetricStatus): Boolean {
        when (status) {
            DutyPeriodMetricStatus.REQUESTED -> {
                when (next) {
                    DutyPeriodMetricStatus.GRANTED, DutyPeriodMetricStatus.EXPIRED -> return true
                }
                return false
            }
            DutyPeriodMetricStatus.GRANTED -> return next == DutyPeriodMetricStatus.REVOKED || next == DutyPeriodMetricStatus.EXPIRED
            DutyPeriodMetricStatus.REVOKED -> return next == DutyPeriodMetricStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DutyPeriodMetricStatus) {
        if (!canMoveTo(next)) {
            error("duty period metric $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code ($title)"

    fun projectedExposure(): Double = round((revision.toDouble() + maximum) * 100) / 100

    fun snapshot(): DutyPeriodMetric {
        return DutyPeriodMetric(
            code = code,
            title = title,
            lane = lane,
            updatedAt = updatedAt,
            externalRef = externalRef,
            revision = revision,
            odometer = odometer,
            ownerId = ownerId,
            notes = notes,
            maximum = maximum,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a duty period metric in status requested. */
fun newDutyPeriodMetric(code: String, title: String): DutyPeriodMetric {
    return DutyPeriodMetric(
        code = code,
        title = title,
        lane = "",
        updatedAt = 0,
        externalRef = "",
        revision = 0,
        odometer = 0,
        ownerId = "",
        notes = "",
        maximum = 0.0,
        budget = 0.0,
        status = DutyPeriodMetricStatus.REQUESTED,
    )
}
