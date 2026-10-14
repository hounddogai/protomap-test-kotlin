package com.acme.transport.emissions.carbonreport.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy carbon report mapping records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a legacy carbon report mapping is in its life cycle. */
enum class LegacyCarbonReportMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a legacy carbon report mapping and its current status. */
data class LegacyCarbonReportMapping(
    var code: String,
    var title: String,
    var ceiling: Double,
    var driverId: String,
    var summary: String,
    var dock: String,
    var ratio: Double,
    var capacity: Int,
    var channel: String,
    var status: LegacyCarbonReportMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCarbonReportMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCarbonReportMappingStatus): Boolean {
        when (status) {
            LegacyCarbonReportMappingStatus.NEW -> return next == LegacyCarbonReportMappingStatus.QUEUED || next == LegacyCarbonReportMappingStatus.RUNNING
            LegacyCarbonReportMappingStatus.QUEUED -> {
                when (next) {
                    LegacyCarbonReportMappingStatus.FAILED -> return true
                }
                return false
            }
            LegacyCarbonReportMappingStatus.RUNNING -> {
                when (next) {
                    LegacyCarbonReportMappingStatus.DONE, LegacyCarbonReportMappingStatus.FAILED -> return true
                }
                return false
            }
            LegacyCarbonReportMappingStatus.DONE -> {
                when (next) {
                    LegacyCarbonReportMappingStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyCarbonReportMappingStatus) {
        if (!canMoveTo(next)) {
            error("legacy carbon report mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netLoad(): Double = round((capacity.toDouble() + ceiling) * 100) / 100

    fun snapshot(): LegacyCarbonReportMapping {
        return LegacyCarbonReportMapping(
            code = code,
            title = title,
            ceiling = ceiling,
            driverId = driverId,
            summary = summary,
            dock = dock,
            ratio = ratio,
            capacity = capacity,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a legacy carbon report mapping in status new. */
fun newLegacyCarbonReportMapping(code: String, title: String): LegacyCarbonReportMapping {
    return LegacyCarbonReportMapping(
        code = code,
        title = title,
        ceiling = 0.0,
        driverId = "",
        summary = "",
        dock = "",
        ratio = 0.0,
        capacity = 0,
        channel = "",
        status = LegacyCarbonReportMappingStatus.NEW,
    )
}
