package com.acme.transport.terminal.dockdoor.metric

import com.acme.transport.kit.text.normalize

// The dock door metric records of terminal's dock door work, and the rules, stores, and calculations around
// them.

/** Where a dock door metric is in its life cycle. */
enum class DockDoorMetricStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A dock door metric. Its code is unique within a tenant. */
data class DockDoorMetric(
    var code: String,
    var name: String,
    var ownerId: String,
    var fuelLiters: Double,
    var vehicleId: String,
    var trailerId: String,
    var status: DockDoorMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorMetricStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorMetricStatus): Boolean {
        when (status) {
            DockDoorMetricStatus.PROPOSED -> return next == DockDoorMetricStatus.APPROVED || next == DockDoorMetricStatus.WITHDRAWN
            DockDoorMetricStatus.APPROVED -> return next == DockDoorMetricStatus.WITHDRAWN
            DockDoorMetricStatus.REJECTED -> return next == DockDoorMetricStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DockDoorMetricStatus) {
        if (!canMoveTo(next)) {
            error("dock door metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): DockDoorMetric {
        return DockDoorMetric(
            code = code,
            name = name,
            ownerId = ownerId,
            fuelLiters = fuelLiters,
            vehicleId = vehicleId,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dock door metric in status proposed. */
fun newDockDoorMetric(code: String, name: String): DockDoorMetric {
    return DockDoorMetric(
        code = code,
        name = name,
        ownerId = "",
        fuelLiters = 0.0,
        vehicleId = "",
        trailerId = "",
        status = DockDoorMetricStatus.PROPOSED,
    )
}
