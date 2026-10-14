package com.acme.transport.terminal.dockdoor.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door tier is in its life cycle. */
enum class DockDoorTierStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One dock door tier, with what the planners need to act on it. */
data class DockDoorTier(
    var code: String,
    var name: String,
    var floorValue: Double,
    var carrier: String,
    var archived: Boolean,
    var unit: String,
    var windowDays: Int,
    var description: String,
    var label: String,
    var threshold: Double,
    var status: DockDoorTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorTierStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorTierStatus): Boolean {
        when (status) {
            DockDoorTierStatus.IDLE -> {
                when (next) {
                    DockDoorTierStatus.DRAINING -> return true
                }
                return false
            }
            DockDoorTierStatus.WARMING -> {
                when (next) {
                    DockDoorTierStatus.DRAINING -> return true
                }
                return false
            }
            DockDoorTierStatus.READY -> return next == DockDoorTierStatus.DRAINING
            DockDoorTierStatus.DRAINING -> return next == DockDoorTierStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DockDoorTierStatus) {
        if (!canMoveTo(next)) {
            error("dock door tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($name)"

    fun effectiveVolume(): Double = round((threshold - floorValue) * 100) / 100

    fun snapshot(): DockDoorTier {
        return DockDoorTier(
            code = code,
            name = name,
            floorValue = floorValue,
            carrier = carrier,
            archived = archived,
            unit = unit,
            windowDays = windowDays,
            description = description,
            label = label,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a dock door tier in status idle. */
fun newDockDoorTier(code: String, name: String): DockDoorTier {
    return DockDoorTier(
        code = code,
        name = name,
        floorValue = 0.0,
        carrier = "",
        archived = false,
        unit = "",
        windowDays = 0,
        description = "",
        label = "",
        threshold = 0.0,
        status = DockDoorTierStatus.IDLE,
    )
}
