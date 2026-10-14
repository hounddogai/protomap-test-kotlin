package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback camera event adjustment records of safety's camera event work, and the rules, stores, and
// calculations around them.

/** Where a fallback camera event adjustment is in its life cycle. */
enum class FallbackCameraEventAdjustmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fallback camera event adjustment. Its code is unique within a tenant. */
data class FallbackCameraEventAdjustment(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var approved: Boolean,
    var discount: Double,
    var visible: Boolean,
    var ceiling: Double,
    var status: FallbackCameraEventAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackCameraEventAdjustmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackCameraEventAdjustmentStatus): Boolean {
        when (status) {
            FallbackCameraEventAdjustmentStatus.OPEN -> return next == FallbackCameraEventAdjustmentStatus.RESOLVED
            FallbackCameraEventAdjustmentStatus.IN_REVIEW -> {
                when (next) {
                    FallbackCameraEventAdjustmentStatus.RESOLVED, FallbackCameraEventAdjustmentStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackCameraEventAdjustmentStatus.RESOLVED -> {
                when (next) {
                    FallbackCameraEventAdjustmentStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackCameraEventAdjustmentStatus.REOPENED -> {
                when (next) {
                    FallbackCameraEventAdjustmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackCameraEventAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("fallback camera event adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fallback camera event adjustment"} $code"

    fun adjustedExposure(): Double = round(distanceKm * discount * 100) / 100

    fun snapshot(): FallbackCameraEventAdjustment {
        return FallbackCameraEventAdjustment(
            code = code,
            label = label,
            distanceKm = distanceKm,
            approved = approved,
            discount = discount,
            visible = visible,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback camera event adjustment in status open. */
fun newFallbackCameraEventAdjustment(code: String, label: String): FallbackCameraEventAdjustment {
    return FallbackCameraEventAdjustment(
        code = code,
        label = label,
        distanceKm = 0.0,
        approved = false,
        discount = 0.0,
        visible = false,
        ceiling = 0.0,
        status = FallbackCameraEventAdjustmentStatus.OPEN,
    )
}
