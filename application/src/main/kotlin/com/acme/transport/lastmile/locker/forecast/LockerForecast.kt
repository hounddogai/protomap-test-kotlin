package com.acme.transport.lastmile.locker.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The locker forecast records of lastmile's locker work, and the rules, stores, and calculations around them.

/** Where a locker forecast is in its life cycle. */
enum class LockerForecastStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A locker forecast as the locker team keeps it. */
data class LockerForecast(
    var code: String,
    var label: String,
    var checksum: String,
    var dock: String,
    var floorValue: Double,
    var priority: Int,
    var pinned: Boolean,
    var durationMinutes: Int,
    var status: LockerForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerForecastStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerForecastStatus): Boolean {
        when (status) {
            LockerForecastStatus.OPEN -> return next == LockerForecastStatus.ARCHIVED
            LockerForecastStatus.IN_REVIEW -> {
                when (next) {
                    LockerForecastStatus.RESOLVED, LockerForecastStatus.REOPENED -> return true
                }
                return false
            }
            LockerForecastStatus.RESOLVED -> {
                when (next) {
                    LockerForecastStatus.REOPENED, LockerForecastStatus.ARCHIVED -> return true
                }
                return false
            }
            LockerForecastStatus.REOPENED -> return next == LockerForecastStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LockerForecastStatus) {
        if (!canMoveTo(next)) {
            error("locker forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedTotal(): Double = round((priority.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): LockerForecast {
        return LockerForecast(
            code = code,
            label = label,
            checksum = checksum,
            dock = dock,
            floorValue = floorValue,
            priority = priority,
            pinned = pinned,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a locker forecast in status open. */
fun newLockerForecast(code: String, label: String): LockerForecast {
    return LockerForecast(
        code = code,
        label = label,
        checksum = "",
        dock = "",
        floorValue = 0.0,
        priority = 0,
        pinned = false,
        durationMinutes = 0,
        status = LockerForecastStatus.OPEN,
    )
}
