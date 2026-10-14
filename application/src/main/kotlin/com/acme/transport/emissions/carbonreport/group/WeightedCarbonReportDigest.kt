package com.acme.transport.emissions.carbonreport.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted carbon report digest is in its life cycle. */
enum class WeightedCarbonReportDigestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A weighted carbon report digest as the carbon report team keeps it. */
data class WeightedCarbonReportDigest(
    var code: String,
    var title: String,
    var timezone: String,
    var unit: String,
    var ceiling: Double,
    var reviewer: String,
    var rate: Double,
    var channel: String,
    var updatedAt: Int,
    var status: WeightedCarbonReportDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedCarbonReportDigestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedCarbonReportDigestStatus): Boolean {
        when (status) {
            WeightedCarbonReportDigestStatus.DRAFT -> {
                when (next) {
                    WeightedCarbonReportDigestStatus.SUSPENDED -> return true
                }
                return false
            }
            WeightedCarbonReportDigestStatus.PENDING -> return next == WeightedCarbonReportDigestStatus.ACTIVE || next == WeightedCarbonReportDigestStatus.CLOSED
            WeightedCarbonReportDigestStatus.ACTIVE -> {
                when (next) {
                    WeightedCarbonReportDigestStatus.SUSPENDED -> return true
                }
                return false
            }
            WeightedCarbonReportDigestStatus.SUSPENDED -> {
                when (next) {
                    WeightedCarbonReportDigestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedCarbonReportDigestStatus) {
        if (!canMoveTo(next)) {
            error("weighted carbon report digest $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun effectiveExposure(): Double = round((updatedAt.toDouble() - ceiling) * 100) / 100

    fun snapshot(): WeightedCarbonReportDigest {
        return WeightedCarbonReportDigest(
            code = code,
            title = title,
            timezone = timezone,
            unit = unit,
            ceiling = ceiling,
            reviewer = reviewer,
            rate = rate,
            channel = channel,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a weighted carbon report digest in status draft. */
fun newWeightedCarbonReportDigest(code: String, title: String): WeightedCarbonReportDigest {
    return WeightedCarbonReportDigest(
        code = code,
        title = title,
        timezone = "",
        unit = "",
        ceiling = 0.0,
        reviewer = "",
        rate = 0.0,
        channel = "",
        updatedAt = 0,
        status = WeightedCarbonReportDigestStatus.DRAFT,
    )
}
