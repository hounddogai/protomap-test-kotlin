package com.acme.transport.fleetops.utilization.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional utilization threshold is in its life cycle. */
enum class RegionalUtilizationThresholdStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a regional utilization threshold and its current status. */
data class RegionalUtilizationThreshold(
    var code: String,
    var name: String,
    var endsAt: Int,
    var reviewer: String,
    var timezone: String,
    var fuelLiters: Double,
    var pinned: Boolean,
    var status: RegionalUtilizationThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalUtilizationThresholdStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalUtilizationThresholdStatus): Boolean {
        when (status) {
            RegionalUtilizationThresholdStatus.PLANNED -> return next == RegionalUtilizationThresholdStatus.SCHEDULED || next == RegionalUtilizationThresholdStatus.COMPLETED
            RegionalUtilizationThresholdStatus.SCHEDULED -> {
                when (next) {
                    RegionalUtilizationThresholdStatus.IN_PROGRESS, RegionalUtilizationThresholdStatus.COMPLETED -> return true
                }
                return false
            }
            RegionalUtilizationThresholdStatus.IN_PROGRESS -> {
                when (next) {
                    RegionalUtilizationThresholdStatus.COMPLETED, RegionalUtilizationThresholdStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalUtilizationThresholdStatus.COMPLETED -> {
                when (next) {
                    RegionalUtilizationThresholdStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalUtilizationThresholdStatus) {
        if (!canMoveTo(next)) {
            error("regional utilization threshold $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"regional utilization threshold"} $code"

    fun effectiveValue(): Double = round((fuelLiters + endsAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalUtilizationThreshold {
        return RegionalUtilizationThreshold(
            code = code,
            name = name,
            endsAt = endsAt,
            reviewer = reviewer,
            timezone = timezone,
            fuelLiters = fuelLiters,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional utilization threshold in status planned. */
fun newRegionalUtilizationThreshold(code: String, name: String): RegionalUtilizationThreshold {
    return RegionalUtilizationThreshold(
        code = code,
        name = name,
        endsAt = 0,
        reviewer = "",
        timezone = "",
        fuelLiters = 0.0,
        pinned = false,
        status = RegionalUtilizationThresholdStatus.PLANNED,
    )
}
