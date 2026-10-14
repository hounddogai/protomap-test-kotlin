package com.acme.transport.safety.cameraevent.eventclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual camera event baseline is in its life cycle. */
enum class ManualCameraEventBaselineStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A manual camera event baseline as the camera event team keeps it. */
data class ManualCameraEventBaseline(
    var code: String,
    var name: String,
    var reviewer: String,
    var createdAt: Int,
    var durationMinutes: Int,
    var minimum: Double,
    var version: Int,
    var status: ManualCameraEventBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCameraEventBaselineStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCameraEventBaselineStatus): Boolean {
        when (status) {
            ManualCameraEventBaselineStatus.IDLE -> {
                when (next) {
                    ManualCameraEventBaselineStatus.STOPPED -> return true
                }
                return false
            }
            ManualCameraEventBaselineStatus.WARMING -> return next == ManualCameraEventBaselineStatus.READY
            ManualCameraEventBaselineStatus.READY -> {
                when (next) {
                    ManualCameraEventBaselineStatus.DRAINING -> return true
                }
                return false
            }
            ManualCameraEventBaselineStatus.DRAINING -> return next == ManualCameraEventBaselineStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ManualCameraEventBaselineStatus) {
        if (!canMoveTo(next)) {
            error("manual camera event baseline $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"manual camera event baseline"} $code"

    fun netTotal(): Double = round(version.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): ManualCameraEventBaseline {
        return ManualCameraEventBaseline(
            code = code,
            name = name,
            reviewer = reviewer,
            createdAt = createdAt,
            durationMinutes = durationMinutes,
            minimum = minimum,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual camera event baseline in status idle. */
fun newManualCameraEventBaseline(code: String, name: String): ManualCameraEventBaseline {
    return ManualCameraEventBaseline(
        code = code,
        name = name,
        reviewer = "",
        createdAt = 0,
        durationMinutes = 0,
        minimum = 0.0,
        version = 0,
        status = ManualCameraEventBaselineStatus.IDLE,
    )
}
