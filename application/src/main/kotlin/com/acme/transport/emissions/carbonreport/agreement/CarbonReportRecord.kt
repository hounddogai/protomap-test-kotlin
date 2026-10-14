package com.acme.transport.emissions.carbonreport.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report record is in its life cycle. */
enum class CarbonReportRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a carbon report record and its current status. */
data class CarbonReportRecord(
    var code: String,
    var label: String,
    var revision: Int,
    var driverId: String,
    var version: Int,
    var reviewer: String,
    var threshold: Double,
    var category: String,
    var startsAt: Int,
    var source: String,
    var status: CarbonReportRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportRecordStatus): Boolean {
        when (status) {
            CarbonReportRecordStatus.PROPOSED -> return next == CarbonReportRecordStatus.REJECTED
            CarbonReportRecordStatus.APPROVED -> {
                when (next) {
                    CarbonReportRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
            CarbonReportRecordStatus.REJECTED -> return next == CarbonReportRecordStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CarbonReportRecordStatus) {
        if (!canMoveTo(next)) {
            error("carbon report record $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "${"carbon report record"} $code"

    fun netVolume(): Double = round((version.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): CarbonReportRecord {
        return CarbonReportRecord(
            code = code,
            label = label,
            revision = revision,
            driverId = driverId,
            version = version,
            reviewer = reviewer,
            threshold = threshold,
            category = category,
            startsAt = startsAt,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a carbon report record in status proposed. */
fun newCarbonReportRecord(code: String, label: String): CarbonReportRecord {
    return CarbonReportRecord(
        code = code,
        label = label,
        revision = 0,
        driverId = "",
        version = 0,
        reviewer = "",
        threshold = 0.0,
        category = "",
        startsAt = 0,
        source = "",
        status = CarbonReportRecordStatus.PROPOSED,
    )
}
