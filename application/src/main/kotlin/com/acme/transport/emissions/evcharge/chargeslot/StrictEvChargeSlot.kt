package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict ev charge slot records of emissions's ev charge work, and the rules, stores, and calculations
// around them.

/** Where a strict ev charge slot is in its life cycle. */
enum class StrictEvChargeSlotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One strict ev charge slot, with what the planners need to act on it. */
data class StrictEvChargeSlot(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var name: String,
    var startsAt: Int,
    var durationMinutes: Int,
    var approved: Boolean,
    var maximum: Double,
    var graceDays: Int,
    var status: StrictEvChargeSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictEvChargeSlotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictEvChargeSlotStatus): Boolean {
        when (status) {
            StrictEvChargeSlotStatus.PLANNED -> {
                when (next) {
                    StrictEvChargeSlotStatus.COMPLETED -> return true
                }
                return false
            }
            StrictEvChargeSlotStatus.SCHEDULED -> return next == StrictEvChargeSlotStatus.IN_PROGRESS || next == StrictEvChargeSlotStatus.CANCELLED
            StrictEvChargeSlotStatus.IN_PROGRESS -> {
                when (next) {
                    StrictEvChargeSlotStatus.COMPLETED -> return true
                }
                return false
            }
            StrictEvChargeSlotStatus.COMPLETED -> {
                when (next) {
                    StrictEvChargeSlotStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictEvChargeSlotStatus) {
        if (!canMoveTo(next)) {
            error("strict ev charge slot $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round((startsAt.toDouble() - maximum) * 100) / 100

    fun snapshot(): StrictEvChargeSlot {
        return StrictEvChargeSlot(
            code = code,
            label = label,
            attributes = attributes,
            name = name,
            startsAt = startsAt,
            durationMinutes = durationMinutes,
            approved = approved,
            maximum = maximum,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict ev charge slot in status planned. */
fun newStrictEvChargeSlot(code: String, label: String): StrictEvChargeSlot {
    return StrictEvChargeSlot(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        name = "",
        startsAt = 0,
        durationMinutes = 0,
        approved = false,
        maximum = 0.0,
        graceDays = 0,
        status = StrictEvChargeSlotStatus.PLANNED,
    )
}
