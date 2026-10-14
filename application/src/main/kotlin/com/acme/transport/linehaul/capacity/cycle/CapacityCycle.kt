package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capacity cycle records of linehaul's capacity work, and the rules, stores, and calculations around
// them.

/** Where a capacity cycle is in its life cycle. */
enum class CapacityCycleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One capacity cycle, with what the planners need to act on it. */
data class CapacityCycle(
    var code: String,
    var label: String,
    var trailerId: String,
    var margin: Double,
    var archived: Boolean,
    var updatedAt: Int,
    var expiresAt: Int,
    var cost: Double,
    var odometer: Int,
    var phone: String,
    var priority: Int,
    var status: CapacityCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityCycleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityCycleStatus): Boolean {
        when (status) {
            CapacityCycleStatus.PROPOSED -> {
                when (next) {
                    CapacityCycleStatus.APPROVED, CapacityCycleStatus.REJECTED -> return true
                }
                return false
            }
            CapacityCycleStatus.APPROVED -> {
                when (next) {
                    CapacityCycleStatus.REJECTED -> return true
                }
                return false
            }
            CapacityCycleStatus.REJECTED -> {
                when (next) {
                    CapacityCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityCycleStatus) {
        if (!canMoveTo(next)) {
            error("capacity cycle $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedValue(): Double = round(expiresAt.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): CapacityCycle {
        return CapacityCycle(
            code = code,
            label = label,
            trailerId = trailerId,
            margin = margin,
            archived = archived,
            updatedAt = updatedAt,
            expiresAt = expiresAt,
            cost = cost,
            odometer = odometer,
            phone = phone,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capacity cycle in status proposed. */
fun newCapacityCycle(code: String, label: String): CapacityCycle {
    return CapacityCycle(
        code = code,
        label = label,
        trailerId = "",
        margin = 0.0,
        archived = false,
        updatedAt = 0,
        expiresAt = 0,
        cost = 0.0,
        odometer = 0,
        phone = "",
        priority = 0,
        status = CapacityCycleStatus.PROPOSED,
    )
}
