package com.acme.transport.safety.cameraevent.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The camera event slot records of safety's camera event work, and the rules, stores, and calculations around
// them.

/** Where a camera event slot is in its life cycle. */
enum class CameraEventSlotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One camera event slot, with what the planners need to act on it. */
data class CameraEventSlot(
    var code: String,
    var title: String,
    var maximum: Double,
    var etaMinutes: Int,
    var unit: String,
    var shiftCode: String,
    var carrier: String,
    var priority: Int,
    var status: CameraEventSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventSlotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventSlotStatus): Boolean {
        when (status) {
            CameraEventSlotStatus.CREATED -> return next == CameraEventSlotStatus.VALIDATED || next == CameraEventSlotStatus.RETIRED
            CameraEventSlotStatus.VALIDATED -> {
                when (next) {
                    CameraEventSlotStatus.RETIRED -> return true
                }
                return false
            }
            CameraEventSlotStatus.PUBLISHED -> {
                when (next) {
                    CameraEventSlotStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CameraEventSlotStatus) {
        if (!canMoveTo(next)) {
            error("camera event slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveValue(): Double = round((etaMinutes.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): CameraEventSlot {
        return CameraEventSlot(
            code = code,
            title = title,
            maximum = maximum,
            etaMinutes = etaMinutes,
            unit = unit,
            shiftCode = shiftCode,
            carrier = carrier,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a camera event slot in status created. */
fun newCameraEventSlot(code: String, title: String): CameraEventSlot {
    return CameraEventSlot(
        code = code,
        title = title,
        maximum = 0.0,
        etaMinutes = 0,
        unit = "",
        shiftCode = "",
        carrier = "",
        priority = 0,
        status = CameraEventSlotStatus.CREATED,
    )
}
