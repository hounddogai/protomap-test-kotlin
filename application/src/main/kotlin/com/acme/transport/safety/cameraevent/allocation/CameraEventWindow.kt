package com.acme.transport.safety.cameraevent.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event window is in its life cycle. */
enum class CameraEventWindowStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a camera event window and its current status. */
data class CameraEventWindow(
    var code: String,
    var label: String,
    var priority: Int,
    var trailerId: String,
    var startsAt: Int,
    var segment: String,
    var unit: String,
    var phone: String,
    var status: CameraEventWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventWindowStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventWindowStatus): Boolean {
        when (status) {
            CameraEventWindowStatus.OPEN -> {
                when (next) {
                    CameraEventWindowStatus.RESOLVED, CameraEventWindowStatus.REOPENED -> return true
                }
                return false
            }
            CameraEventWindowStatus.IN_REVIEW -> return next == CameraEventWindowStatus.RESOLVED
            CameraEventWindowStatus.RESOLVED -> {
                when (next) {
                    CameraEventWindowStatus.REOPENED -> return true
                }
                return false
            }
            CameraEventWindowStatus.REOPENED -> {
                when (next) {
                    CameraEventWindowStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CameraEventWindowStatus) {
        if (!canMoveTo(next)) {
            error("camera event window $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun netExposure(): Double = round((priority.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): CameraEventWindow {
        return CameraEventWindow(
            code = code,
            label = label,
            priority = priority,
            trailerId = trailerId,
            startsAt = startsAt,
            segment = segment,
            unit = unit,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a camera event window in status open. */
fun newCameraEventWindow(code: String, label: String): CameraEventWindow {
    return CameraEventWindow(
        code = code,
        label = label,
        priority = 0,
        trailerId = "",
        startsAt = 0,
        segment = "",
        unit = "",
        phone = "",
        status = CameraEventWindowStatus.OPEN,
    )
}
