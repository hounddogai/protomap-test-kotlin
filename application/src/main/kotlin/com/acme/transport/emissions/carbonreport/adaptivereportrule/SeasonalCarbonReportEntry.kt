package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal carbon report entry is in its life cycle. */
enum class SeasonalCarbonReportEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One seasonal carbon report entry, with what the planners need to act on it. */
data class SeasonalCarbonReportEntry(
    var code: String,
    var label: String,
    var active: Boolean,
    var threshold: Double,
    var stopCount: Int,
    var margin: Double,
    var ownerId: String,
    var description: String,
    var depot: String,
    var score: Double,
    var status: SeasonalCarbonReportEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCarbonReportEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCarbonReportEntryStatus): Boolean {
        when (status) {
            SeasonalCarbonReportEntryStatus.CREATED -> {
                when (next) {
                    SeasonalCarbonReportEntryStatus.PUBLISHED -> return true
                }
                return false
            }
            SeasonalCarbonReportEntryStatus.VALIDATED -> {
                when (next) {
                    SeasonalCarbonReportEntryStatus.PUBLISHED -> return true
                }
                return false
            }
            SeasonalCarbonReportEntryStatus.PUBLISHED -> return next == SeasonalCarbonReportEntryStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: SeasonalCarbonReportEntryStatus) {
        if (!canMoveTo(next)) {
            error("seasonal carbon report entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal carbon report entry"} $code"

    fun netExposure(): Double = round((score - stopCount.toDouble()) * 100) / 100

    fun snapshot(): SeasonalCarbonReportEntry {
        return SeasonalCarbonReportEntry(
            code = code,
            label = label,
            active = active,
            threshold = threshold,
            stopCount = stopCount,
            margin = margin,
            ownerId = ownerId,
            description = description,
            depot = depot,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a seasonal carbon report entry in status created. */
fun newSeasonalCarbonReportEntry(code: String, label: String): SeasonalCarbonReportEntry {
    return SeasonalCarbonReportEntry(
        code = code,
        label = label,
        active = false,
        threshold = 0.0,
        stopCount = 0,
        margin = 0.0,
        ownerId = "",
        description = "",
        depot = "",
        score = 0.0,
        status = SeasonalCarbonReportEntryStatus.CREATED,
    )
}
