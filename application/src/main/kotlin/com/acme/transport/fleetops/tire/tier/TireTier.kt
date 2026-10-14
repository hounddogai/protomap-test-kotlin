package com.acme.transport.fleetops.tire.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tire tier records of fleetops's tire work, and the rules, stores, and calculations around them.

/** Where a tire tier is in its life cycle. */
enum class TireTierStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tire tier as the tire team keeps it. */
data class TireTier(
    var code: String,
    var name: String,
    var priority: Int,
    var weight: Double,
    var trailerId: String,
    var status: TireTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireTierStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireTierStatus): Boolean {
        when (status) {
            TireTierStatus.PLANNED -> {
                when (next) {
                    TireTierStatus.IN_PROGRESS, TireTierStatus.COMPLETED -> return true
                }
                return false
            }
            TireTierStatus.SCHEDULED -> {
                when (next) {
                    TireTierStatus.COMPLETED, TireTierStatus.CANCELLED -> return true
                }
                return false
            }
            TireTierStatus.IN_PROGRESS -> return next == TireTierStatus.COMPLETED || next == TireTierStatus.CANCELLED
            TireTierStatus.COMPLETED -> {
                when (next) {
                    TireTierStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireTierStatus) {
        if (!canMoveTo(next)) {
            error("tire tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netTotal(): Double = round(weight * priority.toDouble() * 100) / 100

    fun snapshot(): TireTier {
        return TireTier(
            code = code,
            name = name,
            priority = priority,
            weight = weight,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tire tier in status planned. */
fun newTireTier(code: String, name: String): TireTier {
    return TireTier(
        code = code,
        name = name,
        priority = 0,
        weight = 0.0,
        trailerId = "",
        status = TireTierStatus.PLANNED,
    )
}
