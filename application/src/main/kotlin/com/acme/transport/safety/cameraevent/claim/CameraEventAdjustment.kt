package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event adjustment is in its life cycle. */
enum class CameraEventAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A camera event adjustment as the camera event team keeps it. */
data class CameraEventAdjustment(
    var code: String,
    var title: String,
    var carrier: String,
    var unit: String,
    var updatedAt: Int,
    var ownerId: String,
    var floorValue: Double,
    var shiftCode: String,
    var source: String,
    var status: CameraEventAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventAdjustmentStatus): Boolean {
        when (status) {
            CameraEventAdjustmentStatus.NEW -> {
                when (next) {
                    CameraEventAdjustmentStatus.DONE -> return true
                }
                return false
            }
            CameraEventAdjustmentStatus.QUEUED -> return next == CameraEventAdjustmentStatus.DONE || next == CameraEventAdjustmentStatus.FAILED
            CameraEventAdjustmentStatus.RUNNING -> return next == CameraEventAdjustmentStatus.DONE || next == CameraEventAdjustmentStatus.FAILED
            CameraEventAdjustmentStatus.DONE -> {
                when (next) {
                    CameraEventAdjustmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CameraEventAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("camera event adjustment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"camera event adjustment"} $code"

    fun projectedExposure(): Double = round((updatedAt.toDouble() - floorValue) * 100) / 100

    fun snapshot(): CameraEventAdjustment {
        return CameraEventAdjustment(
            code = code,
            title = title,
            carrier = carrier,
            unit = unit,
            updatedAt = updatedAt,
            ownerId = ownerId,
            floorValue = floorValue,
            shiftCode = shiftCode,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a camera event adjustment in status new. */
fun newCameraEventAdjustment(code: String, title: String): CameraEventAdjustment {
    return CameraEventAdjustment(
        code = code,
        title = title,
        carrier = "",
        unit = "",
        updatedAt = 0,
        ownerId = "",
        floorValue = 0.0,
        shiftCode = "",
        source = "",
        status = CameraEventAdjustmentStatus.NEW,
    )
}
