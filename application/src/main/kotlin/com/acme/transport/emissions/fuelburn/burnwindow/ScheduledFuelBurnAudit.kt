package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled fuel burn audit is in its life cycle. */
enum class ScheduledFuelBurnAuditStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One scheduled fuel burn audit, with what the planners need to act on it. */
data class ScheduledFuelBurnAudit(
    var code: String,
    var label: String,
    var startsAt: Int,
    var maximum: Double,
    var currency: String,
    var unit: String,
    var distanceKm: Double,
    var status: ScheduledFuelBurnAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledFuelBurnAuditStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledFuelBurnAuditStatus): Boolean {
        when (status) {
            ScheduledFuelBurnAuditStatus.CREATED -> {
                when (next) {
                    ScheduledFuelBurnAuditStatus.VALIDATED, ScheduledFuelBurnAuditStatus.PUBLISHED -> return true
                }
                return false
            }
            ScheduledFuelBurnAuditStatus.VALIDATED -> {
                when (next) {
                    ScheduledFuelBurnAuditStatus.PUBLISHED, ScheduledFuelBurnAuditStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledFuelBurnAuditStatus.PUBLISHED -> {
                when (next) {
                    ScheduledFuelBurnAuditStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledFuelBurnAuditStatus) {
        if (!canMoveTo(next)) {
            error("scheduled fuel burn audit $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"scheduled fuel burn audit"} $code"

    fun weightedExposure(): Double = round(distanceKm * maximum * 100) / 100

    fun snapshot(): ScheduledFuelBurnAudit {
        return ScheduledFuelBurnAudit(
            code = code,
            label = label,
            startsAt = startsAt,
            maximum = maximum,
            currency = currency,
            unit = unit,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scheduled fuel burn audit in status created. */
fun newScheduledFuelBurnAudit(code: String, label: String): ScheduledFuelBurnAudit {
    return ScheduledFuelBurnAudit(
        code = code,
        label = label,
        startsAt = 0,
        maximum = 0.0,
        currency = "",
        unit = "",
        distanceKm = 0.0,
        status = ScheduledFuelBurnAuditStatus.CREATED,
    )
}
