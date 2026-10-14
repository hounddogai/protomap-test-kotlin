package com.acme.transport.safety.cameraevent.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event request is in its life cycle. */
enum class CameraEventRequestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A camera event request. Its code is unique within a tenant. */
data class CameraEventRequest(
    var code: String,
    var label: String,
    var approved: Boolean,
    var amount: Double,
    var dock: String,
    var pallets: Int,
    var cost: Double,
    var status: CameraEventRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventRequestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventRequestStatus): Boolean {
        when (status) {
            CameraEventRequestStatus.CREATED -> return next == CameraEventRequestStatus.VALIDATED || next == CameraEventRequestStatus.PUBLISHED
            CameraEventRequestStatus.VALIDATED -> {
                when (next) {
                    CameraEventRequestStatus.RETIRED -> return true
                }
                return false
            }
            CameraEventRequestStatus.PUBLISHED -> {
                when (next) {
                    CameraEventRequestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CameraEventRequestStatus) {
        if (!canMoveTo(next)) {
            error("camera event request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"camera event request"} $code"

    fun projectedExposure(): Double = round(cost * pallets.toDouble() * 100) / 100

    fun snapshot(): CameraEventRequest {
        return CameraEventRequest(
            code = code,
            label = label,
            approved = approved,
            amount = amount,
            dock = dock,
            pallets = pallets,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a camera event request in status created. */
fun newCameraEventRequest(code: String, label: String): CameraEventRequest {
    return CameraEventRequest(
        code = code,
        label = label,
        approved = false,
        amount = 0.0,
        dock = "",
        pallets = 0,
        cost = 0.0,
        status = CameraEventRequestStatus.CREATED,
    )
}
