package com.acme.transport.customs.inspection.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy inspection forecast records of customs's inspection work, and the rules, stores, and
// calculations around them.

/** Where a legacy inspection forecast is in its life cycle. */
enum class LegacyInspectionForecastStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A legacy inspection forecast. Its code is unique within a tenant. */
data class LegacyInspectionForecast(
    var code: String,
    var label: String,
    var pallets: Int,
    var country: String,
    var etaMinutes: Int,
    var endsAt: Int,
    var notes: String,
    var durationMinutes: Int,
    var status: LegacyInspectionForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyInspectionForecastStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyInspectionForecastStatus): Boolean {
        when (status) {
            LegacyInspectionForecastStatus.PROPOSED -> return next == LegacyInspectionForecastStatus.APPROVED
            LegacyInspectionForecastStatus.APPROVED -> {
                when (next) {
                    LegacyInspectionForecastStatus.REJECTED -> return true
                }
                return false
            }
            LegacyInspectionForecastStatus.REJECTED -> {
                when (next) {
                    LegacyInspectionForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyInspectionForecastStatus) {
        if (!canMoveTo(next)) {
            error("legacy inspection forecast $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedTotal(): Double = round((durationMinutes.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): LegacyInspectionForecast {
        return LegacyInspectionForecast(
            code = code,
            label = label,
            pallets = pallets,
            country = country,
            etaMinutes = etaMinutes,
            endsAt = endsAt,
            notes = notes,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a legacy inspection forecast in status proposed. */
fun newLegacyInspectionForecast(code: String, label: String): LegacyInspectionForecast {
    return LegacyInspectionForecast(
        code = code,
        label = label,
        pallets = 0,
        country = "",
        etaMinutes = 0,
        endsAt = 0,
        notes = "",
        durationMinutes = 0,
        status = LegacyInspectionForecastStatus.PROPOSED,
    )
}
