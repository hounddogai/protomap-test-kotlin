package com.acme.transport.safety.cameraevent.eventslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual camera event slot records of safety's camera event work, and the rules, stores, and calculations
// around them.

/** Where a manual camera event slot is in its life cycle. */
enum class ManualCameraEventSlotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A manual camera event slot as the camera event team keeps it. */
data class ManualCameraEventSlot(
    var code: String,
    var label: String,
    var source: String,
    var priority: Int,
    var weight: Double,
    var status: ManualCameraEventSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCameraEventSlotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCameraEventSlotStatus): Boolean {
        when (status) {
            ManualCameraEventSlotStatus.PROPOSED -> return next == ManualCameraEventSlotStatus.REJECTED || next == ManualCameraEventSlotStatus.WITHDRAWN
            ManualCameraEventSlotStatus.APPROVED -> {
                when (next) {
                    ManualCameraEventSlotStatus.REJECTED -> return true
                }
                return false
            }
            ManualCameraEventSlotStatus.REJECTED -> {
                when (next) {
                    ManualCameraEventSlotStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualCameraEventSlotStatus) {
        if (!canMoveTo(next)) {
            error("manual camera event slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedValue(): Double = round(priority.toDouble() * weight * 100) / 100

    fun snapshot(): ManualCameraEventSlot {
        return ManualCameraEventSlot(
            code = code,
            label = label,
            source = source,
            priority = priority,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a manual camera event slot in status proposed. */
fun newManualCameraEventSlot(code: String, label: String): ManualCameraEventSlot {
    return ManualCameraEventSlot(
        code = code,
        label = label,
        source = "",
        priority = 0,
        weight = 0.0,
        status = ManualCameraEventSlotStatus.PROPOSED,
    )
}
