package com.acme.transport.safety.cameraevent.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event target is in its life cycle. */
enum class CameraEventTargetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A camera event target. Its code is unique within a tenant. */
data class CameraEventTarget(
    var code: String,
    var label: String,
    var discount: Double,
    var priority: Int,
    var odometer: Int,
    var maximum: Double,
    var status: CameraEventTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventTargetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventTargetStatus): Boolean {
        when (status) {
            CameraEventTargetStatus.REQUESTED -> return next == CameraEventTargetStatus.REVOKED || next == CameraEventTargetStatus.EXPIRED
            CameraEventTargetStatus.GRANTED -> return next == CameraEventTargetStatus.REVOKED
            CameraEventTargetStatus.REVOKED -> {
                when (next) {
                    CameraEventTargetStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CameraEventTargetStatus) {
        if (!canMoveTo(next)) {
            error("camera event target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netLoad(): Double = round((discount - maximum) * 100) / 100

    fun snapshot(): CameraEventTarget {
        return CameraEventTarget(
            code = code,
            label = label,
            discount = discount,
            priority = priority,
            odometer = odometer,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a camera event target in status requested. */
fun newCameraEventTarget(code: String, label: String): CameraEventTarget {
    return CameraEventTarget(
        code = code,
        label = label,
        discount = 0.0,
        priority = 0,
        odometer = 0,
        maximum = 0.0,
        status = CameraEventTargetStatus.REQUESTED,
    )
}
