package com.acme.transport.safety.cameraevent.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The camera event bundle records of safety's camera event work, and the rules, stores, and calculations
// around them.

/** Where a camera event bundle is in its life cycle. */
enum class CameraEventBundleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A camera event bundle. Its code is unique within a tenant. */
data class CameraEventBundle(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var floorValue: Double,
    var vehicleId: String,
    var discount: Double,
    var odometer: Int,
    var reviewer: String,
    var unit: String,
    var status: CameraEventBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventBundleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventBundleStatus): Boolean {
        when (status) {
            CameraEventBundleStatus.OPEN -> return next == CameraEventBundleStatus.RESOLVED
            CameraEventBundleStatus.IN_REVIEW -> {
                when (next) {
                    CameraEventBundleStatus.RESOLVED -> return true
                }
                return false
            }
            CameraEventBundleStatus.RESOLVED -> return next == CameraEventBundleStatus.REOPENED || next == CameraEventBundleStatus.ARCHIVED
            CameraEventBundleStatus.REOPENED -> return next == CameraEventBundleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CameraEventBundleStatus) {
        if (!canMoveTo(next)) {
            error("camera event bundle $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"camera event bundle"} $code"

    fun effectiveVolume(): Double = round(discount * odometer.toDouble() * 100) / 100

    fun snapshot(): CameraEventBundle {
        return CameraEventBundle(
            code = code,
            title = title,
            updatedAt = updatedAt,
            floorValue = floorValue,
            vehicleId = vehicleId,
            discount = discount,
            odometer = odometer,
            reviewer = reviewer,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a camera event bundle in status open. */
fun newCameraEventBundle(code: String, title: String): CameraEventBundle {
    return CameraEventBundle(
        code = code,
        title = title,
        updatedAt = 0,
        floorValue = 0.0,
        vehicleId = "",
        discount = 0.0,
        odometer = 0,
        reviewer = "",
        unit = "",
        status = CameraEventBundleStatus.OPEN,
    )
}
