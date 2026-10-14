package com.acme.transport.fleetops.tire.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling tire budget records of fleetops's tire work, and the rules, stores, and calculations around
// them.

/** Where a rolling tire budget is in its life cycle. */
enum class RollingTireBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One rolling tire budget, with what the planners need to act on it. */
data class RollingTireBudget(
    var code: String,
    var title: String,
    var trailerId: String,
    var vehicleId: String,
    var driverId: String,
    var name: String,
    var timezone: String,
    var version: Int,
    var updatedAt: Int,
    var segment: String,
    var stopCount: Int,
    var status: RollingTireBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTireBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTireBudgetStatus): Boolean {
        when (status) {
            RollingTireBudgetStatus.CREATED -> {
                when (next) {
                    RollingTireBudgetStatus.PUBLISHED -> return true
                }
                return false
            }
            RollingTireBudgetStatus.VALIDATED -> {
                when (next) {
                    RollingTireBudgetStatus.PUBLISHED, RollingTireBudgetStatus.RETIRED -> return true
                }
                return false
            }
            RollingTireBudgetStatus.PUBLISHED -> {
                when (next) {
                    RollingTireBudgetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingTireBudgetStatus) {
        if (!canMoveTo(next)) {
            error("rolling tire budget $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code / $title"

    fun projectedValue(): Double = round(updatedAt.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): RollingTireBudget {
        return RollingTireBudget(
            code = code,
            title = title,
            trailerId = trailerId,
            vehicleId = vehicleId,
            driverId = driverId,
            name = name,
            timezone = timezone,
            version = version,
            updatedAt = updatedAt,
            segment = segment,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a rolling tire budget in status created. */
fun newRollingTireBudget(code: String, title: String): RollingTireBudget {
    return RollingTireBudget(
        code = code,
        title = title,
        trailerId = "",
        vehicleId = "",
        driverId = "",
        name = "",
        timezone = "",
        version = 0,
        updatedAt = 0,
        segment = "",
        stopCount = 0,
        status = RollingTireBudgetStatus.CREATED,
    )
}
