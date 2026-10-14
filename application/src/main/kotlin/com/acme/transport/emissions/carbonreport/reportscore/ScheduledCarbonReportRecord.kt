package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled carbon report record is in its life cycle. */
enum class ScheduledCarbonReportRecordStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scheduled carbon report record as the carbon report team keeps it. */
data class ScheduledCarbonReportRecord(
    var code: String,
    var name: String,
    var startsAt: Int,
    var vehicleId: String,
    var batchSize: Int,
    var status: ScheduledCarbonReportRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledCarbonReportRecordStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledCarbonReportRecordStatus): Boolean {
        when (status) {
            ScheduledCarbonReportRecordStatus.DRAFT -> {
                when (next) {
                    ScheduledCarbonReportRecordStatus.PENDING, ScheduledCarbonReportRecordStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledCarbonReportRecordStatus.PENDING -> return next == ScheduledCarbonReportRecordStatus.CLOSED
            ScheduledCarbonReportRecordStatus.ACTIVE -> return next == ScheduledCarbonReportRecordStatus.CLOSED
            ScheduledCarbonReportRecordStatus.SUSPENDED -> {
                when (next) {
                    ScheduledCarbonReportRecordStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledCarbonReportRecordStatus) {
        if (!canMoveTo(next)) {
            error("scheduled carbon report record $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"scheduled carbon report record"} $code"

    fun weightedLoad(): Double = round((startsAt.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): ScheduledCarbonReportRecord {
        return ScheduledCarbonReportRecord(
            code = code,
            name = name,
            startsAt = startsAt,
            vehicleId = vehicleId,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled carbon report record in status draft. */
fun newScheduledCarbonReportRecord(code: String, name: String): ScheduledCarbonReportRecord {
    return ScheduledCarbonReportRecord(
        code = code,
        name = name,
        startsAt = 0,
        vehicleId = "",
        batchSize = 0,
        status = ScheduledCarbonReportRecordStatus.DRAFT,
    )
}
