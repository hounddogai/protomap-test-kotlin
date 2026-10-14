package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire slot is in its life cycle. */
enum class TireSlotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tire slot as the tire team keeps it. */
data class TireSlot(
    var code: String,
    var label: String,
    var margin: Double,
    var timezone: String,
    var stopCount: Int,
    var maximum: Double,
    var windowDays: Int,
    var odometer: Int,
    var description: String,
    var country: String,
    var amount: Double,
    var status: TireSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireSlotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TireSlotStatus): Boolean {
        when (status) {
            TireSlotStatus.PROPOSED -> {
                when (next) {
                    TireSlotStatus.APPROVED -> return true
                }
                return false
            }
            TireSlotStatus.APPROVED -> {
                when (next) {
                    TireSlotStatus.REJECTED, TireSlotStatus.WITHDRAWN -> return true
                }
                return false
            }
            TireSlotStatus.REJECTED -> {
                when (next) {
                    TireSlotStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireSlotStatus) {
        if (!canMoveTo(next)) {
            error("tire slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedTotal(): Double = round((windowDays.toDouble() + maximum) * 100) / 100

    fun snapshot(): TireSlot {
        return TireSlot(
            code = code,
            label = label,
            margin = margin,
            timezone = timezone,
            stopCount = stopCount,
            maximum = maximum,
            windowDays = windowDays,
            odometer = odometer,
            description = description,
            country = country,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tire slot in status proposed. */
fun newTireSlot(code: String, label: String): TireSlot {
    return TireSlot(
        code = code,
        label = label,
        margin = 0.0,
        timezone = "",
        stopCount = 0,
        maximum = 0.0,
        windowDays = 0,
        odometer = 0,
        description = "",
        country = "",
        amount = 0.0,
        status = TireSlotStatus.PROPOSED,
    )
}
