package com.acme.transport.emissions.fuelburn.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn entry is in its life cycle. */
enum class FuelBurnEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One fuel burn entry, with what the planners need to act on it. */
data class FuelBurnEntry(
    var code: String,
    var label: String,
    var budget: Double,
    var createdAt: Int,
    var ceiling: Double,
    var amount: Double,
    var limit: Int,
    var tags: MutableList<String>,
    var locale: String,
    var status: FuelBurnEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnEntryStatus): Boolean {
        when (status) {
            FuelBurnEntryStatus.REQUESTED -> return next == FuelBurnEntryStatus.REVOKED
            FuelBurnEntryStatus.GRANTED -> return next == FuelBurnEntryStatus.REVOKED || next == FuelBurnEntryStatus.EXPIRED
            FuelBurnEntryStatus.REVOKED -> {
                when (next) {
                    FuelBurnEntryStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnEntryStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn entry $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round(amount * createdAt.toDouble() * 100) / 100

    fun snapshot(): FuelBurnEntry {
        return FuelBurnEntry(
            code = code,
            label = label,
            budget = budget,
            createdAt = createdAt,
            ceiling = ceiling,
            amount = amount,
            limit = limit,
            tags = tags,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fuel burn entry in status requested. */
fun newFuelBurnEntry(code: String, label: String): FuelBurnEntry {
    return FuelBurnEntry(
        code = code,
        label = label,
        budget = 0.0,
        createdAt = 0,
        ceiling = 0.0,
        amount = 0.0,
        limit = 0,
        tags = mutableListOf<String>(),
        locale = "",
        status = FuelBurnEntryStatus.REQUESTED,
    )
}
