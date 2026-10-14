package com.acme.transport.emissions.carbonreport.variance

import com.acme.transport.kit.text.normalize

/** Where a carbon report segment is in its life cycle. */
enum class CarbonReportSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a carbon report segment and its current status. */
data class CarbonReportSegment(
    var code: String,
    var title: String,
    var driverId: String,
    var source: String,
    var notes: String,
    var quantity: Int,
    var status: CarbonReportSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportSegmentStatus): Boolean {
        when (status) {
            CarbonReportSegmentStatus.PLANNED -> {
                when (next) {
                    CarbonReportSegmentStatus.SCHEDULED -> return true
                }
                return false
            }
            CarbonReportSegmentStatus.SCHEDULED -> {
                when (next) {
                    CarbonReportSegmentStatus.IN_PROGRESS -> return true
                }
                return false
            }
            CarbonReportSegmentStatus.IN_PROGRESS -> return next == CarbonReportSegmentStatus.COMPLETED || next == CarbonReportSegmentStatus.CANCELLED
            CarbonReportSegmentStatus.COMPLETED -> return next == CarbonReportSegmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CarbonReportSegmentStatus) {
        if (!canMoveTo(next)) {
            error("carbon report segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): CarbonReportSegment {
        return CarbonReportSegment(
            code = code,
            title = title,
            driverId = driverId,
            source = source,
            notes = notes,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a carbon report segment in status planned. */
fun newCarbonReportSegment(code: String, title: String): CarbonReportSegment {
    return CarbonReportSegment(
        code = code,
        title = title,
        driverId = "",
        source = "",
        notes = "",
        quantity = 0,
        status = CarbonReportSegmentStatus.PLANNED,
    )
}
