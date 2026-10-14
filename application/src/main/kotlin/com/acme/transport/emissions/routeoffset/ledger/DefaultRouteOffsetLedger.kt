package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default route offset ledger records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a default route offset ledger is in its life cycle. */
enum class DefaultRouteOffsetLedgerStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A default route offset ledger as the route offset team keeps it. */
data class DefaultRouteOffsetLedger(
    var code: String,
    var name: String,
    var ceiling: Double,
    var maximum: Double,
    var vehicleId: String,
    var windowDays: Int,
    var fuelLiters: Double,
    var status: DefaultRouteOffsetLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRouteOffsetLedgerStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRouteOffsetLedgerStatus): Boolean {
        when (status) {
            DefaultRouteOffsetLedgerStatus.PLANNED -> return next == DefaultRouteOffsetLedgerStatus.SCHEDULED || next == DefaultRouteOffsetLedgerStatus.IN_PROGRESS
            DefaultRouteOffsetLedgerStatus.SCHEDULED -> return next == DefaultRouteOffsetLedgerStatus.IN_PROGRESS || next == DefaultRouteOffsetLedgerStatus.COMPLETED
            DefaultRouteOffsetLedgerStatus.IN_PROGRESS -> return next == DefaultRouteOffsetLedgerStatus.CANCELLED
            DefaultRouteOffsetLedgerStatus.COMPLETED -> {
                when (next) {
                    DefaultRouteOffsetLedgerStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultRouteOffsetLedgerStatus) {
        if (!canMoveTo(next)) {
            error("default route offset ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedLoad(): Double = round((maximum - ceiling) * 100) / 100

    fun snapshot(): DefaultRouteOffsetLedger {
        return DefaultRouteOffsetLedger(
            code = code,
            name = name,
            ceiling = ceiling,
            maximum = maximum,
            vehicleId = vehicleId,
            windowDays = windowDays,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default route offset ledger in status planned. */
fun newDefaultRouteOffsetLedger(code: String, name: String): DefaultRouteOffsetLedger {
    return DefaultRouteOffsetLedger(
        code = code,
        name = name,
        ceiling = 0.0,
        maximum = 0.0,
        vehicleId = "",
        windowDays = 0,
        fuelLiters = 0.0,
        status = DefaultRouteOffsetLedgerStatus.PLANNED,
    )
}
