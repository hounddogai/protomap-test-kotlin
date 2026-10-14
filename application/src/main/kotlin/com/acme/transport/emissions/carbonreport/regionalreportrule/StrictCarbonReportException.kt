package com.acme.transport.emissions.carbonreport.regionalreportrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict carbon report exception is in its life cycle. */
enum class StrictCarbonReportExceptionStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a strict carbon report exception and its current status. */
data class StrictCarbonReportException(
    var code: String,
    var label: String,
    var maximum: Double,
    var weight: Double,
    var revision: Int,
    var status: StrictCarbonReportExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictCarbonReportExceptionStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictCarbonReportExceptionStatus): Boolean {
        when (status) {
            StrictCarbonReportExceptionStatus.PROPOSED -> {
                when (next) {
                    StrictCarbonReportExceptionStatus.WITHDRAWN -> return true
                }
                return false
            }
            StrictCarbonReportExceptionStatus.APPROVED -> {
                when (next) {
                    StrictCarbonReportExceptionStatus.REJECTED, StrictCarbonReportExceptionStatus.WITHDRAWN -> return true
                }
                return false
            }
            StrictCarbonReportExceptionStatus.REJECTED -> return next == StrictCarbonReportExceptionStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: StrictCarbonReportExceptionStatus) {
        if (!canMoveTo(next)) {
            error("strict carbon report exception $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedLoad(): Double = round((weight - revision.toDouble()) * 100) / 100

    fun snapshot(): StrictCarbonReportException {
        return StrictCarbonReportException(
            code = code,
            label = label,
            maximum = maximum,
            weight = weight,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict carbon report exception in status proposed. */
fun newStrictCarbonReportException(code: String, label: String): StrictCarbonReportException {
    return StrictCarbonReportException(
        code = code,
        label = label,
        maximum = 0.0,
        weight = 0.0,
        revision = 0,
        status = StrictCarbonReportExceptionStatus.PROPOSED,
    )
}
