package com.acme.transport.emissions.carbonreport.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report audit is in its life cycle. */
enum class CarbonReportAuditStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a carbon report audit and its current status. */
data class CarbonReportAudit(
    var code: String,
    var label: String,
    var stopCount: Int,
    var expiresAt: Int,
    var odometer: Int,
    var lane: String,
    var status: CarbonReportAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportAuditStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportAuditStatus): Boolean {
        when (status) {
            CarbonReportAuditStatus.PLANNED -> {
                when (next) {
                    CarbonReportAuditStatus.IN_PROGRESS, CarbonReportAuditStatus.COMPLETED -> return true
                }
                return false
            }
            CarbonReportAuditStatus.SCHEDULED -> {
                when (next) {
                    CarbonReportAuditStatus.IN_PROGRESS, CarbonReportAuditStatus.CANCELLED -> return true
                }
                return false
            }
            CarbonReportAuditStatus.IN_PROGRESS -> return next == CarbonReportAuditStatus.COMPLETED || next == CarbonReportAuditStatus.CANCELLED
            CarbonReportAuditStatus.COMPLETED -> return next == CarbonReportAuditStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CarbonReportAuditStatus) {
        if (!canMoveTo(next)) {
            error("carbon report audit $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveVolume(): Double = round((expiresAt.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): CarbonReportAudit {
        return CarbonReportAudit(
            code = code,
            label = label,
            stopCount = stopCount,
            expiresAt = expiresAt,
            odometer = odometer,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a carbon report audit in status planned. */
fun newCarbonReportAudit(code: String, label: String): CarbonReportAudit {
    return CarbonReportAudit(
        code = code,
        label = label,
        stopCount = 0,
        expiresAt = 0,
        odometer = 0,
        lane = "",
        status = CarbonReportAuditStatus.PLANNED,
    )
}
