package com.acme.transport.crewing.availability.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal availability cycle is in its life cycle. */
enum class SeasonalAvailabilityCycleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a seasonal availability cycle and its current status. */
data class SeasonalAvailabilityCycle(
    var code: String,
    var label: String,
    var summary: String,
    var batchSize: Int,
    var distanceKm: Double,
    var budget: Double,
    var active: Boolean,
    var carrier: String,
    var limit: Int,
    var driverId: String,
    var status: SeasonalAvailabilityCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalAvailabilityCycleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalAvailabilityCycleStatus): Boolean {
        when (status) {
            SeasonalAvailabilityCycleStatus.REQUESTED -> return next == SeasonalAvailabilityCycleStatus.GRANTED || next == SeasonalAvailabilityCycleStatus.EXPIRED
            SeasonalAvailabilityCycleStatus.GRANTED -> {
                when (next) {
                    SeasonalAvailabilityCycleStatus.REVOKED, SeasonalAvailabilityCycleStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalAvailabilityCycleStatus.REVOKED -> {
                when (next) {
                    SeasonalAvailabilityCycleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalAvailabilityCycleStatus) {
        if (!canMoveTo(next)) {
            error("seasonal availability cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal availability cycle"} $code"

    fun effectiveValue(): Double = round(budget * limit.toDouble() * 100) / 100

    fun snapshot(): SeasonalAvailabilityCycle {
        return SeasonalAvailabilityCycle(
            code = code,
            label = label,
            summary = summary,
            batchSize = batchSize,
            distanceKm = distanceKm,
            budget = budget,
            active = active,
            carrier = carrier,
            limit = limit,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal availability cycle in status requested. */
fun newSeasonalAvailabilityCycle(code: String, label: String): SeasonalAvailabilityCycle {
    return SeasonalAvailabilityCycle(
        code = code,
        label = label,
        summary = "",
        batchSize = 0,
        distanceKm = 0.0,
        budget = 0.0,
        active = false,
        carrier = "",
        limit = 0,
        driverId = "",
        status = SeasonalAvailabilityCycleStatus.REQUESTED,
    )
}
