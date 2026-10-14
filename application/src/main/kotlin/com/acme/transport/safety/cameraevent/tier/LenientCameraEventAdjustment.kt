package com.acme.transport.safety.cameraevent.tier

import com.acme.transport.kit.text.normalize

/** Where a lenient camera event adjustment is in its life cycle. */
enum class LenientCameraEventAdjustmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A lenient camera event adjustment as the camera event team keeps it. */
data class LenientCameraEventAdjustment(
    var code: String,
    var label: String,
    var unit: String,
    var trailerId: String,
    var version: Int,
    var status: LenientCameraEventAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCameraEventAdjustmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCameraEventAdjustmentStatus): Boolean {
        when (status) {
            LenientCameraEventAdjustmentStatus.CREATED -> return next == LenientCameraEventAdjustmentStatus.VALIDATED || next == LenientCameraEventAdjustmentStatus.RETIRED
            LenientCameraEventAdjustmentStatus.VALIDATED -> {
                when (next) {
                    LenientCameraEventAdjustmentStatus.PUBLISHED, LenientCameraEventAdjustmentStatus.RETIRED -> return true
                }
                return false
            }
            LenientCameraEventAdjustmentStatus.PUBLISHED -> {
                when (next) {
                    LenientCameraEventAdjustmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientCameraEventAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("lenient camera event adjustment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): LenientCameraEventAdjustment {
        return LenientCameraEventAdjustment(
            code = code,
            label = label,
            unit = unit,
            trailerId = trailerId,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lenient camera event adjustment in status created. */
fun newLenientCameraEventAdjustment(code: String, label: String): LenientCameraEventAdjustment {
    return LenientCameraEventAdjustment(
        code = code,
        label = label,
        unit = "",
        trailerId = "",
        version = 0,
        status = LenientCameraEventAdjustmentStatus.CREATED,
    )
}
