package com.acme.transport.lastmile.redelivery.redeliveryquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal redelivery group is in its life cycle. */
enum class SeasonalRedeliveryGroupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a seasonal redelivery group and its current status. */
data class SeasonalRedeliveryGroup(
    var code: String,
    var label: String,
    var ceiling: Double,
    var source: String,
    var cost: Double,
    var odometer: Int,
    var externalRef: String,
    var graceDays: Int,
    var unit: String,
    var budget: Double,
    var status: SeasonalRedeliveryGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRedeliveryGroupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRedeliveryGroupStatus): Boolean {
        when (status) {
            SeasonalRedeliveryGroupStatus.PLANNED -> {
                when (next) {
                    SeasonalRedeliveryGroupStatus.COMPLETED, SeasonalRedeliveryGroupStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalRedeliveryGroupStatus.SCHEDULED -> return next == SeasonalRedeliveryGroupStatus.IN_PROGRESS || next == SeasonalRedeliveryGroupStatus.CANCELLED
            SeasonalRedeliveryGroupStatus.IN_PROGRESS -> return next == SeasonalRedeliveryGroupStatus.CANCELLED
            SeasonalRedeliveryGroupStatus.COMPLETED -> return next == SeasonalRedeliveryGroupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SeasonalRedeliveryGroupStatus) {
        if (!canMoveTo(next)) {
            error("seasonal redelivery group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedExposure(): Double = round((cost + graceDays.toDouble()) * 100) / 100

    fun snapshot(): SeasonalRedeliveryGroup {
        return SeasonalRedeliveryGroup(
            code = code,
            label = label,
            ceiling = ceiling,
            source = source,
            cost = cost,
            odometer = odometer,
            externalRef = externalRef,
            graceDays = graceDays,
            unit = unit,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a seasonal redelivery group in status planned. */
fun newSeasonalRedeliveryGroup(code: String, label: String): SeasonalRedeliveryGroup {
    return SeasonalRedeliveryGroup(
        code = code,
        label = label,
        ceiling = 0.0,
        source = "",
        cost = 0.0,
        odometer = 0,
        externalRef = "",
        graceDays = 0,
        unit = "",
        budget = 0.0,
        status = SeasonalRedeliveryGroupStatus.PLANNED,
    )
}
