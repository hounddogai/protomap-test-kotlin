package com.acme.transport.fleetops.tire.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy tire cycle is in its life cycle. */
enum class LegacyTireCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy tire cycle as the tire team keeps it. */
data class LegacyTireCycle(
    var code: String,
    var title: String,
    var windowDays: Int,
    var attempts: Int,
    var dock: String,
    var timezone: String,
    var shiftCode: String,
    var status: LegacyTireCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTireCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTireCycleStatus): Boolean {
        when (status) {
            LegacyTireCycleStatus.PLANNED -> return next == LegacyTireCycleStatus.CANCELLED
            LegacyTireCycleStatus.SCHEDULED -> return next == LegacyTireCycleStatus.IN_PROGRESS || next == LegacyTireCycleStatus.COMPLETED
            LegacyTireCycleStatus.IN_PROGRESS -> return next == LegacyTireCycleStatus.CANCELLED
            LegacyTireCycleStatus.COMPLETED -> {
                when (next) {
                    LegacyTireCycleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyTireCycleStatus) {
        if (!canMoveTo(next)) {
            error("legacy tire cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy tire cycle"} $code"

    fun adjustedLoad(): Double = round((attempts.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): LegacyTireCycle {
        return LegacyTireCycle(
            code = code,
            title = title,
            windowDays = windowDays,
            attempts = attempts,
            dock = dock,
            timezone = timezone,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy tire cycle in status planned. */
fun newLegacyTireCycle(code: String, title: String): LegacyTireCycle {
    return LegacyTireCycle(
        code = code,
        title = title,
        windowDays = 0,
        attempts = 0,
        dock = "",
        timezone = "",
        shiftCode = "",
        status = LegacyTireCycleStatus.PLANNED,
    )
}
