package com.acme.transport.emissions.carbonreport.reportadjustment

import com.acme.transport.kit.text.normalize

/** Where a carbon report entry is in its life cycle. */
enum class CarbonReportEntryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A carbon report entry as the carbon report team keeps it. */
data class CarbonReportEntry(
    var code: String,
    var label: String,
    var batchSize: Int,
    var dock: String,
    var routeCode: String,
    var active: Boolean,
    var status: CarbonReportEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportEntryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportEntryStatus): Boolean {
        when (status) {
            CarbonReportEntryStatus.IDLE -> {
                when (next) {
                    CarbonReportEntryStatus.WARMING, CarbonReportEntryStatus.DRAINING -> return true
                }
                return false
            }
            CarbonReportEntryStatus.WARMING -> return next == CarbonReportEntryStatus.READY || next == CarbonReportEntryStatus.DRAINING
            CarbonReportEntryStatus.READY -> {
                when (next) {
                    CarbonReportEntryStatus.DRAINING, CarbonReportEntryStatus.STOPPED -> return true
                }
                return false
            }
            CarbonReportEntryStatus.DRAINING -> return next == CarbonReportEntryStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CarbonReportEntryStatus) {
        if (!canMoveTo(next)) {
            error("carbon report entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"carbon report entry"} $code"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): CarbonReportEntry {
        return CarbonReportEntry(
            code = code,
            label = label,
            batchSize = batchSize,
            dock = dock,
            routeCode = routeCode,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a carbon report entry in status idle. */
fun newCarbonReportEntry(code: String, label: String): CarbonReportEntry {
    return CarbonReportEntry(
        code = code,
        label = label,
        batchSize = 0,
        dock = "",
        routeCode = "",
        active = false,
        status = CarbonReportEntryStatus.IDLE,
    )
}
