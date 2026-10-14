package com.acme.transport.safety.cameraevent.globaleventaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event schedule is in its life cycle. */
enum class CameraEventScheduleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A camera event schedule. Its code is unique within a tenant. */
data class CameraEventSchedule(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var endsAt: Int,
    var dock: String,
    var lane: String,
    var stopCount: Int,
    var etaMinutes: Int,
    var segment: String,
    var retries: Int,
    var status: CameraEventScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventScheduleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventScheduleStatus): Boolean {
        when (status) {
            CameraEventScheduleStatus.OPEN -> {
                when (next) {
                    CameraEventScheduleStatus.RESOLVED, CameraEventScheduleStatus.REOPENED -> return true
                }
                return false
            }
            CameraEventScheduleStatus.IN_REVIEW -> return next == CameraEventScheduleStatus.RESOLVED
            CameraEventScheduleStatus.RESOLVED -> {
                when (next) {
                    CameraEventScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
            CameraEventScheduleStatus.REOPENED -> return next == CameraEventScheduleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CameraEventScheduleStatus) {
        if (!canMoveTo(next)) {
            error("camera event schedule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"camera event schedule"} $code"

    fun netValue(): Double = round((endsAt.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): CameraEventSchedule {
        return CameraEventSchedule(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            endsAt = endsAt,
            dock = dock,
            lane = lane,
            stopCount = stopCount,
            etaMinutes = etaMinutes,
            segment = segment,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a camera event schedule in status open. */
fun newCameraEventSchedule(code: String, label: String): CameraEventSchedule {
    return CameraEventSchedule(
        code = code,
        label = label,
        durationMinutes = 0,
        endsAt = 0,
        dock = "",
        lane = "",
        stopCount = 0,
        etaMinutes = 0,
        segment = "",
        retries = 0,
        status = CameraEventScheduleStatus.OPEN,
    )
}
