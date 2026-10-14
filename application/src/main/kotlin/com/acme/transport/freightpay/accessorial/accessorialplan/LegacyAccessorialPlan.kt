package com.acme.transport.freightpay.accessorial.accessorialplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy accessorial plan records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a legacy accessorial plan is in its life cycle. */
enum class LegacyAccessorialPlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy accessorial plan. Its code is unique within a tenant. */
data class LegacyAccessorialPlan(
    var code: String,
    var name: String,
    var priority: Int,
    var weight: Double,
    var windowDays: Int,
    var timezone: String,
    var maximum: Double,
    var retries: Int,
    var pinned: Boolean,
    var status: LegacyAccessorialPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyAccessorialPlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyAccessorialPlanStatus): Boolean {
        when (status) {
            LegacyAccessorialPlanStatus.IDLE -> {
                when (next) {
                    LegacyAccessorialPlanStatus.WARMING, LegacyAccessorialPlanStatus.DRAINING -> return true
                }
                return false
            }
            LegacyAccessorialPlanStatus.WARMING -> return next == LegacyAccessorialPlanStatus.READY
            LegacyAccessorialPlanStatus.READY -> return next == LegacyAccessorialPlanStatus.DRAINING || next == LegacyAccessorialPlanStatus.STOPPED
            LegacyAccessorialPlanStatus.DRAINING -> return next == LegacyAccessorialPlanStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LegacyAccessorialPlanStatus) {
        if (!canMoveTo(next)) {
            error("legacy accessorial plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy accessorial plan"} $code"

    fun netTotal(): Double = round((windowDays.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): LegacyAccessorialPlan {
        return LegacyAccessorialPlan(
            code = code,
            name = name,
            priority = priority,
            weight = weight,
            windowDays = windowDays,
            timezone = timezone,
            maximum = maximum,
            retries = retries,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy accessorial plan in status idle. */
fun newLegacyAccessorialPlan(code: String, name: String): LegacyAccessorialPlan {
    return LegacyAccessorialPlan(
        code = code,
        name = name,
        priority = 0,
        weight = 0.0,
        windowDays = 0,
        timezone = "",
        maximum = 0.0,
        retries = 0,
        pinned = false,
        status = LegacyAccessorialPlanStatus.IDLE,
    )
}
