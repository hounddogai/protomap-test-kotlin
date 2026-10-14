package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn override is in its life cycle. */
enum class FuelBurnOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a fuel burn override and its current status. */
data class FuelBurnOverride(
    var code: String,
    var name: String,
    var startsAt: Int,
    var tags: MutableList<String>,
    var active: Boolean,
    var notes: String,
    var attempts: Int,
    var maximum: Double,
    var status: FuelBurnOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnOverrideStatus): Boolean {
        when (status) {
            FuelBurnOverrideStatus.DRAFT -> {
                when (next) {
                    FuelBurnOverrideStatus.ACTIVE -> return true
                }
                return false
            }
            FuelBurnOverrideStatus.PENDING -> {
                when (next) {
                    FuelBurnOverrideStatus.ACTIVE, FuelBurnOverrideStatus.SUSPENDED -> return true
                }
                return false
            }
            FuelBurnOverrideStatus.ACTIVE -> {
                when (next) {
                    FuelBurnOverrideStatus.SUSPENDED, FuelBurnOverrideStatus.CLOSED -> return true
                }
                return false
            }
            FuelBurnOverrideStatus.SUSPENDED -> {
                when (next) {
                    FuelBurnOverrideStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnOverrideStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn override $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fuel burn override"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedValue(): Double = round((attempts.toDouble() - maximum) * 100) / 100

    fun snapshot(): FuelBurnOverride {
        return FuelBurnOverride(
            code = code,
            name = name,
            startsAt = startsAt,
            tags = tags,
            active = active,
            notes = notes,
            attempts = attempts,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fuel burn override in status draft. */
fun newFuelBurnOverride(code: String, name: String): FuelBurnOverride {
    return FuelBurnOverride(
        code = code,
        name = name,
        startsAt = 0,
        tags = mutableListOf<String>(),
        active = false,
        notes = "",
        attempts = 0,
        maximum = 0.0,
        status = FuelBurnOverrideStatus.DRAFT,
    )
}
