package com.acme.transport.customs.tariff.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback tariff baseline is in its life cycle. */
enum class FallbackTariffBaselineStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fallback tariff baseline as the tariff team keeps it. */
data class FallbackTariffBaseline(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var windowDays: Int,
    var maximum: Double,
    var status: FallbackTariffBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackTariffBaselineStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackTariffBaselineStatus): Boolean {
        when (status) {
            FallbackTariffBaselineStatus.IDLE -> {
                when (next) {
                    FallbackTariffBaselineStatus.WARMING -> return true
                }
                return false
            }
            FallbackTariffBaselineStatus.WARMING -> return next == FallbackTariffBaselineStatus.STOPPED
            FallbackTariffBaselineStatus.READY -> return next == FallbackTariffBaselineStatus.DRAINING || next == FallbackTariffBaselineStatus.STOPPED
            FallbackTariffBaselineStatus.DRAINING -> {
                when (next) {
                    FallbackTariffBaselineStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackTariffBaselineStatus) {
        if (!canMoveTo(next)) {
            error("fallback tariff baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback tariff baseline"} $code"

    fun effectiveLoad(): Double = round(windowDays.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): FallbackTariffBaseline {
        return FallbackTariffBaseline(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            windowDays = windowDays,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback tariff baseline in status idle. */
fun newFallbackTariffBaseline(code: String, name: String): FallbackTariffBaseline {
    return FallbackTariffBaseline(
        code = code,
        name = name,
        durationMinutes = 0,
        windowDays = 0,
        maximum = 0.0,
        status = FallbackTariffBaselineStatus.IDLE,
    )
}
