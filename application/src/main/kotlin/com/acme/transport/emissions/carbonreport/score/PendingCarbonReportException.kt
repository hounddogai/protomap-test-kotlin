package com.acme.transport.emissions.carbonreport.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending carbon report exception is in its life cycle. */
enum class PendingCarbonReportExceptionStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a pending carbon report exception and its current status. */
data class PendingCarbonReportException(
    var code: String,
    var title: String,
    var ceiling: Double,
    var quantity: Int,
    var budget: Double,
    var floorValue: Double,
    var status: PendingCarbonReportExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCarbonReportExceptionStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCarbonReportExceptionStatus): Boolean {
        when (status) {
            PendingCarbonReportExceptionStatus.CREATED -> return next == PendingCarbonReportExceptionStatus.VALIDATED || next == PendingCarbonReportExceptionStatus.PUBLISHED
            PendingCarbonReportExceptionStatus.VALIDATED -> return next == PendingCarbonReportExceptionStatus.PUBLISHED || next == PendingCarbonReportExceptionStatus.RETIRED
            PendingCarbonReportExceptionStatus.PUBLISHED -> return next == PendingCarbonReportExceptionStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PendingCarbonReportExceptionStatus) {
        if (!canMoveTo(next)) {
            error("pending carbon report exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun netValue(): Double = round((budget + ceiling) * 100) / 100

    fun snapshot(): PendingCarbonReportException {
        return PendingCarbonReportException(
            code = code,
            title = title,
            ceiling = ceiling,
            quantity = quantity,
            budget = budget,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending carbon report exception in status created. */
fun newPendingCarbonReportException(code: String, title: String): PendingCarbonReportException {
    return PendingCarbonReportException(
        code = code,
        title = title,
        ceiling = 0.0,
        quantity = 0,
        budget = 0.0,
        floorValue = 0.0,
        status = PendingCarbonReportExceptionStatus.CREATED,
    )
}
