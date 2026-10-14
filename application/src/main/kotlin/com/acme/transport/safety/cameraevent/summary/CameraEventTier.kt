package com.acme.transport.safety.cameraevent.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event tier is in its life cycle. */
enum class CameraEventTierStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A camera event tier as the camera event team keeps it. */
data class CameraEventTier(
    var code: String,
    var title: String,
    var maximum: Double,
    var quantity: Int,
    var depot: String,
    var stopCount: Int,
    var status: CameraEventTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventTierStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventTierStatus): Boolean {
        when (status) {
            CameraEventTierStatus.NEW -> return next == CameraEventTierStatus.RUNNING
            CameraEventTierStatus.QUEUED -> {
                when (next) {
                    CameraEventTierStatus.RUNNING, CameraEventTierStatus.DONE -> return true
                }
                return false
            }
            CameraEventTierStatus.RUNNING -> return next == CameraEventTierStatus.DONE
            CameraEventTierStatus.DONE -> {
                when (next) {
                    CameraEventTierStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CameraEventTierStatus) {
        if (!canMoveTo(next)) {
            error("camera event tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"camera event tier"} $code"

    fun adjustedExposure(): Double = round(stopCount.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): CameraEventTier {
        return CameraEventTier(
            code = code,
            title = title,
            maximum = maximum,
            quantity = quantity,
            depot = depot,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a camera event tier in status new. */
fun newCameraEventTier(code: String, title: String): CameraEventTier {
    return CameraEventTier(
        code = code,
        title = title,
        maximum = 0.0,
        quantity = 0,
        depot = "",
        stopCount = 0,
        status = CameraEventTierStatus.NEW,
    )
}
