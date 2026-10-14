package com.acme.transport.safety.coaching.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global coaching adjustment is in its life cycle. */
enum class GlobalCoachingAdjustmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a global coaching adjustment and its current status. */
data class GlobalCoachingAdjustment(
    var code: String,
    var title: String,
    var shiftCode: String,
    var rate: Double,
    var floorValue: Double,
    var endsAt: Int,
    var timezone: String,
    var region: String,
    var status: GlobalCoachingAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCoachingAdjustmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCoachingAdjustmentStatus): Boolean {
        when (status) {
            GlobalCoachingAdjustmentStatus.OPEN -> {
                when (next) {
                    GlobalCoachingAdjustmentStatus.REOPENED, GlobalCoachingAdjustmentStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalCoachingAdjustmentStatus.IN_REVIEW -> return next == GlobalCoachingAdjustmentStatus.REOPENED || next == GlobalCoachingAdjustmentStatus.ARCHIVED
            GlobalCoachingAdjustmentStatus.RESOLVED -> {
                when (next) {
                    GlobalCoachingAdjustmentStatus.REOPENED, GlobalCoachingAdjustmentStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalCoachingAdjustmentStatus.REOPENED -> {
                when (next) {
                    GlobalCoachingAdjustmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalCoachingAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("global coaching adjustment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global coaching adjustment"} $code"

    fun weightedExposure(): Double = round((floorValue - rate) * 100) / 100

    fun snapshot(): GlobalCoachingAdjustment {
        return GlobalCoachingAdjustment(
            code = code,
            title = title,
            shiftCode = shiftCode,
            rate = rate,
            floorValue = floorValue,
            endsAt = endsAt,
            timezone = timezone,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a global coaching adjustment in status open. */
fun newGlobalCoachingAdjustment(code: String, title: String): GlobalCoachingAdjustment {
    return GlobalCoachingAdjustment(
        code = code,
        title = title,
        shiftCode = "",
        rate = 0.0,
        floorValue = 0.0,
        endsAt = 0,
        timezone = "",
        region = "",
        status = GlobalCoachingAdjustmentStatus.OPEN,
    )
}
