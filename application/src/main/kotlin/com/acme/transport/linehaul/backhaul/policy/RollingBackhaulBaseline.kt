package com.acme.transport.linehaul.backhaul.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling backhaul baseline is in its life cycle. */
enum class RollingBackhaulBaselineStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One rolling backhaul baseline, with what the planners need to act on it. */
data class RollingBackhaulBaseline(
    var code: String,
    var title: String,
    var floorValue: Double,
    var phone: String,
    var fuelLiters: Double,
    var updatedAt: Int,
    var locale: String,
    var loadKg: Double,
    var status: RollingBackhaulBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingBackhaulBaselineStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingBackhaulBaselineStatus): Boolean {
        when (status) {
            RollingBackhaulBaselineStatus.IDLE -> return next == RollingBackhaulBaselineStatus.WARMING || next == RollingBackhaulBaselineStatus.STOPPED
            RollingBackhaulBaselineStatus.WARMING -> {
                when (next) {
                    RollingBackhaulBaselineStatus.READY, RollingBackhaulBaselineStatus.STOPPED -> return true
                }
                return false
            }
            RollingBackhaulBaselineStatus.READY -> return next == RollingBackhaulBaselineStatus.DRAINING
            RollingBackhaulBaselineStatus.DRAINING -> return next == RollingBackhaulBaselineStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RollingBackhaulBaselineStatus) {
        if (!canMoveTo(next)) {
            error("rolling backhaul baseline $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netVolume(): Double = round((floorValue + fuelLiters) * 100) / 100

    fun snapshot(): RollingBackhaulBaseline {
        return RollingBackhaulBaseline(
            code = code,
            title = title,
            floorValue = floorValue,
            phone = phone,
            fuelLiters = fuelLiters,
            updatedAt = updatedAt,
            locale = locale,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rolling backhaul baseline in status idle. */
fun newRollingBackhaulBaseline(code: String, title: String): RollingBackhaulBaseline {
    return RollingBackhaulBaseline(
        code = code,
        title = title,
        floorValue = 0.0,
        phone = "",
        fuelLiters = 0.0,
        updatedAt = 0,
        locale = "",
        loadKg = 0.0,
        status = RollingBackhaulBaselineStatus.IDLE,
    )
}
