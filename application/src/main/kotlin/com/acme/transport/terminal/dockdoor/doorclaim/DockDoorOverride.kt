package com.acme.transport.terminal.dockdoor.doorclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door override is in its life cycle. */
enum class DockDoorOverrideStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One dock door override, with what the planners need to act on it. */
data class DockDoorOverride(
    var code: String,
    var label: String,
    var shiftCode: String,
    var driverId: String,
    var windowDays: Int,
    var routeCode: String,
    var loadKg: Double,
    var batchSize: Int,
    var trailerId: String,
    var status: DockDoorOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorOverrideStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorOverrideStatus): Boolean {
        when (status) {
            DockDoorOverrideStatus.CREATED -> {
                when (next) {
                    DockDoorOverrideStatus.VALIDATED, DockDoorOverrideStatus.RETIRED -> return true
                }
                return false
            }
            DockDoorOverrideStatus.VALIDATED -> {
                when (next) {
                    DockDoorOverrideStatus.PUBLISHED, DockDoorOverrideStatus.RETIRED -> return true
                }
                return false
            }
            DockDoorOverrideStatus.PUBLISHED -> return next == DockDoorOverrideStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DockDoorOverrideStatus) {
        if (!canMoveTo(next)) {
            error("dock door override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"dock door override"} $code"

    fun projectedTotal(): Double = round(batchSize.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): DockDoorOverride {
        return DockDoorOverride(
            code = code,
            label = label,
            shiftCode = shiftCode,
            driverId = driverId,
            windowDays = windowDays,
            routeCode = routeCode,
            loadKg = loadKg,
            batchSize = batchSize,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a dock door override in status created. */
fun newDockDoorOverride(code: String, label: String): DockDoorOverride {
    return DockDoorOverride(
        code = code,
        label = label,
        shiftCode = "",
        driverId = "",
        windowDays = 0,
        routeCode = "",
        loadKg = 0.0,
        batchSize = 0,
        trailerId = "",
        status = DockDoorOverrideStatus.CREATED,
    )
}
