package com.acme.transport.fleetops.tire.tirebudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual tire budget records of fleetops's tire work, and the rules, stores, and calculations around
// them.

/** Where a manual tire budget is in its life cycle. */
enum class ManualTireBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A manual tire budget as the tire team keeps it. */
data class ManualTireBudget(
    var code: String,
    var name: String,
    var summary: String,
    var trailerId: String,
    var durationMinutes: Int,
    var createdAt: Int,
    var status: ManualTireBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTireBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTireBudgetStatus): Boolean {
        when (status) {
            ManualTireBudgetStatus.IDLE -> {
                when (next) {
                    ManualTireBudgetStatus.READY -> return true
                }
                return false
            }
            ManualTireBudgetStatus.WARMING -> {
                when (next) {
                    ManualTireBudgetStatus.READY, ManualTireBudgetStatus.DRAINING -> return true
                }
                return false
            }
            ManualTireBudgetStatus.READY -> {
                when (next) {
                    ManualTireBudgetStatus.DRAINING, ManualTireBudgetStatus.STOPPED -> return true
                }
                return false
            }
            ManualTireBudgetStatus.DRAINING -> return next == ManualTireBudgetStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ManualTireBudgetStatus) {
        if (!canMoveTo(next)) {
            error("manual tire budget $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun effectiveTotal(): Double = round(durationMinutes.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): ManualTireBudget {
        return ManualTireBudget(
            code = code,
            name = name,
            summary = summary,
            trailerId = trailerId,
            durationMinutes = durationMinutes,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual tire budget in status idle. */
fun newManualTireBudget(code: String, name: String): ManualTireBudget {
    return ManualTireBudget(
        code = code,
        name = name,
        summary = "",
        trailerId = "",
        durationMinutes = 0,
        createdAt = 0,
        status = ManualTireBudgetStatus.IDLE,
    )
}
