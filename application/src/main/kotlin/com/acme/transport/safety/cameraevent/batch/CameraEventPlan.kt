package com.acme.transport.safety.cameraevent.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event plan is in its life cycle. */
enum class CameraEventPlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a camera event plan and its current status. */
data class CameraEventPlan(
    var code: String,
    var name: String,
    var checksum: String,
    var active: Boolean,
    var updatedAt: Int,
    var quantity: Int,
    var threshold: Double,
    var expiresAt: Int,
    var country: String,
    var capacity: Int,
    var status: CameraEventPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventPlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventPlanStatus): Boolean {
        when (status) {
            CameraEventPlanStatus.OPEN -> return next == CameraEventPlanStatus.RESOLVED || next == CameraEventPlanStatus.REOPENED
            CameraEventPlanStatus.IN_REVIEW -> return next == CameraEventPlanStatus.RESOLVED
            CameraEventPlanStatus.RESOLVED -> return next == CameraEventPlanStatus.ARCHIVED
            CameraEventPlanStatus.REOPENED -> {
                when (next) {
                    CameraEventPlanStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CameraEventPlanStatus) {
        if (!canMoveTo(next)) {
            error("camera event plan $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun netVolume(): Double = round(capacity.toDouble() * threshold * 100) / 100

    fun snapshot(): CameraEventPlan {
        return CameraEventPlan(
            code = code,
            name = name,
            checksum = checksum,
            active = active,
            updatedAt = updatedAt,
            quantity = quantity,
            threshold = threshold,
            expiresAt = expiresAt,
            country = country,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a camera event plan in status open. */
fun newCameraEventPlan(code: String, name: String): CameraEventPlan {
    return CameraEventPlan(
        code = code,
        name = name,
        checksum = "",
        active = false,
        updatedAt = 0,
        quantity = 0,
        threshold = 0.0,
        expiresAt = 0,
        country = "",
        capacity = 0,
        status = CameraEventPlanStatus.OPEN,
    )
}
