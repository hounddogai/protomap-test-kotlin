package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling fuel burn target is in its life cycle. */
enum class RollingFuelBurnTargetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One rolling fuel burn target, with what the planners need to act on it. */
data class RollingFuelBurnTarget(
    var code: String,
    var title: String,
    var region: String,
    var threshold: Double,
    var capacity: Int,
    var stopCount: Int,
    var startsAt: Int,
    var category: String,
    var visible: Boolean,
    var ratio: Double,
    var retries: Int,
    var status: RollingFuelBurnTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingFuelBurnTargetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingFuelBurnTargetStatus): Boolean {
        when (status) {
            RollingFuelBurnTargetStatus.IDLE -> {
                when (next) {
                    RollingFuelBurnTargetStatus.READY, RollingFuelBurnTargetStatus.DRAINING -> return true
                }
                return false
            }
            RollingFuelBurnTargetStatus.WARMING -> {
                when (next) {
                    RollingFuelBurnTargetStatus.DRAINING -> return true
                }
                return false
            }
            RollingFuelBurnTargetStatus.READY -> {
                when (next) {
                    RollingFuelBurnTargetStatus.DRAINING, RollingFuelBurnTargetStatus.STOPPED -> return true
                }
                return false
            }
            RollingFuelBurnTargetStatus.DRAINING -> return next == RollingFuelBurnTargetStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RollingFuelBurnTargetStatus) {
        if (!canMoveTo(next)) {
            error("rolling fuel burn target $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"rolling fuel burn target"} $code"

    fun projectedValue(): Double = round((retries.toDouble() + ratio) * 100) / 100

    fun snapshot(): RollingFuelBurnTarget {
        return RollingFuelBurnTarget(
            code = code,
            title = title,
            region = region,
            threshold = threshold,
            capacity = capacity,
            stopCount = stopCount,
            startsAt = startsAt,
            category = category,
            visible = visible,
            ratio = ratio,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rolling fuel burn target in status idle. */
fun newRollingFuelBurnTarget(code: String, title: String): RollingFuelBurnTarget {
    return RollingFuelBurnTarget(
        code = code,
        title = title,
        region = "",
        threshold = 0.0,
        capacity = 0,
        stopCount = 0,
        startsAt = 0,
        category = "",
        visible = false,
        ratio = 0.0,
        retries = 0,
        status = RollingFuelBurnTargetStatus.IDLE,
    )
}
