package com.acme.transport.safety.cameraevent.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event notice is in its life cycle. */
enum class CameraEventNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A camera event notice as the camera event team keeps it. */
data class CameraEventNotice(
    var code: String,
    var label: String,
    var budget: Double,
    var sequence: Int,
    var fuelLiters: Double,
    var summary: String,
    var contactEmail: String,
    var shiftCode: String,
    var ceiling: Double,
    var status: CameraEventNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventNoticeStatus): Boolean {
        when (status) {
            CameraEventNoticeStatus.IDLE -> {
                when (next) {
                    CameraEventNoticeStatus.READY, CameraEventNoticeStatus.STOPPED -> return true
                }
                return false
            }
            CameraEventNoticeStatus.WARMING -> return next == CameraEventNoticeStatus.DRAINING
            CameraEventNoticeStatus.READY -> return next == CameraEventNoticeStatus.DRAINING || next == CameraEventNoticeStatus.STOPPED
            CameraEventNoticeStatus.DRAINING -> return next == CameraEventNoticeStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CameraEventNoticeStatus) {
        if (!canMoveTo(next)) {
            error("camera event notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedTotal(): Double = round((budget - sequence.toDouble()) * 100) / 100

    fun snapshot(): CameraEventNotice {
        return CameraEventNotice(
            code = code,
            label = label,
            budget = budget,
            sequence = sequence,
            fuelLiters = fuelLiters,
            summary = summary,
            contactEmail = contactEmail,
            shiftCode = shiftCode,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a camera event notice in status idle. */
fun newCameraEventNotice(code: String, label: String): CameraEventNotice {
    return CameraEventNotice(
        code = code,
        label = label,
        budget = 0.0,
        sequence = 0,
        fuelLiters = 0.0,
        summary = "",
        contactEmail = "",
        shiftCode = "",
        ceiling = 0.0,
        status = CameraEventNoticeStatus.IDLE,
    )
}
