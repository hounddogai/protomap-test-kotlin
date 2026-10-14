package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default carbon report mapping is in its life cycle. */
enum class DefaultCarbonReportMappingStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a default carbon report mapping and its current status. */
data class DefaultCarbonReportMapping(
    var code: String,
    var title: String,
    var startsAt: Int,
    var archived: Boolean,
    var retries: Int,
    var segment: String,
    var summary: String,
    var status: DefaultCarbonReportMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCarbonReportMappingStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCarbonReportMappingStatus): Boolean {
        when (status) {
            DefaultCarbonReportMappingStatus.IDLE -> return next == DefaultCarbonReportMappingStatus.WARMING
            DefaultCarbonReportMappingStatus.WARMING -> {
                when (next) {
                    DefaultCarbonReportMappingStatus.STOPPED -> return true
                }
                return false
            }
            DefaultCarbonReportMappingStatus.READY -> return next == DefaultCarbonReportMappingStatus.STOPPED
            DefaultCarbonReportMappingStatus.DRAINING -> {
                when (next) {
                    DefaultCarbonReportMappingStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultCarbonReportMappingStatus) {
        if (!canMoveTo(next)) {
            error("default carbon report mapping $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun effectiveTotal(): Double = round(retries.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): DefaultCarbonReportMapping {
        return DefaultCarbonReportMapping(
            code = code,
            title = title,
            startsAt = startsAt,
            archived = archived,
            retries = retries,
            segment = segment,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a default carbon report mapping in status idle. */
fun newDefaultCarbonReportMapping(code: String, title: String): DefaultCarbonReportMapping {
    return DefaultCarbonReportMapping(
        code = code,
        title = title,
        startsAt = 0,
        archived = false,
        retries = 0,
        segment = "",
        summary = "",
        status = DefaultCarbonReportMappingStatus.IDLE,
    )
}
