package com.acme.transport.emissions.carbonreport.strictreportadjustment

import com.acme.transport.kit.text.normalize

/** Where a global carbon report record is in its life cycle. */
enum class GlobalCarbonReportRecordStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One global carbon report record, with what the planners need to act on it. */
data class GlobalCarbonReportRecord(
    var code: String,
    var label: String,
    var trailerId: String,
    var score: Double,
    var pinned: Boolean,
    var visible: Boolean,
    var status: GlobalCarbonReportRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCarbonReportRecordStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCarbonReportRecordStatus): Boolean {
        when (status) {
            GlobalCarbonReportRecordStatus.PLANNED -> {
                when (next) {
                    GlobalCarbonReportRecordStatus.COMPLETED, GlobalCarbonReportRecordStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalCarbonReportRecordStatus.SCHEDULED -> {
                when (next) {
                    GlobalCarbonReportRecordStatus.IN_PROGRESS, GlobalCarbonReportRecordStatus.COMPLETED -> return true
                }
                return false
            }
            GlobalCarbonReportRecordStatus.IN_PROGRESS -> {
                when (next) {
                    GlobalCarbonReportRecordStatus.COMPLETED -> return true
                }
                return false
            }
            GlobalCarbonReportRecordStatus.COMPLETED -> {
                when (next) {
                    GlobalCarbonReportRecordStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalCarbonReportRecordStatus) {
        if (!canMoveTo(next)) {
            error("global carbon report record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"global carbon report record"} $code"

    fun measuredValue(): Double = score

    fun snapshot(): GlobalCarbonReportRecord {
        return GlobalCarbonReportRecord(
            code = code,
            label = label,
            trailerId = trailerId,
            score = score,
            pinned = pinned,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global carbon report record in status planned. */
fun newGlobalCarbonReportRecord(code: String, label: String): GlobalCarbonReportRecord {
    return GlobalCarbonReportRecord(
        code = code,
        label = label,
        trailerId = "",
        score = 0.0,
        pinned = false,
        visible = false,
        status = GlobalCarbonReportRecordStatus.PLANNED,
    )
}
