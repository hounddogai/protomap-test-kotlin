package com.acme.transport.emissions.fuelburn.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fuel burn schedule records of emissions's fuel burn work, and the rules, stores, and calculations
// around them.

/** Where a fuel burn schedule is in its life cycle. */
enum class FuelBurnScheduleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A fuel burn schedule. Its code is unique within a tenant. */
data class FuelBurnSchedule(
    var code: String,
    var label: String,
    var startsAt: Int,
    var routeCode: String,
    var attributes: MutableMap<String, String>,
    var etaMinutes: Int,
    var description: String,
    var unit: String,
    var visible: Boolean,
    var discount: Double,
    var status: FuelBurnScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnScheduleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnScheduleStatus): Boolean {
        when (status) {
            FuelBurnScheduleStatus.REQUESTED -> {
                when (next) {
                    FuelBurnScheduleStatus.REVOKED, FuelBurnScheduleStatus.EXPIRED -> return true
                }
                return false
            }
            FuelBurnScheduleStatus.GRANTED -> {
                when (next) {
                    FuelBurnScheduleStatus.EXPIRED -> return true
                }
                return false
            }
            FuelBurnScheduleStatus.REVOKED -> {
                when (next) {
                    FuelBurnScheduleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnScheduleStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn schedule $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round((startsAt.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): FuelBurnSchedule {
        return FuelBurnSchedule(
            code = code,
            label = label,
            startsAt = startsAt,
            routeCode = routeCode,
            attributes = attributes,
            etaMinutes = etaMinutes,
            description = description,
            unit = unit,
            visible = visible,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fuel burn schedule in status requested. */
fun newFuelBurnSchedule(code: String, label: String): FuelBurnSchedule {
    return FuelBurnSchedule(
        code = code,
        label = label,
        startsAt = 0,
        routeCode = "",
        attributes = mutableMapOf<String, String>(),
        etaMinutes = 0,
        description = "",
        unit = "",
        visible = false,
        discount = 0.0,
        status = FuelBurnScheduleStatus.REQUESTED,
    )
}
