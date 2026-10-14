package com.acme.transport.customs.tariff.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive tariff milestone records of customs's tariff work, and the rules, stores, and calculations
// around them.

/** Where a adaptive tariff milestone is in its life cycle. */
enum class AdaptiveTariffMilestoneStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One adaptive tariff milestone, with what the planners need to act on it. */
data class AdaptiveTariffMilestone(
    var code: String,
    var label: String,
    var visible: Boolean,
    var driverId: String,
    var routeCode: String,
    var updatedAt: Int,
    var channel: String,
    var version: Int,
    var unit: String,
    var durationMinutes: Int,
    var status: AdaptiveTariffMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTariffMilestoneStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTariffMilestoneStatus): Boolean {
        when (status) {
            AdaptiveTariffMilestoneStatus.DRAFT -> return next == AdaptiveTariffMilestoneStatus.ACTIVE
            AdaptiveTariffMilestoneStatus.PENDING -> return next == AdaptiveTariffMilestoneStatus.ACTIVE
            AdaptiveTariffMilestoneStatus.ACTIVE -> return next == AdaptiveTariffMilestoneStatus.SUSPENDED || next == AdaptiveTariffMilestoneStatus.CLOSED
            AdaptiveTariffMilestoneStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveTariffMilestoneStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveTariffMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("adaptive tariff milestone $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedLoad(): Double = round((version.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveTariffMilestone {
        return AdaptiveTariffMilestone(
            code = code,
            label = label,
            visible = visible,
            driverId = driverId,
            routeCode = routeCode,
            updatedAt = updatedAt,
            channel = channel,
            version = version,
            unit = unit,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a adaptive tariff milestone in status draft. */
fun newAdaptiveTariffMilestone(code: String, label: String): AdaptiveTariffMilestone {
    return AdaptiveTariffMilestone(
        code = code,
        label = label,
        visible = false,
        driverId = "",
        routeCode = "",
        updatedAt = 0,
        channel = "",
        version = 0,
        unit = "",
        durationMinutes = 0,
        status = AdaptiveTariffMilestoneStatus.DRAFT,
    )
}
