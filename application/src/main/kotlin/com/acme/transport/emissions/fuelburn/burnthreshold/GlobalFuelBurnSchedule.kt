package com.acme.transport.emissions.fuelburn.burnthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global fuel burn schedule is in its life cycle. */
enum class GlobalFuelBurnScheduleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One global fuel burn schedule, with what the planners need to act on it. */
data class GlobalFuelBurnSchedule(
    var code: String,
    var title: String,
    var country: String,
    var region: String,
    var floorValue: Double,
    var updatedAt: Int,
    var summary: String,
    var discount: Double,
    var category: String,
    var currency: String,
    var loadKg: Double,
    var status: GlobalFuelBurnScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalFuelBurnScheduleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalFuelBurnScheduleStatus): Boolean {
        when (status) {
            GlobalFuelBurnScheduleStatus.PROPOSED -> return next == GlobalFuelBurnScheduleStatus.WITHDRAWN
            GlobalFuelBurnScheduleStatus.APPROVED -> {
                when (next) {
                    GlobalFuelBurnScheduleStatus.REJECTED, GlobalFuelBurnScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
            GlobalFuelBurnScheduleStatus.REJECTED -> return next == GlobalFuelBurnScheduleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalFuelBurnScheduleStatus) {
        if (!canMoveTo(next)) {
            error("global fuel burn schedule $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun effectiveExposure(): Double = round((loadKg + floorValue) * 100) / 100

    fun snapshot(): GlobalFuelBurnSchedule {
        return GlobalFuelBurnSchedule(
            code = code,
            title = title,
            country = country,
            region = region,
            floorValue = floorValue,
            updatedAt = updatedAt,
            summary = summary,
            discount = discount,
            category = category,
            currency = currency,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a global fuel burn schedule in status proposed. */
fun newGlobalFuelBurnSchedule(code: String, title: String): GlobalFuelBurnSchedule {
    return GlobalFuelBurnSchedule(
        code = code,
        title = title,
        country = "",
        region = "",
        floorValue = 0.0,
        updatedAt = 0,
        summary = "",
        discount = 0.0,
        category = "",
        currency = "",
        loadKg = 0.0,
        status = GlobalFuelBurnScheduleStatus.PROPOSED,
    )
}
