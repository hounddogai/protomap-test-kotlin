package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report exception is in its life cycle. */
enum class CarbonReportExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A carbon report exception. Its code is unique within a tenant. */
data class CarbonReportException(
    var code: String,
    var label: String,
    var rate: Double,
    var durationMinutes: Int,
    var phone: String,
    var loadKg: Double,
    var status: CarbonReportExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportExceptionStatus): Boolean {
        when (status) {
            CarbonReportExceptionStatus.IDLE -> {
                when (next) {
                    CarbonReportExceptionStatus.WARMING -> return true
                }
                return false
            }
            CarbonReportExceptionStatus.WARMING -> return next == CarbonReportExceptionStatus.STOPPED
            CarbonReportExceptionStatus.READY -> {
                when (next) {
                    CarbonReportExceptionStatus.DRAINING, CarbonReportExceptionStatus.STOPPED -> return true
                }
                return false
            }
            CarbonReportExceptionStatus.DRAINING -> return next == CarbonReportExceptionStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CarbonReportExceptionStatus) {
        if (!canMoveTo(next)) {
            error("carbon report exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedExposure(): Double = round(durationMinutes.toDouble() * rate * 100) / 100

    fun snapshot(): CarbonReportException {
        return CarbonReportException(
            code = code,
            label = label,
            rate = rate,
            durationMinutes = durationMinutes,
            phone = phone,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a carbon report exception in status idle. */
fun newCarbonReportException(code: String, label: String): CarbonReportException {
    return CarbonReportException(
        code = code,
        label = label,
        rate = 0.0,
        durationMinutes = 0,
        phone = "",
        loadKg = 0.0,
        status = CarbonReportExceptionStatus.IDLE,
    )
}
