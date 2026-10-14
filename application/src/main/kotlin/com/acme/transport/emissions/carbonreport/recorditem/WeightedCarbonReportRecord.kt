package com.acme.transport.emissions.carbonreport.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted carbon report record records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a weighted carbon report record is in its life cycle. */
enum class WeightedCarbonReportRecordStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One weighted carbon report record, with what the planners need to act on it. */
data class WeightedCarbonReportRecord(
    var code: String,
    var label: String,
    var category: String,
    var weight: Double,
    var priority: Int,
    var capacity: Int,
    var windowDays: Int,
    var status: WeightedCarbonReportRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedCarbonReportRecordStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedCarbonReportRecordStatus): Boolean {
        when (status) {
            WeightedCarbonReportRecordStatus.IDLE -> return next == WeightedCarbonReportRecordStatus.WARMING
            WeightedCarbonReportRecordStatus.WARMING -> {
                when (next) {
                    WeightedCarbonReportRecordStatus.STOPPED -> return true
                }
                return false
            }
            WeightedCarbonReportRecordStatus.READY -> {
                when (next) {
                    WeightedCarbonReportRecordStatus.STOPPED -> return true
                }
                return false
            }
            WeightedCarbonReportRecordStatus.DRAINING -> return next == WeightedCarbonReportRecordStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: WeightedCarbonReportRecordStatus) {
        if (!canMoveTo(next)) {
            error("weighted carbon report record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"weighted carbon report record"} $code"

    fun effectiveLoad(): Double = round(windowDays.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): WeightedCarbonReportRecord {
        return WeightedCarbonReportRecord(
            code = code,
            label = label,
            category = category,
            weight = weight,
            priority = priority,
            capacity = capacity,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a weighted carbon report record in status idle. */
fun newWeightedCarbonReportRecord(code: String, label: String): WeightedCarbonReportRecord {
    return WeightedCarbonReportRecord(
        code = code,
        label = label,
        category = "",
        weight = 0.0,
        priority = 0,
        capacity = 0,
        windowDays = 0,
        status = WeightedCarbonReportRecordStatus.IDLE,
    )
}
