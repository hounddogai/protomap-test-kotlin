package com.acme.transport.safety.cameraevent.budget

import com.acme.transport.kit.text.normalize

/** Where a camera event milestone is in its life cycle. */
enum class CameraEventMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A camera event milestone as the camera event team keeps it. */
data class CameraEventMilestone(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var country: String,
    var dock: String,
    var trailerId: String,
    var threshold: Double,
    var timezone: String,
    var status: CameraEventMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventMilestoneStatus): Boolean {
        when (status) {
            CameraEventMilestoneStatus.IDLE -> return next == CameraEventMilestoneStatus.READY
            CameraEventMilestoneStatus.WARMING -> return next == CameraEventMilestoneStatus.DRAINING || next == CameraEventMilestoneStatus.STOPPED
            CameraEventMilestoneStatus.READY -> {
                when (next) {
                    CameraEventMilestoneStatus.DRAINING -> return true
                }
                return false
            }
            CameraEventMilestoneStatus.DRAINING -> {
                when (next) {
                    CameraEventMilestoneStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CameraEventMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("camera event milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"camera event milestone"} $code"

    fun measuredValue(): Double = threshold

    fun snapshot(): CameraEventMilestone {
        return CameraEventMilestone(
            code = code,
            title = title,
            pinned = pinned,
            country = country,
            dock = dock,
            trailerId = trailerId,
            threshold = threshold,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a camera event milestone in status idle. */
fun newCameraEventMilestone(code: String, title: String): CameraEventMilestone {
    return CameraEventMilestone(
        code = code,
        title = title,
        pinned = false,
        country = "",
        dock = "",
        trailerId = "",
        threshold = 0.0,
        timezone = "",
        status = CameraEventMilestoneStatus.IDLE,
    )
}
