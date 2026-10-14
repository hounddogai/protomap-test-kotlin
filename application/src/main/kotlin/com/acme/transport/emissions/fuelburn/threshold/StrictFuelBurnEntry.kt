package com.acme.transport.emissions.fuelburn.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict fuel burn entry is in its life cycle. */
enum class StrictFuelBurnEntryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One strict fuel burn entry, with what the planners need to act on it. */
data class StrictFuelBurnEntry(
    var code: String,
    var label: String,
    var cost: Double,
    var weight: Double,
    var priority: Int,
    var status: StrictFuelBurnEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictFuelBurnEntryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictFuelBurnEntryStatus): Boolean {
        when (status) {
            StrictFuelBurnEntryStatus.DRAFT -> {
                when (next) {
                    StrictFuelBurnEntryStatus.PENDING, StrictFuelBurnEntryStatus.SUSPENDED -> return true
                }
                return false
            }
            StrictFuelBurnEntryStatus.PENDING -> {
                when (next) {
                    StrictFuelBurnEntryStatus.SUSPENDED, StrictFuelBurnEntryStatus.CLOSED -> return true
                }
                return false
            }
            StrictFuelBurnEntryStatus.ACTIVE -> return next == StrictFuelBurnEntryStatus.CLOSED
            StrictFuelBurnEntryStatus.SUSPENDED -> return next == StrictFuelBurnEntryStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: StrictFuelBurnEntryStatus) {
        if (!canMoveTo(next)) {
            error("strict fuel burn entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedVolume(): Double = round((priority.toDouble() + cost) * 100) / 100

    fun snapshot(): StrictFuelBurnEntry {
        return StrictFuelBurnEntry(
            code = code,
            label = label,
            cost = cost,
            weight = weight,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict fuel burn entry in status draft. */
fun newStrictFuelBurnEntry(code: String, label: String): StrictFuelBurnEntry {
    return StrictFuelBurnEntry(
        code = code,
        label = label,
        cost = 0.0,
        weight = 0.0,
        priority = 0,
        status = StrictFuelBurnEntryStatus.DRAFT,
    )
}
