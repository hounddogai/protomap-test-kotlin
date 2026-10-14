package com.acme.transport.customs.tariff.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tariff override records of customs's tariff work, and the rules, stores, and calculations around them.

/** Where a tariff override is in its life cycle. */
enum class TariffOverrideStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One tariff override, with what the planners need to act on it. */
data class TariffOverride(
    var code: String,
    var title: String,
    var currency: String,
    var shiftCode: String,
    var odometer: Int,
    var locale: String,
    var rate: Double,
    var status: TariffOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffOverrideStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffOverrideStatus): Boolean {
        when (status) {
            TariffOverrideStatus.CREATED -> {
                when (next) {
                    TariffOverrideStatus.VALIDATED, TariffOverrideStatus.RETIRED -> return true
                }
                return false
            }
            TariffOverrideStatus.VALIDATED -> return next == TariffOverrideStatus.PUBLISHED || next == TariffOverrideStatus.RETIRED
            TariffOverrideStatus.PUBLISHED -> {
                when (next) {
                    TariffOverrideStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TariffOverrideStatus) {
        if (!canMoveTo(next)) {
            error("tariff override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tariff override"} $code"

    fun netExposure(): Double = round(odometer.toDouble() * rate * 100) / 100

    fun snapshot(): TariffOverride {
        return TariffOverride(
            code = code,
            title = title,
            currency = currency,
            shiftCode = shiftCode,
            odometer = odometer,
            locale = locale,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a tariff override in status created. */
fun newTariffOverride(code: String, title: String): TariffOverride {
    return TariffOverride(
        code = code,
        title = title,
        currency = "",
        shiftCode = "",
        odometer = 0,
        locale = "",
        rate = 0.0,
        status = TariffOverrideStatus.CREATED,
    )
}
