package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback lane plan bundle is in its life cycle. */
enum class FallbackLanePlanBundleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a fallback lane plan bundle and its current status. */
data class FallbackLanePlanBundle(
    var code: String,
    var name: String,
    var limit: Int,
    var odometer: Int,
    var carrier: String,
    var fuelLiters: Double,
    var locale: String,
    var timezone: String,
    var unit: String,
    var sequence: Int,
    var externalRef: String,
    var status: FallbackLanePlanBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackLanePlanBundleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackLanePlanBundleStatus): Boolean {
        when (status) {
            FallbackLanePlanBundleStatus.PLANNED -> return next == FallbackLanePlanBundleStatus.CANCELLED
            FallbackLanePlanBundleStatus.SCHEDULED -> return next == FallbackLanePlanBundleStatus.IN_PROGRESS
            FallbackLanePlanBundleStatus.IN_PROGRESS -> return next == FallbackLanePlanBundleStatus.COMPLETED
            FallbackLanePlanBundleStatus.COMPLETED -> return next == FallbackLanePlanBundleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: FallbackLanePlanBundleStatus) {
        if (!canMoveTo(next)) {
            error("fallback lane plan bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveTotal(): Double = round(fuelLiters * odometer.toDouble() * 100) / 100

    fun snapshot(): FallbackLanePlanBundle {
        return FallbackLanePlanBundle(
            code = code,
            name = name,
            limit = limit,
            odometer = odometer,
            carrier = carrier,
            fuelLiters = fuelLiters,
            locale = locale,
            timezone = timezone,
            unit = unit,
            sequence = sequence,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback lane plan bundle in status planned. */
fun newFallbackLanePlanBundle(code: String, name: String): FallbackLanePlanBundle {
    return FallbackLanePlanBundle(
        code = code,
        name = name,
        limit = 0,
        odometer = 0,
        carrier = "",
        fuelLiters = 0.0,
        locale = "",
        timezone = "",
        unit = "",
        sequence = 0,
        externalRef = "",
        status = FallbackLanePlanBundleStatus.PLANNED,
    )
}
