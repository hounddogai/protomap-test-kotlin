package com.acme.transport.emissions.carbonreport.batch

import com.acme.transport.kit.text.normalize

// The carbon report batch records of emissions's carbon report work, and the rules, stores, and calculations
// around them.

/** Where a carbon report batch is in its life cycle. */
enum class CarbonReportBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A carbon report batch as the carbon report team keeps it. */
data class CarbonReportBatch(
    var code: String,
    var label: String,
    var country: String,
    var pinned: Boolean,
    var durationMinutes: Int,
    var status: CarbonReportBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportBatchStatus): Boolean {
        when (status) {
            CarbonReportBatchStatus.REQUESTED -> return next == CarbonReportBatchStatus.GRANTED || next == CarbonReportBatchStatus.REVOKED
            CarbonReportBatchStatus.GRANTED -> {
                when (next) {
                    CarbonReportBatchStatus.REVOKED, CarbonReportBatchStatus.EXPIRED -> return true
                }
                return false
            }
            CarbonReportBatchStatus.REVOKED -> {
                when (next) {
                    CarbonReportBatchStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CarbonReportBatchStatus) {
        if (!canMoveTo(next)) {
            error("carbon report batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): CarbonReportBatch {
        return CarbonReportBatch(
            code = code,
            label = label,
            country = country,
            pinned = pinned,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a carbon report batch in status requested. */
fun newCarbonReportBatch(code: String, label: String): CarbonReportBatch {
    return CarbonReportBatch(
        code = code,
        label = label,
        country = "",
        pinned = false,
        durationMinutes = 0,
        status = CarbonReportBatchStatus.REQUESTED,
    )
}
