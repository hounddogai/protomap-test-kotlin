package com.acme.transport.freightpay.accessorial.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The accessorial cycle records of freightpay's accessorial work, and the rules, stores, and calculations
// around them.

/** Where a accessorial cycle is in its life cycle. */
enum class AccessorialCycleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A accessorial cycle as the accessorial team keeps it. */
data class AccessorialCycle(
    var code: String,
    var name: String,
    var category: String,
    var description: String,
    var ratio: Double,
    var depot: String,
    var sequence: Int,
    var fuelLiters: Double,
    var status: AccessorialCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialCycleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialCycleStatus): Boolean {
        when (status) {
            AccessorialCycleStatus.DRAFT -> return next == AccessorialCycleStatus.CLOSED
            AccessorialCycleStatus.PENDING -> {
                when (next) {
                    AccessorialCycleStatus.ACTIVE -> return true
                }
                return false
            }
            AccessorialCycleStatus.ACTIVE -> return next == AccessorialCycleStatus.CLOSED
            AccessorialCycleStatus.SUSPENDED -> return next == AccessorialCycleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AccessorialCycleStatus) {
        if (!canMoveTo(next)) {
            error("accessorial cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedValue(): Double = round(fuelLiters * sequence.toDouble() * 100) / 100

    fun snapshot(): AccessorialCycle {
        return AccessorialCycle(
            code = code,
            name = name,
            category = category,
            description = description,
            ratio = ratio,
            depot = depot,
            sequence = sequence,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a accessorial cycle in status draft. */
fun newAccessorialCycle(code: String, name: String): AccessorialCycle {
    return AccessorialCycle(
        code = code,
        name = name,
        category = "",
        description = "",
        ratio = 0.0,
        depot = "",
        sequence = 0,
        fuelLiters = 0.0,
        status = AccessorialCycleStatus.DRAFT,
    )
}
