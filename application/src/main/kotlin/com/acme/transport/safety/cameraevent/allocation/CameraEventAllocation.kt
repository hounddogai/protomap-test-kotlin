package com.acme.transport.safety.cameraevent.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The camera event allocation records of safety's camera event work, and the rules, stores, and calculations
// around them.

/** Where a camera event allocation is in its life cycle. */
enum class CameraEventAllocationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A camera event allocation as the camera event team keeps it. */
data class CameraEventAllocation(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var priority: Int,
    var ceiling: Double,
    var phone: String,
    var etaMinutes: Int,
    var contactEmail: String,
    var discount: Double,
    var source: String,
    var status: CameraEventAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventAllocationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventAllocationStatus): Boolean {
        when (status) {
            CameraEventAllocationStatus.PLANNED -> {
                when (next) {
                    CameraEventAllocationStatus.IN_PROGRESS -> return true
                }
                return false
            }
            CameraEventAllocationStatus.SCHEDULED -> {
                when (next) {
                    CameraEventAllocationStatus.IN_PROGRESS -> return true
                }
                return false
            }
            CameraEventAllocationStatus.IN_PROGRESS -> return next == CameraEventAllocationStatus.COMPLETED || next == CameraEventAllocationStatus.CANCELLED
            CameraEventAllocationStatus.COMPLETED -> return next == CameraEventAllocationStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CameraEventAllocationStatus) {
        if (!canMoveTo(next)) {
            error("camera event allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun netLoad(): Double = round(discount * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): CameraEventAllocation {
        return CameraEventAllocation(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            priority = priority,
            ceiling = ceiling,
            phone = phone,
            etaMinutes = etaMinutes,
            contactEmail = contactEmail,
            discount = discount,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a camera event allocation in status planned. */
fun newCameraEventAllocation(code: String, name: String): CameraEventAllocation {
    return CameraEventAllocation(
        code = code,
        name = name,
        fuelLiters = 0.0,
        priority = 0,
        ceiling = 0.0,
        phone = "",
        etaMinutes = 0,
        contactEmail = "",
        discount = 0.0,
        source = "",
        status = CameraEventAllocationStatus.PLANNED,
    )
}
