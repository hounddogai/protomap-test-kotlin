package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered capacity schedule is in its life cycle. */
enum class TieredCapacityScheduleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a tiered capacity schedule and its current status. */
data class TieredCapacitySchedule(
    var code: String,
    var name: String,
    var capacity: Int,
    var cost: Double,
    var trailerId: String,
    var status: TieredCapacityScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCapacityScheduleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCapacityScheduleStatus): Boolean {
        when (status) {
            TieredCapacityScheduleStatus.CREATED -> return next == TieredCapacityScheduleStatus.PUBLISHED
            TieredCapacityScheduleStatus.VALIDATED -> return next == TieredCapacityScheduleStatus.RETIRED
            TieredCapacityScheduleStatus.PUBLISHED -> return next == TieredCapacityScheduleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TieredCapacityScheduleStatus) {
        if (!canMoveTo(next)) {
            error("tiered capacity schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedTotal(): Double = round((cost + capacity.toDouble()) * 100) / 100

    fun snapshot(): TieredCapacitySchedule {
        return TieredCapacitySchedule(
            code = code,
            name = name,
            capacity = capacity,
            cost = cost,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered capacity schedule in status created. */
fun newTieredCapacitySchedule(code: String, name: String): TieredCapacitySchedule {
    return TieredCapacitySchedule(
        code = code,
        name = name,
        capacity = 0,
        cost = 0.0,
        trailerId = "",
        status = TieredCapacityScheduleStatus.CREATED,
    )
}
