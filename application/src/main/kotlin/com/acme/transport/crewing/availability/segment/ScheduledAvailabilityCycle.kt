package com.acme.transport.crewing.availability.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled availability cycle is in its life cycle. */
enum class ScheduledAvailabilityCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a scheduled availability cycle and its current status. */
data class ScheduledAvailabilityCycle(
    var code: String,
    var name: String,
    var visible: Boolean,
    var country: String,
    var pinned: Boolean,
    var lane: String,
    var summary: String,
    var score: Double,
    var fuelLiters: Double,
    var sequence: Int,
    var status: ScheduledAvailabilityCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledAvailabilityCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledAvailabilityCycleStatus): Boolean {
        when (status) {
            ScheduledAvailabilityCycleStatus.PLANNED -> {
                when (next) {
                    ScheduledAvailabilityCycleStatus.CANCELLED -> return true
                }
                return false
            }
            ScheduledAvailabilityCycleStatus.SCHEDULED -> return next == ScheduledAvailabilityCycleStatus.COMPLETED || next == ScheduledAvailabilityCycleStatus.CANCELLED
            ScheduledAvailabilityCycleStatus.IN_PROGRESS -> return next == ScheduledAvailabilityCycleStatus.COMPLETED || next == ScheduledAvailabilityCycleStatus.CANCELLED
            ScheduledAvailabilityCycleStatus.COMPLETED -> return next == ScheduledAvailabilityCycleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ScheduledAvailabilityCycleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled availability cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveExposure(): Double = round(fuelLiters * score * 100) / 100

    fun snapshot(): ScheduledAvailabilityCycle {
        return ScheduledAvailabilityCycle(
            code = code,
            name = name,
            visible = visible,
            country = country,
            pinned = pinned,
            lane = lane,
            summary = summary,
            score = score,
            fuelLiters = fuelLiters,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a scheduled availability cycle in status planned. */
fun newScheduledAvailabilityCycle(code: String, name: String): ScheduledAvailabilityCycle {
    return ScheduledAvailabilityCycle(
        code = code,
        name = name,
        visible = false,
        country = "",
        pinned = false,
        lane = "",
        summary = "",
        score = 0.0,
        fuelLiters = 0.0,
        sequence = 0,
        status = ScheduledAvailabilityCycleStatus.PLANNED,
    )
}
