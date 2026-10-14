package com.acme.transport.customs.clearance.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling clearance adjustment is in its life cycle. */
enum class RollingClearanceAdjustmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One rolling clearance adjustment, with what the planners need to act on it. */
data class RollingClearanceAdjustment(
    var code: String,
    var title: String,
    var label: String,
    var minimum: Double,
    var retries: Int,
    var lane: String,
    var pinned: Boolean,
    var floorValue: Double,
    var locale: String,
    var status: RollingClearanceAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingClearanceAdjustmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingClearanceAdjustmentStatus): Boolean {
        when (status) {
            RollingClearanceAdjustmentStatus.REQUESTED -> {
                when (next) {
                    RollingClearanceAdjustmentStatus.GRANTED, RollingClearanceAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
            RollingClearanceAdjustmentStatus.GRANTED -> {
                when (next) {
                    RollingClearanceAdjustmentStatus.REVOKED, RollingClearanceAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
            RollingClearanceAdjustmentStatus.REVOKED -> return next == RollingClearanceAdjustmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RollingClearanceAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("rolling clearance adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun effectiveExposure(): Double = round(floorValue * retries.toDouble() * 100) / 100

    fun snapshot(): RollingClearanceAdjustment {
        return RollingClearanceAdjustment(
            code = code,
            title = title,
            label = label,
            minimum = minimum,
            retries = retries,
            lane = lane,
            pinned = pinned,
            floorValue = floorValue,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rolling clearance adjustment in status requested. */
fun newRollingClearanceAdjustment(code: String, title: String): RollingClearanceAdjustment {
    return RollingClearanceAdjustment(
        code = code,
        title = title,
        label = "",
        minimum = 0.0,
        retries = 0,
        lane = "",
        pinned = false,
        floorValue = 0.0,
        locale = "",
        status = RollingClearanceAdjustmentStatus.REQUESTED,
    )
}
