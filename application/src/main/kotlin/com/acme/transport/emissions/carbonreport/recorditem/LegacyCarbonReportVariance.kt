package com.acme.transport.emissions.carbonreport.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy carbon report variance is in its life cycle. */
enum class LegacyCarbonReportVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A legacy carbon report variance. Its code is unique within a tenant. */
data class LegacyCarbonReportVariance(
    var code: String,
    var name: String,
    var vehicleId: String,
    var ownerId: String,
    var cost: Double,
    var externalRef: String,
    var segment: String,
    var shiftCode: String,
    var lane: String,
    var threshold: Double,
    var discount: Double,
    var status: LegacyCarbonReportVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCarbonReportVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCarbonReportVarianceStatus): Boolean {
        when (status) {
            LegacyCarbonReportVarianceStatus.PROPOSED -> {
                when (next) {
                    LegacyCarbonReportVarianceStatus.REJECTED, LegacyCarbonReportVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
            LegacyCarbonReportVarianceStatus.APPROVED -> return next == LegacyCarbonReportVarianceStatus.WITHDRAWN
            LegacyCarbonReportVarianceStatus.REJECTED -> return next == LegacyCarbonReportVarianceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacyCarbonReportVarianceStatus) {
        if (!canMoveTo(next)) {
            error("legacy carbon report variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedVolume(): Double = round((discount - threshold) * 100) / 100

    fun snapshot(): LegacyCarbonReportVariance {
        return LegacyCarbonReportVariance(
            code = code,
            name = name,
            vehicleId = vehicleId,
            ownerId = ownerId,
            cost = cost,
            externalRef = externalRef,
            segment = segment,
            shiftCode = shiftCode,
            lane = lane,
            threshold = threshold,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a legacy carbon report variance in status proposed. */
fun newLegacyCarbonReportVariance(code: String, name: String): LegacyCarbonReportVariance {
    return LegacyCarbonReportVariance(
        code = code,
        name = name,
        vehicleId = "",
        ownerId = "",
        cost = 0.0,
        externalRef = "",
        segment = "",
        shiftCode = "",
        lane = "",
        threshold = 0.0,
        discount = 0.0,
        status = LegacyCarbonReportVarianceStatus.PROPOSED,
    )
}
