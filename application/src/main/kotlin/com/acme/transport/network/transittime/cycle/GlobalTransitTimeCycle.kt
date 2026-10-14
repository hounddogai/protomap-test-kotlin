package com.acme.transport.network.transittime.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global transit time cycle records of network's transit time work, and the rules, stores, and
// calculations around them.

/** Where a global transit time cycle is in its life cycle. */
enum class GlobalTransitTimeCycleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A global transit time cycle as the transit time team keeps it. */
data class GlobalTransitTimeCycle(
    var code: String,
    var label: String,
    var contactEmail: String,
    var rate: Double,
    var attempts: Int,
    var weight: Double,
    var carrier: String,
    var sequence: Int,
    var status: GlobalTransitTimeCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTransitTimeCycleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTransitTimeCycleStatus): Boolean {
        when (status) {
            GlobalTransitTimeCycleStatus.CREATED -> return next == GlobalTransitTimeCycleStatus.VALIDATED || next == GlobalTransitTimeCycleStatus.PUBLISHED
            GlobalTransitTimeCycleStatus.VALIDATED -> return next == GlobalTransitTimeCycleStatus.PUBLISHED || next == GlobalTransitTimeCycleStatus.RETIRED
            GlobalTransitTimeCycleStatus.PUBLISHED -> {
                when (next) {
                    GlobalTransitTimeCycleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalTransitTimeCycleStatus) {
        if (!canMoveTo(next)) {
            error("global transit time cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedTotal(): Double = round(attempts.toDouble() * rate * 100) / 100

    fun snapshot(): GlobalTransitTimeCycle {
        return GlobalTransitTimeCycle(
            code = code,
            label = label,
            contactEmail = contactEmail,
            rate = rate,
            attempts = attempts,
            weight = weight,
            carrier = carrier,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global transit time cycle in status created. */
fun newGlobalTransitTimeCycle(code: String, label: String): GlobalTransitTimeCycle {
    return GlobalTransitTimeCycle(
        code = code,
        label = label,
        contactEmail = "",
        rate = 0.0,
        attempts = 0,
        weight = 0.0,
        carrier = "",
        sequence = 0,
        status = GlobalTransitTimeCycleStatus.CREATED,
    )
}
