package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn milestone is in its life cycle. */
enum class FuelBurnMilestoneStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One fuel burn milestone, with what the planners need to act on it. */
data class FuelBurnMilestone(
    var code: String,
    var title: String,
    var phone: String,
    var routeCode: String,
    var active: Boolean,
    var sequence: Int,
    var ratio: Double,
    var updatedAt: Int,
    var durationMinutes: Int,
    var status: FuelBurnMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnMilestoneStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnMilestoneStatus): Boolean {
        when (status) {
            FuelBurnMilestoneStatus.DRAFT -> return next == FuelBurnMilestoneStatus.ACTIVE || next == FuelBurnMilestoneStatus.CLOSED
            FuelBurnMilestoneStatus.PENDING -> return next == FuelBurnMilestoneStatus.ACTIVE || next == FuelBurnMilestoneStatus.SUSPENDED
            FuelBurnMilestoneStatus.ACTIVE -> {
                when (next) {
                    FuelBurnMilestoneStatus.SUSPENDED -> return true
                }
                return false
            }
            FuelBurnMilestoneStatus.SUSPENDED -> {
                when (next) {
                    FuelBurnMilestoneStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn milestone $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netValue(): Double = round((sequence.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): FuelBurnMilestone {
        return FuelBurnMilestone(
            code = code,
            title = title,
            phone = phone,
            routeCode = routeCode,
            active = active,
            sequence = sequence,
            ratio = ratio,
            updatedAt = updatedAt,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fuel burn milestone in status draft. */
fun newFuelBurnMilestone(code: String, title: String): FuelBurnMilestone {
    return FuelBurnMilestone(
        code = code,
        title = title,
        phone = "",
        routeCode = "",
        active = false,
        sequence = 0,
        ratio = 0.0,
        updatedAt = 0,
        durationMinutes = 0,
        status = FuelBurnMilestoneStatus.DRAFT,
    )
}
