package com.acme.transport.fleetops.utilization.score

import com.acme.transport.kit.text.normalize

/** Where a utilization tier is in its life cycle. */
enum class UtilizationTierStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a utilization tier and its current status. */
data class UtilizationTier(
    var code: String,
    var label: String,
    var timezone: String,
    var rate: Double,
    var driverId: String,
    var status: UtilizationTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationTierStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationTierStatus): Boolean {
        when (status) {
            UtilizationTierStatus.PLANNED -> {
                when (next) {
                    UtilizationTierStatus.SCHEDULED -> return true
                }
                return false
            }
            UtilizationTierStatus.SCHEDULED -> return next == UtilizationTierStatus.CANCELLED
            UtilizationTierStatus.IN_PROGRESS -> return next == UtilizationTierStatus.COMPLETED
            UtilizationTierStatus.COMPLETED -> {
                when (next) {
                    UtilizationTierStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationTierStatus) {
        if (!canMoveTo(next)) {
            error("utilization tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"utilization tier"} $code"

    fun measuredValue(): Double = rate

    fun snapshot(): UtilizationTier {
        return UtilizationTier(
            code = code,
            label = label,
            timezone = timezone,
            rate = rate,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a utilization tier in status planned. */
fun newUtilizationTier(code: String, label: String): UtilizationTier {
    return UtilizationTier(
        code = code,
        label = label,
        timezone = "",
        rate = 0.0,
        driverId = "",
        status = UtilizationTierStatus.PLANNED,
    )
}
