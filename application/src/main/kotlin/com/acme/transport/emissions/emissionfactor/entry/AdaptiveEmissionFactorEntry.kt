package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive emission factor entry records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a adaptive emission factor entry is in its life cycle. */
enum class AdaptiveEmissionFactorEntryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a adaptive emission factor entry and its current status. */
data class AdaptiveEmissionFactorEntry(
    var code: String,
    var title: String,
    var carrier: String,
    var locale: String,
    var category: String,
    var windowDays: Int,
    var cost: Double,
    var routeCode: String,
    var fuelLiters: Double,
    var status: AdaptiveEmissionFactorEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveEmissionFactorEntryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveEmissionFactorEntryStatus): Boolean {
        when (status) {
            AdaptiveEmissionFactorEntryStatus.PLANNED -> return next == AdaptiveEmissionFactorEntryStatus.COMPLETED || next == AdaptiveEmissionFactorEntryStatus.CANCELLED
            AdaptiveEmissionFactorEntryStatus.SCHEDULED -> {
                when (next) {
                    AdaptiveEmissionFactorEntryStatus.IN_PROGRESS, AdaptiveEmissionFactorEntryStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveEmissionFactorEntryStatus.IN_PROGRESS -> return next == AdaptiveEmissionFactorEntryStatus.CANCELLED
            AdaptiveEmissionFactorEntryStatus.COMPLETED -> {
                when (next) {
                    AdaptiveEmissionFactorEntryStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveEmissionFactorEntryStatus) {
        if (!canMoveTo(next)) {
            error("adaptive emission factor entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive emission factor entry"} $code"

    fun weightedLoad(): Double = round((windowDays.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): AdaptiveEmissionFactorEntry {
        return AdaptiveEmissionFactorEntry(
            code = code,
            title = title,
            carrier = carrier,
            locale = locale,
            category = category,
            windowDays = windowDays,
            cost = cost,
            routeCode = routeCode,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a adaptive emission factor entry in status planned. */
fun newAdaptiveEmissionFactorEntry(code: String, title: String): AdaptiveEmissionFactorEntry {
    return AdaptiveEmissionFactorEntry(
        code = code,
        title = title,
        carrier = "",
        locale = "",
        category = "",
        windowDays = 0,
        cost = 0.0,
        routeCode = "",
        fuelLiters = 0.0,
        status = AdaptiveEmissionFactorEntryStatus.PLANNED,
    )
}
