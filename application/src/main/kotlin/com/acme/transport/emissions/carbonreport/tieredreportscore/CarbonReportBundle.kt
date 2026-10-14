package com.acme.transport.emissions.carbonreport.tieredreportscore

import com.acme.transport.kit.text.normalize

/** Where a carbon report bundle is in its life cycle. */
enum class CarbonReportBundleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One carbon report bundle, with what the planners need to act on it. */
data class CarbonReportBundle(
    var code: String,
    var title: String,
    var channel: String,
    var source: String,
    var floorValue: Double,
    var shiftCode: String,
    var trailerId: String,
    var status: CarbonReportBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportBundleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportBundleStatus): Boolean {
        when (status) {
            CarbonReportBundleStatus.CREATED -> {
                when (next) {
                    CarbonReportBundleStatus.VALIDATED, CarbonReportBundleStatus.PUBLISHED -> return true
                }
                return false
            }
            CarbonReportBundleStatus.VALIDATED -> {
                when (next) {
                    CarbonReportBundleStatus.RETIRED -> return true
                }
                return false
            }
            CarbonReportBundleStatus.PUBLISHED -> return next == CarbonReportBundleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CarbonReportBundleStatus) {
        if (!canMoveTo(next)) {
            error("carbon report bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = floorValue

    fun snapshot(): CarbonReportBundle {
        return CarbonReportBundle(
            code = code,
            title = title,
            channel = channel,
            source = source,
            floorValue = floorValue,
            shiftCode = shiftCode,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a carbon report bundle in status created. */
fun newCarbonReportBundle(code: String, title: String): CarbonReportBundle {
    return CarbonReportBundle(
        code = code,
        title = title,
        channel = "",
        source = "",
        floorValue = 0.0,
        shiftCode = "",
        trailerId = "",
        status = CarbonReportBundleStatus.CREATED,
    )
}
