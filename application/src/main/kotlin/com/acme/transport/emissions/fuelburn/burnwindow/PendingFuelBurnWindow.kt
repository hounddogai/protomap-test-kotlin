package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending fuel burn window records of emissions's fuel burn work, and the rules, stores, and calculations
// around them.

/** Where a pending fuel burn window is in its life cycle. */
enum class PendingFuelBurnWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A pending fuel burn window. Its code is unique within a tenant. */
data class PendingFuelBurnWindow(
    var code: String,
    var title: String,
    var shiftCode: String,
    var unit: String,
    var phone: String,
    var tags: MutableList<String>,
    var ratio: Double,
    var graceDays: Int,
    var trailerId: String,
    var lane: String,
    var status: PendingFuelBurnWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingFuelBurnWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingFuelBurnWindowStatus): Boolean {
        when (status) {
            PendingFuelBurnWindowStatus.DRAFT -> {
                when (next) {
                    PendingFuelBurnWindowStatus.SUSPENDED, PendingFuelBurnWindowStatus.CLOSED -> return true
                }
                return false
            }
            PendingFuelBurnWindowStatus.PENDING -> return next == PendingFuelBurnWindowStatus.CLOSED
            PendingFuelBurnWindowStatus.ACTIVE -> {
                when (next) {
                    PendingFuelBurnWindowStatus.CLOSED -> return true
                }
                return false
            }
            PendingFuelBurnWindowStatus.SUSPENDED -> {
                when (next) {
                    PendingFuelBurnWindowStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingFuelBurnWindowStatus) {
        if (!canMoveTo(next)) {
            error("pending fuel burn window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netVolume(): Double = round((ratio - graceDays.toDouble()) * 100) / 100

    fun snapshot(): PendingFuelBurnWindow {
        return PendingFuelBurnWindow(
            code = code,
            title = title,
            shiftCode = shiftCode,
            unit = unit,
            phone = phone,
            tags = tags,
            ratio = ratio,
            graceDays = graceDays,
            trailerId = trailerId,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a pending fuel burn window in status draft. */
fun newPendingFuelBurnWindow(code: String, title: String): PendingFuelBurnWindow {
    return PendingFuelBurnWindow(
        code = code,
        title = title,
        shiftCode = "",
        unit = "",
        phone = "",
        tags = mutableListOf<String>(),
        ratio = 0.0,
        graceDays = 0,
        trailerId = "",
        lane = "",
        status = PendingFuelBurnWindowStatus.DRAFT,
    )
}
