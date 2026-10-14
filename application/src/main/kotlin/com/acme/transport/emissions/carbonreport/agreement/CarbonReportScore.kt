package com.acme.transport.emissions.carbonreport.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report score is in its life cycle. */
enum class CarbonReportScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a carbon report score and its current status. */
data class CarbonReportScore(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var summary: String,
    var loadKg: Double,
    var version: Int,
    var status: CarbonReportScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportScoreStatus): Boolean {
        when (status) {
            CarbonReportScoreStatus.PLANNED -> {
                when (next) {
                    CarbonReportScoreStatus.IN_PROGRESS, CarbonReportScoreStatus.CANCELLED -> return true
                }
                return false
            }
            CarbonReportScoreStatus.SCHEDULED -> {
                when (next) {
                    CarbonReportScoreStatus.IN_PROGRESS, CarbonReportScoreStatus.COMPLETED -> return true
                }
                return false
            }
            CarbonReportScoreStatus.IN_PROGRESS -> return next == CarbonReportScoreStatus.COMPLETED
            CarbonReportScoreStatus.COMPLETED -> {
                when (next) {
                    CarbonReportScoreStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CarbonReportScoreStatus) {
        if (!canMoveTo(next)) {
            error("carbon report score $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"carbon report score"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round(version.toDouble() * loadKg * 100) / 100

    fun snapshot(): CarbonReportScore {
        return CarbonReportScore(
            code = code,
            title = title,
            attributes = attributes,
            summary = summary,
            loadKg = loadKg,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a carbon report score in status planned. */
fun newCarbonReportScore(code: String, title: String): CarbonReportScore {
    return CarbonReportScore(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        summary = "",
        loadKg = 0.0,
        version = 0,
        status = CarbonReportScoreStatus.PLANNED,
    )
}
