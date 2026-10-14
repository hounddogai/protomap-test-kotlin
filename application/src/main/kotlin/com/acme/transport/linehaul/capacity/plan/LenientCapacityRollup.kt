package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient capacity rollup is in its life cycle. */
enum class LenientCapacityRollupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One lenient capacity rollup, with what the planners need to act on it. */
data class LenientCapacityRollup(
    var code: String,
    var title: String,
    var checksum: String,
    var minimum: Double,
    var expiresAt: Int,
    var driverId: String,
    var startsAt: Int,
    var channel: String,
    var windowDays: Int,
    var status: LenientCapacityRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCapacityRollupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCapacityRollupStatus): Boolean {
        when (status) {
            LenientCapacityRollupStatus.PROPOSED -> {
                when (next) {
                    LenientCapacityRollupStatus.REJECTED -> return true
                }
                return false
            }
            LenientCapacityRollupStatus.APPROVED -> return next == LenientCapacityRollupStatus.REJECTED || next == LenientCapacityRollupStatus.WITHDRAWN
            LenientCapacityRollupStatus.REJECTED -> return next == LenientCapacityRollupStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LenientCapacityRollupStatus) {
        if (!canMoveTo(next)) {
            error("lenient capacity rollup $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun effectiveTotal(): Double = round((minimum - startsAt.toDouble()) * 100) / 100

    fun snapshot(): LenientCapacityRollup {
        return LenientCapacityRollup(
            code = code,
            title = title,
            checksum = checksum,
            minimum = minimum,
            expiresAt = expiresAt,
            driverId = driverId,
            startsAt = startsAt,
            channel = channel,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a lenient capacity rollup in status proposed. */
fun newLenientCapacityRollup(code: String, title: String): LenientCapacityRollup {
    return LenientCapacityRollup(
        code = code,
        title = title,
        checksum = "",
        minimum = 0.0,
        expiresAt = 0,
        driverId = "",
        startsAt = 0,
        channel = "",
        windowDays = 0,
        status = LenientCapacityRollupStatus.PROPOSED,
    )
}
