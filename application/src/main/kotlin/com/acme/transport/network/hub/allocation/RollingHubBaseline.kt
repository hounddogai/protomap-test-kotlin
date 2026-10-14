package com.acme.transport.network.hub.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling hub baseline is in its life cycle. */
enum class RollingHubBaselineStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One rolling hub baseline, with what the planners need to act on it. */
data class RollingHubBaseline(
    var code: String,
    var label: String,
    var visible: Boolean,
    var archived: Boolean,
    var capacity: Int,
    var minimum: Double,
    var endsAt: Int,
    var version: Int,
    var status: RollingHubBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingHubBaselineStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingHubBaselineStatus): Boolean {
        when (status) {
            RollingHubBaselineStatus.PLANNED -> return next == RollingHubBaselineStatus.SCHEDULED || next == RollingHubBaselineStatus.COMPLETED
            RollingHubBaselineStatus.SCHEDULED -> {
                when (next) {
                    RollingHubBaselineStatus.CANCELLED -> return true
                }
                return false
            }
            RollingHubBaselineStatus.IN_PROGRESS -> return next == RollingHubBaselineStatus.CANCELLED
            RollingHubBaselineStatus.COMPLETED -> {
                when (next) {
                    RollingHubBaselineStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingHubBaselineStatus) {
        if (!canMoveTo(next)) {
            error("rolling hub baseline $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "${"rolling hub baseline"} $code"

    fun weightedExposure(): Double = round((version.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): RollingHubBaseline {
        return RollingHubBaseline(
            code = code,
            label = label,
            visible = visible,
            archived = archived,
            capacity = capacity,
            minimum = minimum,
            endsAt = endsAt,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling hub baseline in status planned. */
fun newRollingHubBaseline(code: String, label: String): RollingHubBaseline {
    return RollingHubBaseline(
        code = code,
        label = label,
        visible = false,
        archived = false,
        capacity = 0,
        minimum = 0.0,
        endsAt = 0,
        version = 0,
        status = RollingHubBaselineStatus.PLANNED,
    )
}
