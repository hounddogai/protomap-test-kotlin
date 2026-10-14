package com.acme.transport.emissions.carbonreport.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered carbon report variance is in its life cycle. */
enum class TieredCarbonReportVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tiered carbon report variance. Its code is unique within a tenant. */
data class TieredCarbonReportVariance(
    var code: String,
    var name: String,
    var rate: Double,
    var vehicleId: String,
    var durationMinutes: Int,
    var checksum: String,
    var ratio: Double,
    var status: TieredCarbonReportVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCarbonReportVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCarbonReportVarianceStatus): Boolean {
        when (status) {
            TieredCarbonReportVarianceStatus.PLANNED -> return next == TieredCarbonReportVarianceStatus.SCHEDULED || next == TieredCarbonReportVarianceStatus.IN_PROGRESS
            TieredCarbonReportVarianceStatus.SCHEDULED -> return next == TieredCarbonReportVarianceStatus.IN_PROGRESS || next == TieredCarbonReportVarianceStatus.CANCELLED
            TieredCarbonReportVarianceStatus.IN_PROGRESS -> {
                when (next) {
                    TieredCarbonReportVarianceStatus.COMPLETED -> return true
                }
                return false
            }
            TieredCarbonReportVarianceStatus.COMPLETED -> return next == TieredCarbonReportVarianceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TieredCarbonReportVarianceStatus) {
        if (!canMoveTo(next)) {
            error("tiered carbon report variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedVolume(): Double = round((durationMinutes.toDouble() - rate) * 100) / 100

    fun snapshot(): TieredCarbonReportVariance {
        return TieredCarbonReportVariance(
            code = code,
            name = name,
            rate = rate,
            vehicleId = vehicleId,
            durationMinutes = durationMinutes,
            checksum = checksum,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered carbon report variance in status planned. */
fun newTieredCarbonReportVariance(code: String, name: String): TieredCarbonReportVariance {
    return TieredCarbonReportVariance(
        code = code,
        name = name,
        rate = 0.0,
        vehicleId = "",
        durationMinutes = 0,
        checksum = "",
        ratio = 0.0,
        status = TieredCarbonReportVarianceStatus.PLANNED,
    )
}
