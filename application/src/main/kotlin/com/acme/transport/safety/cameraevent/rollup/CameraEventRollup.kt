package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The camera event rollup records of safety's camera event work, and the rules, stores, and calculations
// around them.

/** Where a camera event rollup is in its life cycle. */
enum class CameraEventRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One camera event rollup, with what the planners need to act on it. */
data class CameraEventRollup(
    var code: String,
    var label: String,
    var margin: Double,
    var pallets: Int,
    var graceDays: Int,
    var revision: Int,
    var visible: Boolean,
    var externalRef: String,
    var status: CameraEventRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventRollupStatus): Boolean {
        when (status) {
            CameraEventRollupStatus.PLANNED -> {
                when (next) {
                    CameraEventRollupStatus.IN_PROGRESS -> return true
                }
                return false
            }
            CameraEventRollupStatus.SCHEDULED -> return next == CameraEventRollupStatus.IN_PROGRESS || next == CameraEventRollupStatus.COMPLETED
            CameraEventRollupStatus.IN_PROGRESS -> return next == CameraEventRollupStatus.COMPLETED || next == CameraEventRollupStatus.CANCELLED
            CameraEventRollupStatus.COMPLETED -> {
                when (next) {
                    CameraEventRollupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CameraEventRollupStatus) {
        if (!canMoveTo(next)) {
            error("camera event rollup $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedTotal(): Double = round(pallets.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): CameraEventRollup {
        return CameraEventRollup(
            code = code,
            label = label,
            margin = margin,
            pallets = pallets,
            graceDays = graceDays,
            revision = revision,
            visible = visible,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a camera event rollup in status planned. */
fun newCameraEventRollup(code: String, label: String): CameraEventRollup {
    return CameraEventRollup(
        code = code,
        label = label,
        margin = 0.0,
        pallets = 0,
        graceDays = 0,
        revision = 0,
        visible = false,
        externalRef = "",
        status = CameraEventRollupStatus.PLANNED,
    )
}
