package com.acme.transport.emissions.carbonreport.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report mapping is in its life cycle. */
enum class CarbonReportMappingStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One carbon report mapping, with what the planners need to act on it. */
data class CarbonReportMapping(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var budget: Double,
    var maximum: Double,
    var retries: Int,
    var etaMinutes: Int,
    var margin: Double,
    var attempts: Int,
    var threshold: Double,
    var name: String,
    var status: CarbonReportMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportMappingStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportMappingStatus): Boolean {
        when (status) {
            CarbonReportMappingStatus.REQUESTED -> return next == CarbonReportMappingStatus.GRANTED || next == CarbonReportMappingStatus.EXPIRED
            CarbonReportMappingStatus.GRANTED -> {
                when (next) {
                    CarbonReportMappingStatus.REVOKED -> return true
                }
                return false
            }
            CarbonReportMappingStatus.REVOKED -> return next == CarbonReportMappingStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CarbonReportMappingStatus) {
        if (!canMoveTo(next)) {
            error("carbon report mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"carbon report mapping"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedTotal(): Double = round((margin + threshold) * 100) / 100

    fun snapshot(): CarbonReportMapping {
        return CarbonReportMapping(
            code = code,
            title = title,
            tags = tags,
            budget = budget,
            maximum = maximum,
            retries = retries,
            etaMinutes = etaMinutes,
            margin = margin,
            attempts = attempts,
            threshold = threshold,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a carbon report mapping in status requested. */
fun newCarbonReportMapping(code: String, title: String): CarbonReportMapping {
    return CarbonReportMapping(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        budget = 0.0,
        maximum = 0.0,
        retries = 0,
        etaMinutes = 0,
        margin = 0.0,
        attempts = 0,
        threshold = 0.0,
        name = "",
        status = CarbonReportMappingStatus.REQUESTED,
    )
}
