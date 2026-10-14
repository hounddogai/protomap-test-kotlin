package com.acme.transport.emissions.carbonreport.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered carbon report entry records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a tiered carbon report entry is in its life cycle. */
enum class TieredCarbonReportEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One tiered carbon report entry, with what the planners need to act on it. */
data class TieredCarbonReportEntry(
    var code: String,
    var title: String,
    var batchSize: Int,
    var windowDays: Int,
    var routeCode: String,
    var status: TieredCarbonReportEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCarbonReportEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCarbonReportEntryStatus): Boolean {
        when (status) {
            TieredCarbonReportEntryStatus.OPEN -> return next == TieredCarbonReportEntryStatus.REOPENED
            TieredCarbonReportEntryStatus.IN_REVIEW -> {
                when (next) {
                    TieredCarbonReportEntryStatus.RESOLVED, TieredCarbonReportEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredCarbonReportEntryStatus.RESOLVED -> {
                when (next) {
                    TieredCarbonReportEntryStatus.REOPENED, TieredCarbonReportEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredCarbonReportEntryStatus.REOPENED -> return next == TieredCarbonReportEntryStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TieredCarbonReportEntryStatus) {
        if (!canMoveTo(next)) {
            error("tiered carbon report entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedLoad(): Double = round(batchSize.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): TieredCarbonReportEntry {
        return TieredCarbonReportEntry(
            code = code,
            title = title,
            batchSize = batchSize,
            windowDays = windowDays,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered carbon report entry in status open. */
fun newTieredCarbonReportEntry(code: String, title: String): TieredCarbonReportEntry {
    return TieredCarbonReportEntry(
        code = code,
        title = title,
        batchSize = 0,
        windowDays = 0,
        routeCode = "",
        status = TieredCarbonReportEntryStatus.OPEN,
    )
}
