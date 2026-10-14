package com.acme.transport.safety.cameraevent.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling camera event group is in its life cycle. */
enum class RollingCameraEventGroupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rolling camera event group as the camera event team keeps it. */
data class RollingCameraEventGroup(
    var code: String,
    var name: String,
    var cost: Double,
    var dock: String,
    var createdAt: Int,
    var retries: Int,
    var status: RollingCameraEventGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCameraEventGroupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCameraEventGroupStatus): Boolean {
        when (status) {
            RollingCameraEventGroupStatus.IDLE -> {
                when (next) {
                    RollingCameraEventGroupStatus.WARMING -> return true
                }
                return false
            }
            RollingCameraEventGroupStatus.WARMING -> {
                when (next) {
                    RollingCameraEventGroupStatus.READY, RollingCameraEventGroupStatus.DRAINING -> return true
                }
                return false
            }
            RollingCameraEventGroupStatus.READY -> return next == RollingCameraEventGroupStatus.DRAINING || next == RollingCameraEventGroupStatus.STOPPED
            RollingCameraEventGroupStatus.DRAINING -> {
                when (next) {
                    RollingCameraEventGroupStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingCameraEventGroupStatus) {
        if (!canMoveTo(next)) {
            error("rolling camera event group $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun effectiveVolume(): Double = round((retries.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): RollingCameraEventGroup {
        return RollingCameraEventGroup(
            code = code,
            name = name,
            cost = cost,
            dock = dock,
            createdAt = createdAt,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling camera event group in status idle. */
fun newRollingCameraEventGroup(code: String, name: String): RollingCameraEventGroup {
    return RollingCameraEventGroup(
        code = code,
        name = name,
        cost = 0.0,
        dock = "",
        createdAt = 0,
        retries = 0,
        status = RollingCameraEventGroupStatus.IDLE,
    )
}
