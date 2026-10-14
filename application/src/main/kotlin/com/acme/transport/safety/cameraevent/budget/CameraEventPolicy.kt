package com.acme.transport.safety.cameraevent.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event policy is in its life cycle. */
enum class CameraEventPolicyStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A camera event policy. Its code is unique within a tenant. */
data class CameraEventPolicy(
    var code: String,
    var name: String,
    var vehicleId: String,
    var label: String,
    var timezone: String,
    var quantity: Int,
    var discount: Double,
    var endsAt: Int,
    var status: CameraEventPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventPolicyStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventPolicyStatus): Boolean {
        when (status) {
            CameraEventPolicyStatus.PLANNED -> {
                when (next) {
                    CameraEventPolicyStatus.COMPLETED -> return true
                }
                return false
            }
            CameraEventPolicyStatus.SCHEDULED -> return next == CameraEventPolicyStatus.COMPLETED || next == CameraEventPolicyStatus.CANCELLED
            CameraEventPolicyStatus.IN_PROGRESS -> {
                when (next) {
                    CameraEventPolicyStatus.COMPLETED -> return true
                }
                return false
            }
            CameraEventPolicyStatus.COMPLETED -> return next == CameraEventPolicyStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CameraEventPolicyStatus) {
        if (!canMoveTo(next)) {
            error("camera event policy $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"camera event policy"} $code"

    fun effectiveTotal(): Double = round(endsAt.toDouble() * discount * 100) / 100

    fun snapshot(): CameraEventPolicy {
        return CameraEventPolicy(
            code = code,
            name = name,
            vehicleId = vehicleId,
            label = label,
            timezone = timezone,
            quantity = quantity,
            discount = discount,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a camera event policy in status planned. */
fun newCameraEventPolicy(code: String, name: String): CameraEventPolicy {
    return CameraEventPolicy(
        code = code,
        name = name,
        vehicleId = "",
        label = "",
        timezone = "",
        quantity = 0,
        discount = 0.0,
        endsAt = 0,
        status = CameraEventPolicyStatus.PLANNED,
    )
}
