package com.acme.transport.safety.nearmiss.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global near miss request is in its life cycle. */
enum class GlobalNearMissRequestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One global near miss request, with what the planners need to act on it. */
data class GlobalNearMissRequest(
    var code: String,
    var title: String,
    var revision: Int,
    var createdAt: Int,
    var version: Int,
    var distanceKm: Double,
    var durationMinutes: Int,
    var driverId: String,
    var status: GlobalNearMissRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalNearMissRequestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalNearMissRequestStatus): Boolean {
        when (status) {
            GlobalNearMissRequestStatus.IDLE -> {
                when (next) {
                    GlobalNearMissRequestStatus.WARMING, GlobalNearMissRequestStatus.READY -> return true
                }
                return false
            }
            GlobalNearMissRequestStatus.WARMING -> return next == GlobalNearMissRequestStatus.READY || next == GlobalNearMissRequestStatus.DRAINING
            GlobalNearMissRequestStatus.READY -> return next == GlobalNearMissRequestStatus.DRAINING
            GlobalNearMissRequestStatus.DRAINING -> {
                when (next) {
                    GlobalNearMissRequestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalNearMissRequestStatus) {
        if (!canMoveTo(next)) {
            error("global near miss request $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun adjustedTotal(): Double = round((version.toDouble() + createdAt.toDouble()) * 100) / 100

    fun snapshot(): GlobalNearMissRequest {
        return GlobalNearMissRequest(
            code = code,
            title = title,
            revision = revision,
            createdAt = createdAt,
            version = version,
            distanceKm = distanceKm,
            durationMinutes = durationMinutes,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global near miss request in status idle. */
fun newGlobalNearMissRequest(code: String, title: String): GlobalNearMissRequest {
    return GlobalNearMissRequest(
        code = code,
        title = title,
        revision = 0,
        createdAt = 0,
        version = 0,
        distanceKm = 0.0,
        durationMinutes = 0,
        driverId = "",
        status = GlobalNearMissRequestStatus.IDLE,
    )
}
