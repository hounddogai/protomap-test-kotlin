package com.acme.transport.emissions.carbonreport.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report profile is in its life cycle. */
enum class CarbonReportProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a carbon report profile and its current status. */
data class CarbonReportProfile(
    var code: String,
    var title: String,
    var loadKg: Double,
    var pallets: Int,
    var ownerId: String,
    var pinned: Boolean,
    var region: String,
    var currency: String,
    var status: CarbonReportProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportProfileStatus): Boolean {
        when (status) {
            CarbonReportProfileStatus.PLANNED -> {
                when (next) {
                    CarbonReportProfileStatus.CANCELLED -> return true
                }
                return false
            }
            CarbonReportProfileStatus.SCHEDULED -> return next == CarbonReportProfileStatus.IN_PROGRESS
            CarbonReportProfileStatus.IN_PROGRESS -> return next == CarbonReportProfileStatus.COMPLETED || next == CarbonReportProfileStatus.CANCELLED
            CarbonReportProfileStatus.COMPLETED -> return next == CarbonReportProfileStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CarbonReportProfileStatus) {
        if (!canMoveTo(next)) {
            error("carbon report profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedLoad(): Double = round(loadKg * pallets.toDouble() * 100) / 100

    fun snapshot(): CarbonReportProfile {
        return CarbonReportProfile(
            code = code,
            title = title,
            loadKg = loadKg,
            pallets = pallets,
            ownerId = ownerId,
            pinned = pinned,
            region = region,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a carbon report profile in status planned. */
fun newCarbonReportProfile(code: String, title: String): CarbonReportProfile {
    return CarbonReportProfile(
        code = code,
        title = title,
        loadKg = 0.0,
        pallets = 0,
        ownerId = "",
        pinned = false,
        region = "",
        currency = "",
        status = CarbonReportProfileStatus.PLANNED,
    )
}
