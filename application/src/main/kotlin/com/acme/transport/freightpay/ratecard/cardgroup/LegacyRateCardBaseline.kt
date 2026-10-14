package com.acme.transport.freightpay.ratecard.cardgroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy rate card baseline is in its life cycle. */
enum class LegacyRateCardBaselineStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One legacy rate card baseline, with what the planners need to act on it. */
data class LegacyRateCardBaseline(
    var code: String,
    var name: String,
    var floorValue: Double,
    var odometer: Int,
    var stopCount: Int,
    var ceiling: Double,
    var category: String,
    var unit: String,
    var region: String,
    var routeCode: String,
    var graceDays: Int,
    var status: LegacyRateCardBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyRateCardBaselineStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyRateCardBaselineStatus): Boolean {
        when (status) {
            LegacyRateCardBaselineStatus.CREATED -> {
                when (next) {
                    LegacyRateCardBaselineStatus.VALIDATED, LegacyRateCardBaselineStatus.RETIRED -> return true
                }
                return false
            }
            LegacyRateCardBaselineStatus.VALIDATED -> {
                when (next) {
                    LegacyRateCardBaselineStatus.RETIRED -> return true
                }
                return false
            }
            LegacyRateCardBaselineStatus.PUBLISHED -> return next == LegacyRateCardBaselineStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LegacyRateCardBaselineStatus) {
        if (!canMoveTo(next)) {
            error("legacy rate card baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy rate card baseline"} $code"

    fun projectedExposure(): Double = round(floorValue * stopCount.toDouble() * 100) / 100

    fun snapshot(): LegacyRateCardBaseline {
        return LegacyRateCardBaseline(
            code = code,
            name = name,
            floorValue = floorValue,
            odometer = odometer,
            stopCount = stopCount,
            ceiling = ceiling,
            category = category,
            unit = unit,
            region = region,
            routeCode = routeCode,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a legacy rate card baseline in status created. */
fun newLegacyRateCardBaseline(code: String, name: String): LegacyRateCardBaseline {
    return LegacyRateCardBaseline(
        code = code,
        name = name,
        floorValue = 0.0,
        odometer = 0,
        stopCount = 0,
        ceiling = 0.0,
        category = "",
        unit = "",
        region = "",
        routeCode = "",
        graceDays = 0,
        status = LegacyRateCardBaselineStatus.CREATED,
    )
}
