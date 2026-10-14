package com.acme.transport.safety.cameraevent.eventbatch

import com.acme.transport.kit.text.normalize

/** Where a camera event segment is in its life cycle. */
enum class CameraEventSegmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A camera event segment as the camera event team keeps it. */
data class CameraEventSegment(
    var code: String,
    var title: String,
    var approved: Boolean,
    var active: Boolean,
    var name: String,
    var ceiling: Double,
    var status: CameraEventSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventSegmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventSegmentStatus): Boolean {
        when (status) {
            CameraEventSegmentStatus.NEW -> {
                when (next) {
                    CameraEventSegmentStatus.DONE -> return true
                }
                return false
            }
            CameraEventSegmentStatus.QUEUED -> {
                when (next) {
                    CameraEventSegmentStatus.DONE, CameraEventSegmentStatus.FAILED -> return true
                }
                return false
            }
            CameraEventSegmentStatus.RUNNING -> return next == CameraEventSegmentStatus.DONE || next == CameraEventSegmentStatus.FAILED
            CameraEventSegmentStatus.DONE -> {
                when (next) {
                    CameraEventSegmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CameraEventSegmentStatus) {
        if (!canMoveTo(next)) {
            error("camera event segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"camera event segment"} $code"

    fun measuredValue(): Double = ceiling

    fun snapshot(): CameraEventSegment {
        return CameraEventSegment(
            code = code,
            title = title,
            approved = approved,
            active = active,
            name = name,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a camera event segment in status new. */
fun newCameraEventSegment(code: String, title: String): CameraEventSegment {
    return CameraEventSegment(
        code = code,
        title = title,
        approved = false,
        active = false,
        name = "",
        ceiling = 0.0,
        status = CameraEventSegmentStatus.NEW,
    )
}
