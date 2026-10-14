package com.acme.transport.customs.tariff.tariffprofile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered tariff profile records of customs's tariff work, and the rules, stores, and calculations around
// them.

/** Where a tiered tariff profile is in its life cycle. */
enum class TieredTariffProfileStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tiered tariff profile. Its code is unique within a tenant. */
data class TieredTariffProfile(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var shiftCode: String,
    var name: String,
    var locale: String,
    var etaMinutes: Int,
    var margin: Double,
    var status: TieredTariffProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTariffProfileStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTariffProfileStatus): Boolean {
        when (status) {
            TieredTariffProfileStatus.OPEN -> return next == TieredTariffProfileStatus.IN_REVIEW || next == TieredTariffProfileStatus.ARCHIVED
            TieredTariffProfileStatus.IN_REVIEW -> {
                when (next) {
                    TieredTariffProfileStatus.REOPENED -> return true
                }
                return false
            }
            TieredTariffProfileStatus.RESOLVED -> return next == TieredTariffProfileStatus.REOPENED || next == TieredTariffProfileStatus.ARCHIVED
            TieredTariffProfileStatus.REOPENED -> return next == TieredTariffProfileStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TieredTariffProfileStatus) {
        if (!canMoveTo(next)) {
            error("tiered tariff profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedLoad(): Double = round((etaMinutes.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): TieredTariffProfile {
        return TieredTariffProfile(
            code = code,
            label = label,
            distanceKm = distanceKm,
            shiftCode = shiftCode,
            name = name,
            locale = locale,
            etaMinutes = etaMinutes,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a tiered tariff profile in status open. */
fun newTieredTariffProfile(code: String, label: String): TieredTariffProfile {
    return TieredTariffProfile(
        code = code,
        label = label,
        distanceKm = 0.0,
        shiftCode = "",
        name = "",
        locale = "",
        etaMinutes = 0,
        margin = 0.0,
        status = TieredTariffProfileStatus.OPEN,
    )
}
