package com.acme.transport.safety.cameraevent.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event profile is in its life cycle. */
enum class CameraEventProfileStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One camera event profile, with what the planners need to act on it. */
data class CameraEventProfile(
    var code: String,
    var title: String,
    var revision: Int,
    var distanceKm: Double,
    var carrier: String,
    var driverId: String,
    var updatedAt: Int,
    var status: CameraEventProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventProfileStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventProfileStatus): Boolean {
        when (status) {
            CameraEventProfileStatus.IDLE -> return next == CameraEventProfileStatus.WARMING
            CameraEventProfileStatus.WARMING -> return next == CameraEventProfileStatus.READY
            CameraEventProfileStatus.READY -> return next == CameraEventProfileStatus.DRAINING || next == CameraEventProfileStatus.STOPPED
            CameraEventProfileStatus.DRAINING -> return next == CameraEventProfileStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CameraEventProfileStatus) {
        if (!canMoveTo(next)) {
            error("camera event profile $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun effectiveValue(): Double = round((distanceKm - revision.toDouble()) * 100) / 100

    fun snapshot(): CameraEventProfile {
        return CameraEventProfile(
            code = code,
            title = title,
            revision = revision,
            distanceKm = distanceKm,
            carrier = carrier,
            driverId = driverId,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a camera event profile in status idle. */
fun newCameraEventProfile(code: String, title: String): CameraEventProfile {
    return CameraEventProfile(
        code = code,
        title = title,
        revision = 0,
        distanceKm = 0.0,
        carrier = "",
        driverId = "",
        updatedAt = 0,
        status = CameraEventProfileStatus.IDLE,
    )
}
