package com.acme.transport.emissions.idling.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered idling slot is in its life cycle. */
enum class TieredIdlingSlotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A tiered idling slot as the idling team keeps it. */
data class TieredIdlingSlot(
    var code: String,
    var title: String,
    var approved: Boolean,
    var fuelLiters: Double,
    var priority: Int,
    var stopCount: Int,
    var summary: String,
    var unit: String,
    var region: String,
    var status: TieredIdlingSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredIdlingSlotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredIdlingSlotStatus): Boolean {
        when (status) {
            TieredIdlingSlotStatus.DRAFT -> {
                when (next) {
                    TieredIdlingSlotStatus.CLOSED -> return true
                }
                return false
            }
            TieredIdlingSlotStatus.PENDING -> return next == TieredIdlingSlotStatus.SUSPENDED
            TieredIdlingSlotStatus.ACTIVE -> return next == TieredIdlingSlotStatus.SUSPENDED
            TieredIdlingSlotStatus.SUSPENDED -> {
                when (next) {
                    TieredIdlingSlotStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredIdlingSlotStatus) {
        if (!canMoveTo(next)) {
            error("tiered idling slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedValue(): Double = round(stopCount.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): TieredIdlingSlot {
        return TieredIdlingSlot(
            code = code,
            title = title,
            approved = approved,
            fuelLiters = fuelLiters,
            priority = priority,
            stopCount = stopCount,
            summary = summary,
            unit = unit,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a tiered idling slot in status draft. */
fun newTieredIdlingSlot(code: String, title: String): TieredIdlingSlot {
    return TieredIdlingSlot(
        code = code,
        title = title,
        approved = false,
        fuelLiters = 0.0,
        priority = 0,
        stopCount = 0,
        summary = "",
        unit = "",
        region = "",
        status = TieredIdlingSlotStatus.DRAFT,
    )
}
