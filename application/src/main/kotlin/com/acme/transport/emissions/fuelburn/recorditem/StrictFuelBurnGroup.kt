package com.acme.transport.emissions.fuelburn.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict fuel burn group is in its life cycle. */
enum class StrictFuelBurnGroupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A strict fuel burn group as the fuel burn team keeps it. */
data class StrictFuelBurnGroup(
    var code: String,
    var label: String,
    var name: String,
    var unit: String,
    var pallets: Int,
    var createdAt: Int,
    var segment: String,
    var status: StrictFuelBurnGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictFuelBurnGroupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictFuelBurnGroupStatus): Boolean {
        when (status) {
            StrictFuelBurnGroupStatus.NEW -> {
                when (next) {
                    StrictFuelBurnGroupStatus.DONE, StrictFuelBurnGroupStatus.FAILED -> return true
                }
                return false
            }
            StrictFuelBurnGroupStatus.QUEUED -> return next == StrictFuelBurnGroupStatus.DONE
            StrictFuelBurnGroupStatus.RUNNING -> return next == StrictFuelBurnGroupStatus.DONE || next == StrictFuelBurnGroupStatus.FAILED
            StrictFuelBurnGroupStatus.DONE -> {
                when (next) {
                    StrictFuelBurnGroupStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictFuelBurnGroupStatus) {
        if (!canMoveTo(next)) {
            error("strict fuel burn group $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveTotal(): Double = round((pallets.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): StrictFuelBurnGroup {
        return StrictFuelBurnGroup(
            code = code,
            label = label,
            name = name,
            unit = unit,
            pallets = pallets,
            createdAt = createdAt,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a strict fuel burn group in status new. */
fun newStrictFuelBurnGroup(code: String, label: String): StrictFuelBurnGroup {
    return StrictFuelBurnGroup(
        code = code,
        label = label,
        name = "",
        unit = "",
        pallets = 0,
        createdAt = 0,
        segment = "",
        status = StrictFuelBurnGroupStatus.NEW,
    )
}
