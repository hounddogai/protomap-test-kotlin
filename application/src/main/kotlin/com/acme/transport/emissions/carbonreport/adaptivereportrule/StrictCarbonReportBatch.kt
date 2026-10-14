package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.kit.text.normalize

/** Where a strict carbon report batch is in its life cycle. */
enum class StrictCarbonReportBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict carbon report batch as the carbon report team keeps it. */
data class StrictCarbonReportBatch(
    var code: String,
    var title: String,
    var pallets: Int,
    var channel: String,
    var dock: String,
    var name: String,
    var status: StrictCarbonReportBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictCarbonReportBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictCarbonReportBatchStatus): Boolean {
        when (status) {
            StrictCarbonReportBatchStatus.OPEN -> return next == StrictCarbonReportBatchStatus.IN_REVIEW || next == StrictCarbonReportBatchStatus.REOPENED
            StrictCarbonReportBatchStatus.IN_REVIEW -> return next == StrictCarbonReportBatchStatus.REOPENED
            StrictCarbonReportBatchStatus.RESOLVED -> {
                when (next) {
                    StrictCarbonReportBatchStatus.REOPENED, StrictCarbonReportBatchStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictCarbonReportBatchStatus.REOPENED -> return next == StrictCarbonReportBatchStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: StrictCarbonReportBatchStatus) {
        if (!canMoveTo(next)) {
            error("strict carbon report batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = pallets.toDouble()

    fun snapshot(): StrictCarbonReportBatch {
        return StrictCarbonReportBatch(
            code = code,
            title = title,
            pallets = pallets,
            channel = channel,
            dock = dock,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a strict carbon report batch in status open. */
fun newStrictCarbonReportBatch(code: String, title: String): StrictCarbonReportBatch {
    return StrictCarbonReportBatch(
        code = code,
        title = title,
        pallets = 0,
        channel = "",
        dock = "",
        name = "",
        status = StrictCarbonReportBatchStatus.OPEN,
    )
}
